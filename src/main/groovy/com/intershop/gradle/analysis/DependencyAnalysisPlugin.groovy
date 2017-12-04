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
import org.gradle.api.Plugin
import org.gradle.api.Project
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
	private DependencyAnalysisExtension extension

    /**
     * Applies this prokugin to the project
     * @param project
     */
    void apply(Project project) {
        //applies the Base reporting pluging
		project.plugins.apply(ReportingBasePlugin)
		
		DependencyAnalysisExtension extension = createExtension(project)

		project.afterEvaluate {
			if(extension.isEnabled()) {
				JavaPluginConvention javaConvention = project.convention.findPlugin(JavaPluginConvention.class)
				if(javaConvention != null) {
                    String sourceSetName = extension.getSourceset()
                    if(! sourceSetName) {
                        sourceSetName = SourceSet.MAIN_SOURCE_SET_NAME
                    }

					SourceSet sourceSet = javaConvention.sourceSets.getByName(sourceSetName)
					Jar jar = project.tasks.findByName(sourceSet.jarTaskName)
				
					project.tasks.getByName('check').dependsOn(project.task(DEPENDENCIESCHECK,
						type: DependencyAnalysisTask,
						group: 'Verification',
						description: 'Determines the usage of existing dependencies') {
						
						base = jar.outputs.files
						classpath = project.configurations['compile']
					})
				} else {
                    project.logger.info('The java plugin convention is not available!')
				}
			}
		}
    }
	
	DependencyAnalysisExtension createExtension(Project project) {
		extension = project.extensions.create(DEPENDENCIESCHECK, DependencyAnalysisExtension)
		extension.reportsDir = project.extensions.getByType(ReportingExtension).file(DEPENDENCIESCHECK)
		return extension
	}
}
