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

import com.intershop.gradle.analysis.model.ProjectArtifact
import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.provider.Property
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
     * Fail on error
     */
    private final Property<Boolean> failOnErrors

    Provider<Boolean> getFailOnErrorsProvider() {
        return failOnErrors
    }

    boolean getFailOnErrors() {
        return failOnErrors.get()
    }

    void setFailOnErrors(boolean failOnErrors) {
        this.failOnErrors.set(failOnErrors)
    }

    /**
     * Fail on warnings
     */
    private final Property<Boolean> failOnWarnings

    Provider<Boolean> getFailOnWarningsProvider() {
        return failOnWarnings
    }

    Boolean getFailOnWarnings() {
        return failOnWarnings.get()
    }

    void setFailOnWarnings(Boolean failOnWarnings) {
        this.failOnWarnings.set(failOnWarnings)
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

    DependencyAnalysisExtension(Project project) {
        reportsDir = project.objects.property(File)

        failOnErrors = project.objects.property(Boolean)
        failOnWarnings = project.objects.property(Boolean)
        enabled = project.objects.property(Boolean)

        sourceset = project.objects.property(String)

        setFailOnErrors(true)
        setFailOnWarnings(false)
        setEnabled(true)
    }
}
