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
import org.apache.commons.collections.CollectionUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.logging.StyledTextOutput
import org.gradle.logging.StyledTextOutputFactory
/**
 * This is task implementation of this task.
 */
class DependencyAnalysisTask extends DefaultTask {

	public static final String REPORT_NAME = 'dependency-report'

    /**
     * Classpath of the project
     */
	@InputFiles
	Configuration classpath

    /**
     * This files will be analyzed.
     */
	@InputFiles
	FileCollection base

    /**
     * Report file
     */
	@OutputFile
	File htmlReport

    /**
     * Constructs tis task
     */
	DependencyAnalysisTask() {
		File reportDir = new File("${project.dependencyAnalysis.reportsDir}")
		htmlReport = new File(reportDir, "${REPORT_NAME}.html")
	}

    /**
     * Task action
     * Creates a report of all used and unused dependencies.
     */
	@TaskAction
	void analyze() {
		Set<Artifact> artifacts = new HashSet()
		Map<String, Set<Artifact>> classfiles = [:]
		def projectArtifacts = []
				
		getClasspath().getResolvedConfiguration().getFirstLevelModuleDependencies().each {ResolvedDependency rd ->
			rd.getModuleArtifacts().each {	
				Artifact a = new Artifact(it.getFile().getAbsoluteFile(),
				        it.getModuleVersion().getId().getModule().toString(),
                        it.getModuleVersion().getId().getVersion())
                artifacts.each {
                    List<Artifact> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                    if(il.size() > 0) {
                        it.dublicatedClasses.addAll(il)
                        a.dublicatedClasses.addAll(il)
                        it.dublicatedArtifacts.add(a)
                        a.dublicatedArtifacts.add(it)
                    }
                }
                artifacts.add(a)
			}
		}

		getClasspath().getResolvedConfiguration().getResolvedArtifacts().each {
            Artifact a = new Artifact(it.getFile().getAbsoluteFile(),
                    it.getModuleVersion().getId().getModule().toString(),
                    it.getModuleVersion().getId().getVersion())
            if(artifacts.contains(a)) {
                a.setTransitive(1)
            } else {
                artifacts.each {
                    List<Artifact> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                    if(il.size() > 0) {
                        it.dublicatedClasses.addAll(il)
                        a.dublicatedClasses.addAll(il)
                        it.dublicatedArtifacts.add(a)
                        a.dublicatedArtifacts.add(it)
                    }
                }
                a.setTransitive(2)
                artifacts.add(a)
            }
		}
		
		getBase().getFiles().each {File f ->
			projectArtifacts.add(new ProjectArtifact(f, project.getModule().toString(), project.getVersion(), false))
		}
		
		projectArtifacts.each { ProjectArtifact pa ->
            pa.getAllDependencyClasses().each {String classname ->
                artifacts.findAll {it.containedClasses.contains(classname)}.each {
                    it.usedClasses.add(classname)
                    project.logger.debug("Add used for ${classname} to ${it}")
                }
			}
		}
		
		HTMLReporter reporter = new HTMLReporter(artifacts, projectArtifacts)
		reporter.createReport(getHtmlReport(), project.name, project.version)

        Set<Artifact> unused = artifacts.findAll{ it.usedClasses.size() == 0 }
        Set<Artifact> usedTransitive = artifacts.findAll{ it.usedClasses.size() > 0 && it.getTransitive() > 1 }
        Set<Artifact> duplicates = artifacts.findAll{ it.dublicatedArtifacts.size() > 0 }
        Set<Artifact> duplicateUsed = artifacts.findAll { it.usedClasses.size() > 0 &&  it.dublicatedArtifacts.size() > 0 }

        int errors = duplicateUsed.size() + unused.findAll { it.transitive == 0 }.size()
        int warnings = unused.findAll { it.transitive > 0 }.size() + usedTransitive.size()

		if((project.dependencyAnalysis.getFailOnErrors() && errors > 0) || (project.dependencyAnalysis.getFailOnWarnings() && warnings > 0)) {
			throw new GradleException("""
                == Dependency Report ==

                  Warnings:   ${warnings}
                  Errors:     ${errors}
                There are several dependency issues.
                Please check ${htmlReport.canonicalPath} for more information

                """.stripIndent(12))
		}
		
		if((errors > 0 || warnings > 0) && ! project.dependencyAnalysis.getFailOnErrors() && ! project.dependencyAnalysis.getFailOnWarnings()) {
            StyledTextOutput output = services.get(StyledTextOutputFactory).create('API Check Report')
            output.println()
            output.withStyle(org.gradle.logging.StyledTextOutput.Style.Info).println('== Dependency Report ==')
            output.println("  Warnings: ${warnings}")
            output.println("  Errors:   ${errors}")
            if(duplicates.size() > 0) {
                output.println()
                output.println("Duplicate Dependencies")
                duplicates.each {
                    output.println("  ${it} (${it.getAbsoluteFile()})")
                }
                output.println()
            }
            output.withStyle(org.gradle.logging.StyledTextOutput.Style.Description).println("Please review ${htmlReport.canonicalPath} for more information")
            output.println()
		}
	}
	

}
