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
package com.intershop.gradle.analysis.task

import com.intershop.gradle.analysis.model.Artifact
import com.intershop.gradle.analysis.model.ProjectArtifact
import com.intershop.gradle.analysis.reporters.HTMLReporter
import groovy.transform.CompileStatic
import org.apache.commons.collections4.CollectionUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * This is task implementation of this task.
 */
@CompileStatic
class DependencyAnalysisTask extends DefaultTask {

	static final String REPORT_NAME = 'dependency-report'

    /**
     * Fail on dublicate classes
     */
    final Property<Boolean> failOnDuplicates = project.objects.property(Boolean)

    @Optional
    @Input
    boolean getFailOnDuplicates() {
        return failOnDuplicates.get()
    }

    void setFailOnDuplicates(boolean failOnDuplicateClasses) {
        this.failOnDuplicates.set(failOnDuplicateClasses)
    }

    void setFailOnDuplicates(Provider<Boolean> failOnDuplicates) {
        this.failOnDuplicates.set(failOnDuplicates)
    }

    /**
     * Fail on unused first level dependencies
     */
    final Property<Boolean> failOnUnusedFirstLevelDependencies = project.objects.property(Boolean)

    @Optional
    @Input
    boolean getFailOnUnusedFirstLevelDependencies() {
        return failOnUnusedFirstLevelDependencies.get()
    }

    void setFailOnUnusedFirstLevelDependencies(boolean failOnUnusedFirstLevelDependencies) {
        this.failOnUnusedFirstLevelDependencies.set(failOnUnusedFirstLevelDependencies)
    }

    void setFailOnUnusedFirstLevelDependencies(Provider<Boolean> failOnUnusedFirstLevelDependencies) {
        this.failOnUnusedFirstLevelDependencies.set(failOnUnusedFirstLevelDependencies)
    }

    /**
     * Fail on used transitive dependencies
     */
    final Property<Boolean> failOnUsedTransitiveDependencies = project.objects.property(Boolean)

    @Optional
    @Input
    boolean getFailOnUsedTransitiveDependencies() {
        return failOnUsedTransitiveDependencies.get()
    }

    void setFailOnUsedTransitiveDependencies(boolean failOnUsedTransitiveDependencies) {
        this.failOnUsedTransitiveDependencies.set(failOnUsedTransitiveDependencies)
    }

    void setFailOnUsedTransitiveDependencies(Provider<Boolean> failOnUsedTransitiveDependencies) {
        this.failOnUsedTransitiveDependencies.set(failOnUsedTransitiveDependencies)
    }

    /**
     * Fail on unused transitive dependencies
     */
    final Property<Boolean> failOnUnusedTransitiveDependencies = project.objects.property(Boolean)

    @Optional
    @Input
    boolean getFailOnUnusedTransitiveDependencies() {
        return failOnUnusedTransitiveDependencies.get()
    }

    void setFailOnUnusedTransitiveDependencies(boolean failOnUnusedTransitiveDependencies) {
        this.failOnUnusedTransitiveDependencies.set(failOnUnusedTransitiveDependencies)
    }

    void setFailOnUnusedTransitiveDependencies(Provider<Boolean> failOnUnusedTransitiveDependencies) {
        this.failOnUnusedTransitiveDependencies.set(failOnUnusedTransitiveDependencies)
    }

    /**
     * Report file
     */
    final Property<File> htmlReportDir = project.objects.property(File)

    void setHtmlReportDir(File htmlReport) {
        this.htmlReportDir.set(htmlReport)
    }

    void setHtmlReportDir(Provider<File> htmlReport) {
        this.htmlReportDir.set(htmlReport)
    }

    @OutputFile
    File getHtmlReport() {
        return new File(htmlReportDir.get(), "${REPORT_NAME}.html")
    }

    /**
     * Resolved dependencies
     */
    @Input
    Set<Artifact> getFirstLevelArtifacts() {
        Set<Artifact> artifacts = new HashSet()

        Configuration compile = project.getConfigurations().findByName('compile')
        if(compile) {
            compile.getResolvedConfiguration().getFirstLevelModuleDependencies().each { ResolvedDependency rd ->
                rd.getModuleArtifacts().each {
                    Artifact a = new Artifact(it.getFile().getAbsoluteFile(),
                            it.getModuleVersion().getId().getModule().toString(),
                            it.getModuleVersion().getId().getVersion(),
                            'compile')
                    artifacts.add(a)
                }
            }
        }

        return artifacts
    }

    @Input
    Set<Artifact> getJavaLibraryDependencies() {
        Set<Artifact> artifacts = new HashSet()

        Configuration api = project.getConfigurations().findByName('api')
        Configuration implementation = project.getConfigurations().findByName('implementation')

        if(api) {
            api.dependencies.each { Dependency dep ->
                Artifact a = new Artifact("${dep.group}:${dep.name}".toString(),
                        dep.getVersion(),
                        'api')
                artifacts.add(a)
            }
        }
        if(implementation) {
            implementation.dependencies.each { Dependency dep ->
                Artifact a = new Artifact("${dep.group}:${dep.name}".toString(),
                        dep.getVersion(),
                        'implementation')
                artifacts.add(a)
            }
        }

        return artifacts
    }

