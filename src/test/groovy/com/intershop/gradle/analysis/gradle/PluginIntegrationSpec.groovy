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

import com.intershop.gradle.analysis.gradle.fixtures.JarArchiveCreator
import com.intershop.gradle.test.AbstractIntegrationSpec
import spock.lang.Unroll

class PluginIntegrationSpec extends AbstractIntegrationSpec {

    def setup() {
        File settingsGradleNew = file('settings.gradle')
        settingsGradleNew << """
            rootProject.name = 'test_new'
        """.stripIndent()

        copyResources('test_new/src', 'src')
    }

    @Unroll
	def 'analyse for project without issues - #gradleVersion'(gradleVersion) {
		given:
        buildFile << """
            plugins {
                id 'java'
                id 'com.intershop.gradle.dependencyanalysis'
            }
            
            dependencyAnalysis {
                failOnDuplicates = false
                failOnUnusedFirstLevelDependencies = false
                failOnUsedTransitiveDependencies = false
                failOnUnusedTransitiveDependencies = false
                
                excludeDuplicatePatterns = ['javax.persistence.*']
                excludeDependencyPatterns = ['com.intershop:api_remote_service:.*']
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

        getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .withGradleVersion(gradleVersion)
                .build()

		then:
		new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

		where:
        gradleVersion << supportedGradleVersions
	}

	@Unroll
	def 'analyse for project not failOnErrors - #gradleVersion'(gradleVersion) {
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
                failOnDuplicates = false
                failOnUnusedFirstLevelDependencies = false
                failOnUsedTransitiveDependencies = false
                failOnUnusedTransitiveDependencies = false

            	dependencyReporting { depReport ->
					File customReportDir = file('build/reports/customDependencyAnalysis/')
					customReportDir.mkdirs()
					File unusedDepsReport = new File(customReportDir, 'unused.txt')
					unusedDepsReport.withPrintWriter { p ->
						depReport.unused.each { dep ->
							p.println dep.name
						}
					}
					
					println "duplicates: " + depReport.duplicates
					File duplicateDepsReport = new File(customReportDir, 'duplicates.txt')
					duplicateDepsReport.withPrintWriter { p ->
						depReport.duplicates.each { dep ->
							p.println dep.name
						}
					}
            	}
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

        getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .withGradleVersion(gradleVersion)
                .build()

        then:
		assertGeneratedReports(1, 2)

        where:
        gradleVersion << supportedGradleVersions
	}

    @Unroll
    def 'analyse for project not failOnErrors with duplicates - #gradleVersion'(gradleVersion) {
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
                failOnDuplicates = false
                failOnUnusedFirstLevelDependencies = false
                failOnUsedTransitiveDependencies = false
                failOnUnusedTransitiveDependencies = false
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

        getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .withGradleVersion(gradleVersion)
                .build()

        then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        where:
        gradleVersion << supportedGradleVersions
    }

	@Unroll
	def 'analyse for project failOnErrors - #gradleVersion'(gradleVersion) {
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

        getPreparedGradleRunner()
                .withArguments(tasksArgs)
                .withGradleVersion(gradleVersion)
                .buildAndFail()

        then:
        new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        where:
        gradleVersion << supportedGradleVersions
	}

	@Unroll
	def 'analyse for project - exclude duplicates - #gradleVersion'(gradleVersion) {
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

            
            dependencyAnalysis {                
                excludeDuplicatePatterns = ['org.objectweb.asm.*']
                excludeDependencyPatterns = ['net.sf.ehcache:ehcache-core:.*', 
                                             'org.springframework:spring-web:.*']
            }
            
            repositories {
                jcenter()
            }

			dependencies {
				compile 'com.google.code.findbugs:annotations:3.0.0'
				compile 'javax.persistence:persistence-api:1.0.2'
				compile 'javax.validation:validation-api:1.0.0.GA'
				compile 'commons-logging:commons-logging:1.2'
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

        getPreparedGradleRunner()
				.withArguments(tasksArgs)
                .withGradleVersion(gradleVersion)
				.build()

		then:
		new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        where:
        gradleVersion << supportedGradleVersions
	}

	@Unroll
	def 'analyse for project failOnErrors with java-library plugin - #gradleVersion'(gradleVersion) {
		given:
		buildFile << """
            plugins {
                id 'java-library'
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
				api 'com.google.code.findbugs:annotations:3.0.0'
				api 'javax.persistence:persistence-api:1.0.2'
				api 'javax.validation:validation-api:1.0.0.GA'
				api 'org.ow2.asm:asm:5.1'
				api 'org.slf4j:slf4j-api:1.7.21'
				
				api 'junit:junit:4.12'
				api('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}

				//not necessary for compilation
				api 'net.sf.ehcache:ehcache-core:2.6.11'
				api 'org.springframework:spring-web:4.1.6.RELEASE'

                // double classes
                api 'org.ow2.asm:asm-all:4.2'
			}
		""".stripIndent()

		when:
		List<String> tasksArgs = ['build', '-s']

		getPreparedGradleRunner()
				.withArguments(tasksArgs)
                .withGradleVersion(gradleVersion)
				.buildAndFail()

		then:
		new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()

        where:
        gradleVersion << supportedGradleVersions
	}

	@Unroll
	def 'analyse for project failOnErrors with file collection dependencies - #gradleVersion'(gradleVersion) {

		given:
		def jar = someJar()
		buildFile << """
            plugins {
                id 'java-library'
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
				
				api 'com.google.code.findbugs:annotations:3.0.0'
				api 'javax.persistence:persistence-api:1.0.2'
				api 'javax.validation:validation-api:1.0.0.GA'
				api 'org.ow2.asm:asm:5.1'
				api 'org.slf4j:slf4j-api:1.7.21'
				
				api 'junit:junit:4.12'
				api('com.netflix.servo:servo-atlas:0.12.11') {
					transitive = false
				}
				api 'commons-logging:commons-logging:1.2'

				// unrequired plain file dependency				
				api files('${jar.name}')
			}
		""".stripIndent()

		when:
		List<String> tasksArgs = ['build', '-s']

		getPreparedGradleRunner()
				.withArguments(tasksArgs)
				.withGradleVersion(gradleVersion)
				.buildAndFail()

		def htmlReport = new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html')
		then:
		htmlReport.exists()

		// unused plain file reference is listed in dependency report
		htmlReport.text.contains(jar.absolutePath)

		where:
		gradleVersion << supportedGradleVersions
	}

	@Unroll
	def 'analyse for project disabled - #gradleVersion'(gradleVersion) {
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
                .withGradleVersion(gradleVersion)
                .build()

        then:
        ! new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()
        ! tasksResult.output.contains(':test_new:dependencyAnalysis')

        where:
        gradleVersion << supportedGradleVersions
	}

	@Unroll
	def 'analyse for project without java - #gradleVersion'(gradleVersion) {
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
                .withGradleVersion(gradleVersion)
				.build()

		then:
		! new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()
		! tasksResult.output.contains(':test_new:dependencyAnalysis')

        where:
        gradleVersion << supportedGradleVersions
	}


	private File someJar(String fileName = "someJar.jar") {
		def file = new File(testProjectDir, fileName)
		new JarArchiveCreator().createJar(file);
	}

	def assertGeneratedReports(int unusedDepsCount, int duplicatesCount) {
		new File(testProjectDir, 'build/reports/dependencyAnalysis/dependency-report.html').exists()
		def unusedDeps = new File(testProjectDir, 'build/reports/customDependencyAnalysis/unused.txt')
		assert unusedDeps.readLines().size() == unusedDepsCount

		def duplicateDeps = new File(testProjectDir, 'build/reports/customDependencyAnalysis/duplicates.txt')
		assert duplicateDeps.readLines().size() == duplicatesCount
		true
	}
}
