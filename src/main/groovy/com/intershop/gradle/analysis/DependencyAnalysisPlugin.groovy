/*
 * Copyright 2015 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.intershop.gradle.analysis

import com.intershop.gradle.analysis.extension.DependencyAnalysisExtension
import com.intershop.gradle.analysis.model.AnalyzedExternalDependency
import com.intershop.gradle.analysis.model.AnalyzedFileDependency
import com.intershop.gradle.analysis.model.AnalyzedProjectDependency
import com.intershop.gradle.analysis.task.DependencyAnalysisTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.LibraryBinaryIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.attributes.Attribute
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.component.local.model.OpaqueComponentArtifactIdentifier

/**
 * Implementation of the plugin
 */
class DependencyAnalysisPlugin implements Plugin<Project> {

    private static final String DEPENDENCIESCHECK = 'dependencyAnalysis'

    /**
     * Applies this prokugin to the project
     * @param project
     */
    void apply(Project project) {
        //applies the Base reporting pluging
        project.plugins.apply(ReportingBasePlugin)

        DependencyAnalysisExtension extension = project.extensions.findByType(DependencyAnalysisExtension) ?: project.extensions.create(DEPENDENCIESCHECK, DependencyAnalysisExtension, project)

        extension.reportsDir = project.extensions.getByType(ReportingExtension).file(DEPENDENCIESCHECK)

        project.getPlugins().withType(JavaPlugin.class, new Action<JavaPlugin>() {
            void execute(JavaPlugin javaPlugin) {
                JavaPluginConvention javaConvention =
                        project.getConvention().getPlugin(JavaPluginConvention.class)

                SourceSet sourceSet = javaConvention.getSourceSets().getByName(extension.getSourceset())
                Jar jar = (Jar) project.tasks.findByName(sourceSet.jarTaskName)

                DependencyAnalysisTask analysisTask = project.tasks.maybeCreate(DEPENDENCIESCHECK, DependencyAnalysisTask)
                analysisTask.setGroup('Verification')
                analysisTask.setDescription('Determines the usage of existing dependencies')
                analysisTask.setBase(jar.outputs.files)

                analysisTask.setFailOnDuplicates(extension.getFailOnDuplicatesProvider())
                analysisTask.setFailOnUnusedFirstLevelDependencies(extension.getFailOnUnusedFirstLevelDependenciesProvider())
                analysisTask.setFailOnUsedTransitiveDependencies(extension.getFailOnUsedTransitiveDependenciesProvider())
                analysisTask.setFailOnUnusedTransitiveDependencies(extension.getFailOnUnusedTransitiveDependenciesProvider())

                analysisTask.setExcludeDependencyPatterns(extension.getExcludeDependencyPatternsProvider())
                analysisTask.setExcludeDuplicatePatterns(extension.getExcludeDuplicatePatternsProvider())

                analysisTask.setHtmlReportDir(extension.getReportsDirProvider())

                project.getConfigurations().all(new Action<Configuration>() {
                    @Override
                    void execute(final Configuration conf) {
                        if (conf.getState() == Configuration.State.UNRESOLVED && conf.getName() == 'compileClasspath') {
                            conf.getIncoming().afterResolve(new Action<ResolvableDependencies>() {
                                @Override
                                void execute(ResolvableDependencies resolvableDependencies) {
                                    Map<String, AnalyzedExternalDependency> artifactMap = [:] as HashMap

                                    for (Dependency dependency : resolvableDependencies.getDependencies()) {
                                        List<AnalyzedExternalDependency> analyzedDependencies = createFirstLevelDependenciesFrom(dependency, conf)
                                        analyzedDependencies.each { analyzedDep ->
                                            artifactMap.put("${dependency.group}:${dependency.name}".toString(), analyzedDep)
                                        }

                                    }

                                    for (ResolvedArtifactResult rar : resolvableDependencies.getArtifacts()) {
                                        if (rar.getVariant().getAttributes().getAttribute(Attribute.of('artifactType', String)) == 'jar') {
                                            ComponentIdentifier ci = rar.getId().getComponentIdentifier()

                                            if (ci instanceof ProjectComponentIdentifier) {
                                                ProjectComponentIdentifier pci = (ProjectComponentIdentifier) ci
                                                Project p = project.getRootProject().project(pci.getProjectPath())

                                                AnalyzedExternalDependency a = artifactMap.get("${p.getGroup()}:${p.getName()}".toString())

                                                if (!a) {
                                                    a = new AnalyzedProjectDependency(p.getGroup().toString(), p.getName(), conf.getName())
                                                    artifactMap.put("${p.getGroup()}:${p.getName()}".toString(), a)
                                                }

                                                a.setVersion(p.getVersion())
                                                a.setProjectDependeny(true)
                                                a.setProjectPath(pci.getProjectPath())

                                                a.addFile(rar.file)
                                            }
                                            if (ci instanceof LibraryBinaryIdentifier) {
                                                LibraryBinaryIdentifier lbi = (LibraryBinaryIdentifier) ci
                                                Project p = project.getRootProject().project(lbi.getProjectPath())

                                                AnalyzedExternalDependency a = artifactMap.get("${p.getGroup()}:${p.getName()}".toString())

                                                if (!a) {
                                                    a = new AnalyzedProjectDependency(p.getGroup().toString(), p.getName(), conf.getName())
                                                    artifactMap.put("${p.getGroup()}:${p.getName()}".toString(), a)
                                                }

                                                a.setVersion(p.getVersion())
                                                a.setLibraryDependency(true)
                                                a.setProjectPath(lbi.getProjectPath())

                                                a.addFile(rar.file)
                                            }
                                            if (ci instanceof ModuleComponentIdentifier) {
                                                ModuleComponentIdentifier mci = (ModuleComponentIdentifier) ci

                                                AnalyzedExternalDependency a = artifactMap.get("${mci.getGroup()}:${mci.getModule()}".toString())
                                                if (!a) {
                                                    a = new AnalyzedExternalDependency(mci.getGroup().toString(), mci.getModule(), conf.getName())
                                                    a.setVersion(mci.getVersion() ?: '')
                                                    artifactMap.put("${mci.getGroup()}:${mci.getModule()}".toString(), a)
                                                }

                                                a.addFile(rar.file)
                                            }
                                            if (ci instanceof OpaqueComponentArtifactIdentifier) {
                                                // should already be handled by first level inspection
                                                // todo: add assert
                                            }

                                        }
                                        analysisTask.setArtifacts(artifactMap.values().asList())
                                    }

                                }
                            })
                        }
                    }
                })


                analysisTask.onlyIf {
                    extension.getEnabled()
                }

                Task checkTask = project.tasks.findByName(JavaBasePlugin.CHECK_TASK_NAME)
                checkTask.dependsOn(analysisTask)
            }
        })

    }

    private static List<AnalyzedExternalDependency> createFirstLevelDependenciesFrom(Dependency dependency, Configuration conf) {
        if (dependency instanceof FileCollectionDependency) {
            def fileCollectionDependency = (FileCollectionDependency) dependency
            return fileCollectionDependency.files.collect { new AnalyzedFileDependency(it, conf.name) }
        } else {
            def moduleDependency = new AnalyzedExternalDependency(dependency.group, dependency.name, conf.getName())
            moduleDependency.setFirstLevel(true)
            moduleDependency.setVersion(dependency.getVersion() ?: '')
            return [moduleDependency]
        }
    }

}
