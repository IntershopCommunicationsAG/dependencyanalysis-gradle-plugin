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
import groovy.util.logging.Slf4j

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor

@CompileStatic
@Slf4j
class AnnotationAnalyzer extends AnnotationVisitor {
	
    AnnotationAnalyzer(ClassNameCollector cc, String desc, boolean visible) {
        super(Opcodes.ASM5)
		if(desc) {
			cc.addDesc(desc)
		}
    }
	/**
     * Class analyzer
     * Identifies also annotation dependencies
     */
	@Slf4j
    static class ClassAnalyzer extends ClassVisitor {

        private ClassNameCollector cc

        ClassAnalyzer(ClassNameCollector cc) {
            super(Opcodes.ASM5)
            this.cc = cc
        }

        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            ClassAnalyzer.log.info('class {} extends {} implements {}' , name, superName, interfaces)

            if (signature == null) {
                cc.addName( superName )
                cc.addNames( interfaces )
            } else {
                addTypeSignature(signature)
            }
        }

        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new AnnotationAnalyzer(cc, desc, visible)
        }

        @Override
        void visitAttribute(Attribute attribute) { }

        @Override
        void visitInnerClass(String name, String outerName, String innerName, int access) { }

        @Override
        void visitSource(String source, String debug) { }

        @Override
        void visitOuterClass(String owner, String name, String desc) {}

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (signature == null) {
                cc.addDesc( desc )
            }

            if (value instanceof Type) {
                cc.addType( (Type) value )
            }
            return new FieldAnalyzer(cc)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (signature == null) {
                cc.addMethodDesc( desc )
            }

            cc.addNames( exceptions )
            return new MethodAnalyzer(cc)
        }

        @Override
        void visitEnd() {
            ClassAnalyzer.log.info("Finished")
        }

        private void addTypeSignature( String signature ) {
            if ( signature != null ) {
                new SignatureReader( signature ).acceptType( (SignatureVisitor)new SignatureVisitorAnalyzer(cc) )
            }
        }
    }
}
