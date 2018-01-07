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
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
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
     * excludeDublicatePatterns to exclude special classes - necessary for special interfaces
     */

    final ListProperty<String> excludeDuplicatePatterns = project.objects.listProperty(String)

    @Optional
    @Input
    List<String> getExcludeDuplicatePatterns() {
        return excludeDuplicatePatterns.getOrElse([])
    }

    void setExcludeDuplicatePatterns(List<String> excludeDuplicatePatterns) {
        this.excludeDuplicatePatterns.set(excludeDuplicatePatterns)
    }

    void setExcludeDuplicatePatterns(Provider<List<String>> excludeDuplicatePatterns) {
        this.excludeDuplicatePatterns.set(excludeDuplicatePatterns)
    }

    /**
     * excludeDublicatePatterns to exclude special classes - necessary for special interfaces
     */

    final ListProperty<String> excludeDependencyPatterns = project.objects.listProperty(String)

    @Optional
    @Input
    List<String> getExcludeDependencyPatterns() {
        return excludeDependencyPatterns.getOrElse([])
    }

    void setExcludeDependencyPatterns(List<String> excludeDependencyPatterns) {
        this.excludeDependencyPatterns.set(excludeDependencyPatterns)
    }

    void setExcludeDependencyPatterns(Provider<List<String>> excludeDependencyPatterns) {
        this.excludeDependencyPatterns.set(excludeDependencyPatterns)
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
     * List of Artifacts
     */
    final ListProperty<Artifact> artifacts = project.objects.listProperty(Artifact)

    void setArtifacts(List<Artifact> newArtifacts) {
        this.artifacts.set(newArtifacts)
    }

    void setArtifacts(ListProperty<Artifact> artifacts) {
        this.artifacts.set(artifacts)
    }

    List<Artifact> getArtifacts() {
        return this.artifacts.get()
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

        getArtifacts().findAll({it.firstLevel}).each { Artifact a ->
            artifacts.each {
                Collection<String> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                cleanedList(il, getExcludeDuplicatePatterns())

                if(il.size() > 0) {
                    it.duplicatedClasses.addAll(il)
                    a.duplicatedClasses.addAll(il)
                    it.duplicatedArtifacts.add(a)
                    a.duplicatedArtifacts.add(it)
                }
            }
            artifacts.add(a)
        }

        getArtifacts().findAll({! it.firstLevel}).each { Artifact a ->

                artifacts.each {
                    Collection<String> il = CollectionUtils.intersection(it.containedClasses, a.containedClasses)
                    cleanedList(il, getExcludeDuplicatePatterns())

                    if(il.size() > 0) {
                        it.duplicatedClasses.addAll(il)
                        a.duplicatedClasses.addAll(il)
                        it.duplicatedArtifacts.add(a)
                        a.duplicatedArtifacts.add(it)
                    }
                }

            artifacts.add(a)
		}
		
		getBase().getFiles().each {File f ->
            ProjectArtifact pa = new ProjectArtifact(project.getGroup().toString(), project.getName())
            pa.addFile(f)
            pa.setVersion(project.getVersion().toString())
			projectArtifacts.add(pa)
		}
		
		projectArtifacts.each { Object pa ->
            ((ProjectArtifact)pa).getAllDependencyClasses().each {String classname ->
                artifacts.findAll {it.containedClasses.contains(classname)}.each {
                    it.usedClasses.add(classname)
                    project.logger.debug("Add used for ${classname} to ${it}")
                }
			}
		}

        artifacts.each { Artifact a ->
            configureIgnore(a, getExcludeDependencyPatterns(), getExcludeDuplicatePatterns())
        }

        Set<Artifact> duplicates = artifacts.findAll{ it.usedClasses.size() > 0 && it.duplicatedArtifacts.size() > 0 }
        Set<Artifact> excludedDuplicates = artifacts.findAll{ it.excludedDuplicatedClasses.size() > 0 && it.excludedDuplicatedArtifacts.size() > 0 }
        Set<Artifact> used = artifacts.findAll{ it.usedClasses.size() > 0 && it.firstLevel && it.duplicatedArtifacts.size() == 0 }
        Set<Artifact> unused = artifacts.findAll{ it.usedClasses.size() == 0 && it.firstLevel }
        Set<Artifact> usedTransitive = artifacts.findAll{ it.usedClasses.size() > 0 && ! it.firstLevel }
        Set<Artifact> unusedTranstive = artifacts.findAll{ it.usedClasses.size() < 1 && ! it.firstLevel }

        HTMLReporter reporter = new HTMLReporter(used, unused, usedTransitive, unusedTranstive, duplicates, excludedDuplicates, projectArtifacts)
        reporter.createReport(getHtmlReport(), project.name, project.version.toString())

        String output = ''

        if( duplicates.findAll({! it.ignoreForAnalysis}).size() > 0) {
            output += "  There are dublicate classes in used dependencies (${duplicates.size() / 2})" + '\n'
        }
        if( unused.findAll({! it.ignoreForAnalysis}).size() > 0) {
            output += "  There are unused dependencies (${unused.size()})" + '\n'
        }
        if(usedTransitive.findAll({! it.ignoreForAnalysis}).size() > 0) {
            output += "  There are used transitive dependencies (${usedTransitive.size()})" + '\n'
        }

        boolean fail = false

        fail = fail || (duplicates.findAll({! it.ignoreForAnalysis}).size() > 0 && getFailOnDuplicates())
        fail = fail || (unused.findAll({! it.ignoreForAnalysis}).size() > 0 && getFailOnUnusedFirstLevelDependencies())
        fail = fail || (usedTransitive.findAll({! it.ignoreForAnalysis}).size() > 0 && getFailOnUsedTransitiveDependencies())
        fail = fail || (unusedTranstive.findAll({! it.ignoreForAnalysis}).size() > 0 && getFailOnUnusedTransitiveDependencies())

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

    static void cleanedList(Collection<String> input, List<String> exclude) {
        exclude.each {String excludePattern ->
            input.removeAll {
                it.matches(excludePattern)
            }
        }
    }

    static void configureIgnore(Artifact a, List<String> excludeDeps, List<String> excludeDuplicates) {
        excludeDeps.each {
            if(a.getName().matches(it)) {
                a.setIgnoreForAnalysis(true)
            }
        }
        excludeDuplicates.each { String excludeDup ->
            if(a.getDuplicatedClasses().size() > 0) {
                a.getExcludedDuplicatedClasses().addAll(a.getDuplicatedClasses().findAll {it.matches(excludeDup)})
                a.getDuplicatedClasses().removeAll(a.getExcludedDuplicatedClasses())
            }
            if(a.getDuplicatedClasses().size() == 0) {
                a.getExcludedDuplicatedArtifacts().addAll(a.duplicatedArtifacts)
                a.duplicatedArtifacts = [] as Set<Artifact>
            }
        }
    }
}
