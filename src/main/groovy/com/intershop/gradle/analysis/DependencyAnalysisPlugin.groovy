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
import com.intershop.gradle.analysis.task.DependencyAnalysisTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

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

        DependencyAnalysisExtension extension = project.extensions.findByType(DependencyAnalysisExtension) ?:   project.extensions.create(DEPENDENCIESCHECK, DependencyAnalysisExtension, project)

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

                analysisTask.setHtmlReportDir(extension.getReportsDirProvider())

                analysisTask.onlyIf {
                    extension.getEnabled()
                }

                Task checkTask = project.tasks.findByName(JavaBasePlugin.CHECK_TASK_NAME)
                checkTask.dependsOn(analysisTask)
            }
        })
    }
}
