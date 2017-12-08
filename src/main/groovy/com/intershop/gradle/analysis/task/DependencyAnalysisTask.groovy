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

import com.intershop.gradle.analysis.extension.DependencyAnalysisExtension
import com.intershop.gradle.analysis.model.Artifact
import com.intershop.gradle.analysis.model.ProjectArtifact
import com.intershop.gradle.analysis.reporters.HTMLReporter
import groovy.transform.CompileStatic
import org.apache.commons.collections4.CollectionUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

/**
 * This is task implementation of this task.
 */
@CompileStatic
class DependencyAnalysisTask extends DefaultTask {

	static final String REPORT_NAME = 'dependency-report'

    /**
     * Fail on errors
     */
    final Property<Boolean> failOnErrors = project.objects.property(Boolean)

    @Input
    boolean getFailOnErrors() {
        return failOnErrors.get()
    }

    void setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors.set(failOnErrors)
    }

    void setFailOnErrors(Provider<Boolean> failOnErrors) {
        this.failOnErrors.set(failOnErrors)
    }

    /**
     * Fail on warnings
     */
    final Property<Boolean> failOnWarnings = project.objects.property(Boolean)

    @Input
    boolean getFailOnWarnings() {
        return failOnWarnings.get()
    }

    void setFailOnWarnings(Boolean failOnWarnings) {
        this.failOnWarnings.set(failOnWarnings)
    }

    void setFailOnWarnings(Provider<Boolean> failOnWarnings) {
        this.failOnWarnings.set(failOnWarnings)
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
        Set<ResolvedDependency> dependencies = []
        Configuration compile = project.getConfigurations().findByName('compile')
        if(compile) {
            dependencies.addAll(compile.getResolvedConfiguration().getFirstLevelModuleDependencies())
        }
        Configuration api = project.getConfigurations().findByName('api')
        if(api) {
            dependencies.addAll(api.getResolvedConfiguration().getFirstLevelModuleDependencies())
        }

        dependencies.each { ResolvedDependency rd ->
            rd.getModuleArtifacts().each {
                Artifact a = new Artifact(it.getFile().getAbsoluteFile(),
                        it.getModuleVersion().getId().getModule().toString(),
                        it.getModuleVersion().getId().getVersion())
                artifacts.add(a)
            }
        }

        return artifacts
    }

    @Input
    Set<Artifact> getResolvedArtifacts() {
        Set<Artifact> artifacts = new HashSet()
        Set<ResolvedArtifact> resolvedArtifacts = []

        Configuration compile = project.getConfigurations().findByName('compile')
        if(compile) {
            resolvedArtifacts.addAll(compile.getResolvedConfiguration().getResolvedArtifacts())
        }
        Configuration api = project.getConfigurations().findByName('api')
        if(api) {
            resolvedArtifacts.addAll(api.getResolvedConfiguration().getResolvedArtifacts())
        }

        resolvedArtifacts.each {ResolvedArtifact ra ->
            Artifact a = new Artifact(ra.getFile().getAbsoluteFile(),
                    ra.getModuleVersion().getId().getModule().toString(),
                    ra.getModuleVersion().getId().getVersion())
            artifacts.add(a)
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

    /**
     * Task action
     * Creates a report of all used and unused dependencies.
     */
	@TaskAction
	void analyze() {
		Set<Artifact> artifacts = new HashSet()
		def projectArtifacts = []

        getFirstLevelArtifacts().each {Artifact a ->
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

        Set<Artifact> unused = artifacts.findAll{ it.usedClasses.size() == 0 }
        Set<Artifact> usedTransitive = artifacts.findAll{ it.usedClasses.size() > 0 && it.getTransitive() > 1 }
        Set<Artifact> duplicates = artifacts.findAll{ it.dublicatedArtifacts.size() > 0 }
        Set<Artifact> duplicateUsed = artifacts.findAll { it.usedClasses.size() > 0 &&  it.dublicatedArtifacts.size() > 0 }

        int errors = duplicateUsed.size() + unused.findAll { it.transitive == 0 }.size()
        int warnings = unused.findAll { it.transitive > 0 }.size() + usedTransitive.size()

		if((getFailOnErrors() && errors > 0) || (getFailOnWarnings() && warnings > 0)) {
			throw new GradleException("""
                == Dependency Report ==

                  Warnings:   ${warnings}
                  Errors:     ${errors}
                There are several dependency issues.
                Please check ${htmlReport.canonicalPath} for more information

                """.stripIndent(12))
		}
		
		if((errors > 0 || warnings > 0) && ! getFailOnErrors() && ! getFailOnWarnings()) {

            String duplicatesStr = ''
            duplicates.each {
                "       ${it} (${it.getAbsoluteFile()})" + '/n'
            }
            duplicatesStr = duplicatesStr ?: 'no duplicate dependencies'
            String report = """
                -----------------------------------------------
                  Dependency Report
                -----------------------------------------------

                     Warnings: ${warnings}
                     Errors:   ${errors}

                     Duplicate Dependencies
                     ------------------------
                     ${duplicatesStr}

                ---------------------------------------------------------------------------------
                  Please review
                    ${htmlReport.canonicalPath}
                  for more information
                ---------------------------------------------------------------------------------

            """.stripIndent()

            println report
		}
	}
	

}
