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
package com.intershop.gradle.analysis.model

import groovy.util.logging.Slf4j

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
/**
 * Artifact class contains all information about the artifact self.
 */
@Slf4j
class Artifact {

	File absoluteFile
	String module
	String version
	Set<String> containedClasses = []
    Set<String> usedClasses = []

    Set<String> dublicatedClasses = []
    Set<Artifact> dublicatedArtifacts = []

    int transitive

    /**
     * Constructor
     *
     * @param absoluteFile  File representation
     * @param module        Module name
     * @param version       Version
     * @param isTransitive  Dependency resolution
     */
	Artifact(File absoluteFile, String module, String version) {
		this.absoluteFile = absoluteFile
		this.module = module
		this.version = version
		
		this.transitive = 0
		
		containedClasses = getClassFiles(absoluteFile)
	}

    void setTransitive(int value) {
        transitive = value
    }

	File getAbsoluteFile() {
		return absoluteFile
	}

	String getName() {
		return "${module}:${version}"
	}	
	
	Set<String> getContainedClasses() {
		return containedClasses
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "${module}:${version}"
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Artifact)) {
            return false;
        }
        return (other.getModule() == module && other.getVersion() == version && other.absoluteFile == absoluteFile)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + module.hashCode()
        hash = 97 * hash + version.hashCode()
        hash = 97 * hash + absoluteFile.getAbsolutePath().hashCode()
        return hash
    }

    /**
     * Calculates the used class files
     *
     * @param file
     * @return
     */
	static Set<String> getClassFiles(File file) {
		Set<String> classfiles = new HashSet<String>()
		
		try {
			ZipFile zipFile = new ZipFile(file)
			
			zipFile.entries().findAll { !it.directory && it.name.endsWith('.class') }.each { ZipEntry entry ->
				String name = entry.getName()
				
				if (name.endsWith( ".class" )){
					classfiles.add(name.replaceAll("/", "."));
				}
			}
			log.info('Analysing of {} was successful.', file)
		} catch(Exception ex) {
			log.error('Errors during the analyze of the jar file {}: {}', file, ex.getMessage())
		}
		return classfiles
	}
}
