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

import com.intershop.gradle.analysis.analyzer.AnnotationAnalyzer
import com.intershop.gradle.analysis.analyzer.ConstantPoolParser
import com.intershop.gradle.analysis.utils.ClassNameCollector
import com.intershop.gradle.analysis.utils.ReadClassException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.objectweb.asm.ClassReader

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Project dependency implementation
 * Extends artifact with additional information.
 */
@CompileStatic
@Slf4j
class AnalyzedProjectDependency extends AnalyzedExternalDependency {

	Map<String, Set<String>> dependencyMap = [:]

	AnalyzedProjectDependency(String group, String module, String configuration) {
		super(group, module, configuration)
	}

	void addFile(File file) {
		super.addFile(file)
		dependencyMap.putAll(getAnalyzedClasses(file))
	}
	
	Set<String> getAllDependencyClasses() {
		Set<String> adc = []
		dependencyMap.keySet().each {
			adc.addAll(dependencyMap.get(it))
		}
		return adc
	}
	
	Map<String, Set<String>> getDependencyMap() {
		return dependencyMap
	}
	
	
	static Map<String,Set<String>> getAnalyzedClasses(File file) {
		Map<String, Set<String>> classmap = [:]		
		
		ZipFile zipFile = new ZipFile(file)
				
		zipFile.entries().findAll { !((ZipEntry)it).directory && ((ZipEntry)it).name.endsWith('.class') }.each { Object entry ->
			String name = ((ZipEntry)entry).getName()
			if (name.endsWith( ".class" )) {
				String className = name.replaceAll('/', '.')
				classmap.put(className, readClass(className, zipFile.getInputStream((ZipEntry)entry).getBytes()))
			}
		}
		return classmap
	}
	
	static Set<String> readClass(String name, byte[] data) throws ReadClassException {
		
		try {
			ClassReader cr = new ClassReader(data)
            ClassNameCollector cc = new ClassNameCollector()

			final Set<String> constantPoolClassRefs = ConstantPoolParser.getConstantPoolClassReferences( cr.b )
            constantPoolClassRefs.each {
                cc.addName(it)
            }

			AnnotationAnalyzer.ClassAnalyzer da = new AnnotationAnalyzer.ClassAnalyzer(cc)
			cr.accept(da, 0)

			return cc.getClasses()

		} catch (RuntimeException exc) {
			exc.printStackTrace()
			throw new ReadClassException("Error occurred while loading class ${name}:${exc.toString()}", exc)
		}
	}
}
