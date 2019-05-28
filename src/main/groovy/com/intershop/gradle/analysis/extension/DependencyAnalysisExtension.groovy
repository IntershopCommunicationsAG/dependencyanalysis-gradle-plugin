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
package com.intershop.gradle.analysis.extension

import com.intershop.gradle.analysis.task.DependencyReporting
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.ClosureBackedAction
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet

/**
 * Implements the extensions of this plugin
 */
@CompileStatic
class DependencyAnalysisExtension {

	/**
	 * Path for the report file
	 */
    private final Property<File> reportsDir

    Provider<File> getReportsDirProvider() {
        return reportsDir
    }

    String getReportsDir() {
        return reportsDir.get()
    }

    void setReportsDir(File reportsDir) {
        this.reportsDir.set(reportsDir)
    }

    /**
     * Fail on duplicate classes
     */
    private final Property<Boolean> failOnDuplicates

    Provider<Boolean> getFailOnDuplicatesProvider() {
        return failOnDuplicates
    }

    boolean getFailOnDuplicates() {
        return failOnDuplicates.get()
    }

    void setFailOnDuplicates(boolean failOnDuplicates) {
        this.failOnDuplicates.set(failOnDuplicates)
    }

    /**
     * Exclude pattern for duplicates
     */
    private final ListProperty<String> excludeDuplicatePatterns

    Provider<List<String>> getExcludeDuplicatePatternsProvider() {
        return excludeDuplicatePatterns
    }

    List<String> getExcludeDuplicatePatterns() {
        excludeDuplicatePatterns.get()
    }

    void setExcludeDuplicatePatterns(List<String> excludeDuplicatePatterns) {
        this.excludeDuplicatePatterns.set(excludeDuplicatePatterns)
    }

    /**
     * Fail on unused first level dependencies
     */
    private final Property<Boolean> failOnUnusedFirstLevelDependencies

    Provider<Boolean> getFailOnUnusedFirstLevelDependenciesProvider() {
        return failOnUnusedFirstLevelDependencies
    }

    boolean getFailOnUnusedFirstLevelDependencies() {
        return failOnUnusedFirstLevelDependencies.get()
    }

    void setFailOnUnusedFirstLevelDependencies(boolean failOnUnusedFirstLevelDependencies) {
        this.failOnUnusedFirstLevelDependencies.set(failOnUnusedFirstLevelDependencies)
    }

    /**
     * Exclude dependencies from analysis of unused dependencies
     */
    private final ListProperty<String> excludeDependencyPatterns

    Provider<List<String>> getExcludeDependencyPatternsProvider() {
        return excludeDependencyPatterns
    }

    List<String> getExcludeDependencyPatterns() {
        excludeDependencyPatterns.get()
    }

    void setExcludeDependencyPatterns(List<String> excludeDependencyPatterns) {
        this.excludeDependencyPatterns.set(excludeDependencyPatterns)
    }

    /**
     * Fail on used transitive dependencies
     */
    private final Property<Boolean> failOnUsedTransitiveDependencies

    Provider<Boolean> getFailOnUsedTransitiveDependenciesProvider() {
        return failOnUsedTransitiveDependencies
    }

    boolean getFailOnUsedTransitiveDependencies() {
        return failOnUsedTransitiveDependencies.get()
    }

    void setFailOnUsedTransitiveDependencies(boolean failOnUsedTransitiveDependencies) {
        this.failOnUsedTransitiveDependencies.set(failOnUsedTransitiveDependencies)
    }

    /**
     * Fail on unused transitive dependencies
     */
    private final Property<Boolean> failOnUnusedTransitiveDependencies

    Provider<Boolean> getFailOnUnusedTransitiveDependenciesProvider() {
        return failOnUnusedTransitiveDependencies
    }

    boolean getFailOnUnusedTransitiveDependencies() {
        return failOnUnusedTransitiveDependencies.get()
    }

    void setFailOnUnusedTransitiveDependencies(boolean failOnUnusedTransitiveDependencies) {
        this.failOnUnusedTransitiveDependencies.set(failOnUnusedTransitiveDependencies)
    }

    /**
     * Inspection can be disabled for an project
     */
    private final Property<Boolean> enabled

    Provider<Boolean> getEnabledProvider() {
        return enabled
    }

    Boolean getEnabled() {
        return enabled.get()
    }

    void setEnabled(Boolean enabled) {
        this.enabled.set(enabled)
    }

    /**
     * Source set name
     */
    private final Property<String> sourceset

    Provider<String> getSourcesetProvider() {
        return sourceset
    }

    String getSourceset() {
        return sourceset.getOrElse(SourceSet.MAIN_SOURCE_SET_NAME)
    }

    void setSourceset(String sourceset) {
        this.sourceset.set(sourceset)
    }

    List<Action<DependencyReporting>> dependencyReportings = []

    void dependencyReporting(Action<DependencyReporting> action) {
        this.dependencyReportings.add(action)
    }

    void dependencyReporting(Closure<DependencyReporting> closure) {
        dependencyReporting(new ClosureBackedAction<DependencyReporting>(closure))
    }

    DependencyAnalysisExtension(Project project) {
        reportsDir = project.objects.property(File)

        excludeDuplicatePatterns = project.objects.listProperty(String)
        excludeDependencyPatterns = project.objects.listProperty(String)

        failOnDuplicates = project.objects.property(Boolean)
        failOnUnusedFirstLevelDependencies = project.objects.property(Boolean)
        failOnUsedTransitiveDependencies = project.objects.property(Boolean)
        failOnUnusedTransitiveDependencies = project.objects.property(Boolean)

        enabled = project.objects.property(Boolean)

        sourceset = project.objects.property(String)

        setFailOnDuplicates(true)
        setFailOnUnusedFirstLevelDependencies(true)
        setFailOnUsedTransitiveDependencies(true)

        setFailOnUnusedTransitiveDependencies(false)

        setEnabled(true)
    }
}
