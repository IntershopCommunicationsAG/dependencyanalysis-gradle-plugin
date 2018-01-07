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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
/**
 * Artifact class contains all information about the artifact self.
 */
@CompileStatic
@Slf4j
class Artifact implements Serializable {

	private Set<File> files

	void addFile(File file) {
		this.files.add(file)
		containedClasses = getClassFiles(file)
	}

	Set<File> getFiles() {
		return files
	}

    String configuration

	String group
	String module
	String version
	Set<String> containedClasses = []
    Set<String> usedClasses = []

    Set<String> duplicatedClasses = []
    Set<String> excludedDuplicatedClasses = []
    Set<Artifact> duplicatedArtifacts = []
    Set<Artifact> excludedDuplicatedArtifacts = []

	boolean ignoreForAnalysis
    boolean projectDependeny
    boolean libraryDependency
    boolean firstLevel

    String projectPath

	/**
	 * Constructor
	 *
	 * @param absoluteFile  File representation
	 * @param module        Module name
	 * @param isTransitive  Dependency resolution
	 */
	Artifact(String group, String module, String configuration) {
        this.group = group
		this.module = module
		this.configuration = configuration

		this.projectDependeny = false
        this.libraryDependency = false

        this.ignoreForAnalysis = false
        this.firstLevel = false

        this.files = [] as HashSet
	}

    void setTransitive(int value) {
        transitive = value
    }

	String getName() {
		return "${group}:${module}:${version}"
	}	
	
	Set<String> getContainedClasses() {
		return containedClasses
	}

    /**
     * {@inheritDoc}
     */
    @Override
    String toString() {
        if(projectPath) {
            return projectPath
        }
		else if(version) {
			return "${group}:${module}:${version}"
		} else {
			return "${group}:${module}:${version}"
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean equals(Object other) {
        if (!(other instanceof Artifact)) {
            return false
        }
        boolean rv = false

        rv = rv && ((Artifact)other).getGroup() == group
        if(!rv) return false

        rv = ((Artifact)other).getModule() == module
        if(!rv) return false

        rv = ((Artifact)other).getVersion() == version
        if(!rv) return false

        rv = ((Artifact)other).getConfiguration() == configuration
        if(!rv) return false

        rv = ((Artifact)other).getProjectPath() == projectPath
        if(!rv) return false

        rv = ((Artifact)other).getFiles().size() == files.size()
        if(!rv) return false

        Collection<File> similar = new HashSet<File>( ((Artifact)other).getFiles() )
        Collection<File> different = new HashSet<File>()
        different.addAll( ((Artifact)other).getFiles() )
        different.addAll( files )

        similar.retainAll( files )
        different.removeAll( similar )

        return different.isEmpty()

    }

    /**
     * {@inheritDoc}
     */
    @Override
    int hashCode() {
        int hash = 5
        hash = 97 * hash + configuration.hashCode()
        hash = 97 * hash + group.hashCode()
        hash = 97 * hash + module.hashCode()
		if(version) {
			hash = 97 * hash + version.hashCode()
		}
        if(projectPath) {
            hash = 97 * hash + projectPath.hashCode()
        }
        if(! files.isEmpty()) {
            files.each {
                hash = 97 * hash + it.getAbsolutePath().hashCode()
            }
        }
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
			
			zipFile.entries().findAll { !((ZipEntry)it).directory && ((ZipEntry)it).name.endsWith('.class') }.each { Object entry ->
				String name = ((ZipEntry)entry).getName()
				
				if (name.endsWith( ".class" )){
					classfiles.add(name.replaceAll("/", "."))
				}
			}
			log.info('Analysing of {} was successful.', file)
		} catch(Exception ex) {
			log.error('Errors during the analyze of the jar file {}: {}', file, ex.getMessage())
		}
		return classfiles
	}
}
