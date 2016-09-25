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
import spock.lang.Unroll

class DependencyAnalysisPluginSpec extends AbstractIntegrationSpec {

    def setup() {
        File settingsGradleNew = file('settings.gradle')
        settingsGradleNew << """
            rootProject.name = 'test_new'
        """.stripIndent()

        copyResources('test_new/src', 'src')
    }

    @Unroll
	def "analyse for project without issues"() {
		given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.dependencyanalysis'
            }

			version = '1.0.0.0'
			group = 'com.test.gradle'

			sourceCompatibility = 1.8
			targetCompatibility = 1.8

			repositories {
                jcenter()
            }

			dependencies {
				compile 'com.google.code.findbugs:annotations:3.0.0'
				compile 'javax.persistence:persistence-api:1.0.2'
				compile 'javax.validation:validation-api:1.0.0.GA'
				compile 'org.slf4j:slf4j-api:1.7.21'
				compile 'commons-logging:commons-logging:1.1.1'

				compile 'org.ow2.asm:asm:5.1'
				compile 'junit:junit:4.12'
				compile('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}
			}
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .build()

		then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        ! tasksResult.output.contains('Warnings:')
        ! tasksResult.output.contains('Errors:')
        ! tasksResult.output.contains('Warnings:')
        ! tasksResult.output.contains('Errors:')
	}

	@Unroll
	def "analyse for project not failOnErrors"() {
		given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.dependencyanalysis'
            }

			version = '1.0.0'
			group = 'com.test.gradle'

			sourceCompatibility = 1.8
			targetCompatibility = 1.8

            repositories {
                jcenter()
            }

            dependencyAnalysis {
                failOnErrors = false
            }

			dependencies {
				compile 'com.google.code.findbugs:annotations:3.0.0'
				compile 'javax.persistence:persistence-api:1.0.2'
				compile 'javax.validation:validation-api:1.0.0.GA'
				compile 'org.slf4j:slf4j-api:1.7.21'
				compile 'junit:junit:4.12'
				compile 'commons-logging:commons-logging:1.1.1'
				compile('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}

				//not necessary for compilation
				compile 'net.sf.ehcache:ehcache-core:2.6.11'

				// duplicate classes
				compile 'org.ow2.asm:asm-all:4.2'
				compile 'org.ow2.asm:asm:5.1'
			}
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .build()

        then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        tasksResult.output.contains('Warnings: 1')
        tasksResult.output.contains('Errors:   3')
	}

    @Unroll
    def "analyse for project not failOnErrors with duplicates"() {
        given:
        (new File(testProjectDir, 'src/main/java/com/test/api/checker/tests/ASMHelloWorld.java')).delete()

        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.dependencyanalysis'
            }

			version = '1.0.0'
			group = 'com.test.gradle'

			sourceCompatibility = 1.8
			targetCompatibility = 1.8

            repositories {
                jcenter()
            }

            dependencyAnalysis {
                failOnErrors = false
            }

			dependencies {
				compile 'com.google.code.findbugs:annotations:3.0.0'
				compile 'javax.persistence:persistence-api:1.0.2'
				compile 'javax.validation:validation-api:1.0.0.GA'
				compile 'org.slf4j:slf4j-api:1.7.21'
				compile 'junit:junit:4.12'
				compile('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}

				//not necessary for compilation
				compile 'net.sf.ehcache:ehcache-core:2.6.11'
				compile 'org.springframework:spring-web:4.1.6.RELEASE'

				// duplicate classes
				compile 'org.ow2.asm:asm-all:4.2'
				compile 'org.ow2.asm:asm:5.1'
			}
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .build()

        then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        tasksResult.output.contains('Warnings: 8')
        tasksResult.output.contains('Errors:   4')
        tasksResult.output.contains('Duplicate Dependencies')
    }

	@Unroll
	def "analyse for project failOnErrors"() {
		given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.dependencyanalysis'
            }

			version = '1.0.0'
			group = 'com.test.gradle'

			sourceCompatibility = 1.8
			targetCompatibility = 1.8

            repositories {
                jcenter()
            }

			dependencies {
				compile 'com.google.code.findbugs:annotations:3.0.0'
				compile 'javax.persistence:persistence-api:1.0.2'
				compile 'javax.validation:validation-api:1.0.0.GA'
				compile 'org.ow2.asm:asm:5.1'
				compile 'org.slf4j:slf4j-api:1.7.21'
				compile 'junit:junit:4.12'
				compile('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}

				//not necessary for compilation
				compile 'net.sf.ehcache:ehcache-core:2.6.11'
				compile 'org.springframework:spring-web:4.1.6.RELEASE'

                // double classes
                compile 'org.ow2.asm:asm-all:4.2'
			}
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .buildAndFail()

        then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        tasksResult.output.contains('Warnings:   8')
        tasksResult.output.contains('Errors:     4')
	}

	@Unroll
	def "analyse for project disabled"() {
		given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.dependencyanalysis'
            }

			version = '1.0.0.0'
			group = 'com.test.gradle'

			sourceCompatibility = 1.8
			targetCompatibility = 1.8

			dependencyAnalysis {
				enabled = false
			}

            repositories {
                jcenter()
            }

			dependencies {
				compile 'com.google.code.findbugs:annotations:3.0.0'
				compile 'javax.persistence:persistence-api:1.0.2'
				compile 'javax.validation:validation-api:1.0.0.GA'
				compile 'org.slf4j:slf4j-api:1.7.21'
				compile 'org.ow2.asm:asm:5.1'
				compile 'junit:junit:4.12'
				compile('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}

				//not necessary for compilation
				compile 'net.sf.ehcache:ehcache-core:2.6.11'
				compile 'org.springframework:spring-web:4.1.6.RELEASE'

                // double classes
                compile 'org.ow2.asm:asm-all:4.2'
			}
		""".stripIndent()

        when:
        List<String> tasksArgs = ['build', '-s']

        def tasksResult = getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .build()

        then:
        ! new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        ! tasksResult.output.contains('Warnings:')
        ! tasksResult.output.contains('Errors:')
        ! tasksResult.output.contains('Warnings:')
        ! tasksResult.output.contains('Errors:')

        ! tasksResult.output.contains(':test_new:dependencyAnalysis')
			
	}

	@Unroll
	def "analyse for project without java"() {
		given:
		buildFile << """
            plugins {
                id 'com.intershop.gradle.dependencyanalysis'
            }

			version = '1.0.0.0'
			group = 'com.test.gradle'
		""".stripIndent()

		when:
		List<String> tasksArgs = ['build', '-s']

		def tasksResult = getPreparedGradleRunner()
				.withArguments(tasksArgs)
				.build()

		then:
		! new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

		! tasksResult.output.contains('Warnings:')
		! tasksResult.output.contains('Errors:')
		! tasksResult.output.contains('Warnings:')
		! tasksResult.output.contains('Errors:')

		! tasksResult.output.contains(':test_new:dependencyAnalysis')

	}
}
