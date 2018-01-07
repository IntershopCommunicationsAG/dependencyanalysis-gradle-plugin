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

class PluginExtIntegrationSpec extends AbstractIntegrationSpec {

    def setup() {
        File settingsGradleNew = file('settings.gradle')
        settingsGradleNew << """
            rootProject.name = 'test_int'
        """.stripIndent()

        copyResources('test_int/src', 'src')
    }

    def 'analyse for simple project with spring boot'() {
        given:
        buildFile << """
			buildscript {
				repositories {
					maven { url 'https://plugins.gradle.org/m2' }
					maven { url 'http://repo.spring.io/plugins-release' }
					jcenter()
				}
				dependencies {
					classpath 'org.springframework.boot:spring-boot-gradle-plugin:2.0.0.M7'
				}
			}

            plugins {
                id 'java'
                id 'io.spring.dependency-management' version '1.0.4.RELEASE'
                id 'com.intershop.gradle.dependencyanalysis'
            }


			version = '1.0.0.0'
			group = 'com.test.gradle'

			sourceCompatibility = 1.8
			targetCompatibility = 1.8

            dependencyManagement {
                imports { mavenBom 'io.spring.platform:platform-bom:2.0.8.RELEASE' }
            }

			repositories {
                maven { url 'http://repo.spring.io/libs-milestone' }
                maven { url 'http://repo.spring.io/release' }
                jcenter()
            }

			dependencies {
                implementation 'log4j:log4j'
			}
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .build()

        then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()
        tasksResult.output.contains(':dependencyAnalysis')
    }
}
