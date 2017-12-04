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
package com.intershop.gradle.analysis.analyzer

import com.intershop.gradle.analysis.utils.ClassNameCollector
import groovy.transform.CompileStatic
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

/**
 * Field analyzer
 * Identifies annotation dependencies
 */
@CompileStatic
class FieldAnalyzer extends FieldVisitor {

	private ClassNameCollector cc
	
	FieldAnalyzer(ClassNameCollector cc) {
		super(Opcodes.ASM5)
		this.cc = cc
	}
	
	@Override
	AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return new AnnotationAnalyzer(cc, desc, visible)
	}

}