    @Input
    Set<Artifact> getResolvedArtifacts() {
        Set<Artifact> artifacts = new HashSet()

        Configuration compile = project.getConfigurations().findByName('compile')
        if(compile) {
            compile.getResolvedConfiguration().getResolvedArtifacts().each { ResolvedArtifact ra ->
                Artifact a = new Artifact(ra.getFile().getAbsoluteFile(),
                        ra.getModuleVersion().getId().getModule().toString(),
                        ra.getModuleVersion().getId().getVersion(), 'compile')
                artifacts.add(a)
            }
        }
        return artifacts
    }

    /**
     * This files will be analyzed.
     */
    final Property<FileCollection> base = project.objects.property(FileCollection)

    @InputFiles
    FileCollection getBase() {
        return base.get()
    }

    void setBase(FileCollection base) {
        this.base.set(base)
    }

    void setBase(Provider<FileCollection> base) {
        this.base.set(base)
    }

    DependencyAnalysisTask() {
        setFailOnDuplicates(true)
        setFailOnUnusedFirstLevelDependencies(true)
        setFailOnUsedTransitiveDependencies(true)
        setFailOnUnusedTransitiveDependencies(false)
    }

    /**
     * Task action
     * Creates a report of all used and unused dependencies.
     */
	@TaskAction
	void analyze() {
		Set<Artifact> artifacts = new HashSet()
		def projectArtifacts = []

        Set<Artifact> libArtifacts = getJavaLibraryDependencies()
        libArtifacts.each {Artifact a ->
            addFile(a)
        }

        libArtifacts.each {Artifact a ->
            artifacts.each {
                Collection<String> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                if(il.size() > 0) {
                    it.dublicatedClasses.addAll(il)
                    a.dublicatedClasses.addAll(il)
                    it.dublicatedArtifacts.add(a)
                    a.dublicatedArtifacts.add(it)
                }
            }
            artifacts.add(a)
        }

        getFirstLevelArtifacts().each { Artifact a ->
            artifacts.each {
                Collection<String> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                if(il.size() > 0) {
                    it.dublicatedClasses.addAll(il)
                    a.dublicatedClasses.addAll(il)
                    it.dublicatedArtifacts.add(a)
                    a.dublicatedArtifacts.add(it)
                }
            }
            artifacts.add(a)
        }

        resolvedArtifacts.each { Artifact a ->
            if(artifacts.contains(a)) {
                a.setTransitive(1)
            } else {
                artifacts.each {
                    Collection<String> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                    if(il.size() > 0) {
                        it.dublicatedClasses.addAll(il)
                        a.dublicatedClasses.addAll(il)
                        it.dublicatedArtifacts.add(a)
                        a.dublicatedArtifacts.add(it)
                    }
                }
                a.setTransitive(2)
            }
            artifacts.add(a)
		}
		
		getBase().getFiles().each {File f ->
			projectArtifacts.add(new ProjectArtifact(f, project.getGroup().toString(), project.getVersion().toString(), false))
		}
		
		projectArtifacts.each { Object pa ->
            ((ProjectArtifact)pa).getAllDependencyClasses().each {String classname ->
                artifacts.findAll {it.containedClasses.contains(classname)}.each {
                    it.usedClasses.add(classname)
                    project.logger.debug("Add used for ${classname} to ${it}")
                }
			}
		}
		
		HTMLReporter reporter = new HTMLReporter(artifacts, projectArtifacts)
		reporter.createReport(getHtmlReport(), project.name, project.version.toString())

        Set<Artifact> duplicates = artifacts.findAll{ it.dublicatedClasses.size() > 0 || it.dublicatedArtifacts.size() > 0 }
        Set<Artifact> unused = artifacts.findAll{ it.usedClasses.size() == 0 && it.transitive == 0 }
        Set<Artifact> usedTransitive = artifacts.findAll{ it.usedClasses.size() > 0 && it.getTransitive() > 0 }
        Set<Artifact> unusedTranstive = artifacts.findAll{ it.usedClasses.size() < 1 && it.getTransitive() > 0 }

        String output = ''

        if( duplicates.size() > 0) {
            output += "  There are dublicates (${duplicates.size()})" + '\n'
        }
        if( unused.size() > 0) {
            output += "  There are unused dependencies (${unused.size()})" + '\n'
        }
        if(usedTransitive.size() > 0) {
            output += "  There are used transitive dependencies (${usedTransitive.size()})" + '\n'
        }

        boolean fail = false

        fail = fail || (duplicates.size() > 0 && getFailOnDuplicates())
        fail = fail || (unused.size() > 0 && getFailOnUnusedFirstLevelDependencies())
        fail = fail || (usedTransitive.size() > 0 && getFailOnUsedTransitiveDependencies())
        fail = fail || (unusedTranstive.size() > 0 && getFailOnUnusedTransitiveDependencies())

        if(fail) {
            throw new GradleException("""
                There are several dependency issues.
                Please check ${htmlReport.canonicalPath} for more information

                """.stripIndent(16))
        } else {
            if(output) {
                println "-- Dependency Analysis" + '\n' + output
            }
        }
	}

    void addFile(Artifact artifact) {
        Configuration config = project.configurations.detachedConfiguration(project.dependencies.create(artifact.toString())).setTransitive(false)
        config.resolvedConfiguration.getResolvedArtifacts().each { ResolvedArtifact ra ->
            if(ra.getFile().getName().endsWith('.jar')) {
                artifact.setAbsoluteFile(ra.getFile())
            }
        }
    }
	

}
