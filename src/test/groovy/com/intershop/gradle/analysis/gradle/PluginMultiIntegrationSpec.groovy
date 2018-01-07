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
package com.intershop.gradle.analysis.gradle

import com.intershop.gradle.test.AbstractIntegrationSpec

class PluginMultiIntegrationSpec extends AbstractIntegrationSpec {

    def setup() {
        File settingsGradleNew = file('settings.gradle')
        settingsGradleNew << """
            rootProject.name = 'test_multi'
            include 'project1'
            include 'project2'
        """.stripIndent()

        copyResources('test_multi/project1', 'project1')
        copyResources('test_multi/project2', 'project2')
    }

    def 'analyse for multi project'() {
        given:
        buildFile << """
            plugins {
                id 'com.intershop.gradle.dependencyanalysis'
            }


			version = '1.0.0'
			group = 'com.test.gradle'

            subprojects {
                apply plugin: 'java'
                apply plugin: 'com.intershop.gradle.dependencyanalysis'
                
                version = '1.0.0'
                group = 'com.test.gradle'

                repositories {
                    jcenter()
                }
            }
            
            
			repositories {
                jcenter()
            }
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .buildAndFail()

        then:
        new File(testProjectDir, 'project1/build/reports/dependencyAnalysis/dependency-report.html').exists()
        tasksResult.output.contains(':project1:dependencyAnalysis')
    }
}
