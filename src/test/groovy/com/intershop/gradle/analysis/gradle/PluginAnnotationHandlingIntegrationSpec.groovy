package com.intershop.gradle.analysis.gradle

import com.intershop.gradle.test.AbstractIntegrationSpec
import spock.lang.Unroll

class PluginAnnotationHandlingIntegrationSpec extends AbstractIntegrationSpec {

    def setup() {
        File settingsGradleNew = file('settings.gradle')
        settingsGradleNew << """
            rootProject.name = 'test_new'
        """.stripIndent()

        copyResources('test_annotations/src', 'src')
    }

    @Unroll
    def 'detects class reference in annotations - #gradleVersion'(gradleVersion) {
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
				compile 'org.slf4j:slf4j-api:1.7.21'
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
}