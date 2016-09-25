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
package com.intershop.gradle.analysis.analyzer;

import com.intershop.gradle.analysis.utils.ClassNameCollector;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Computes the set of classes referenced by visited code.
 * Inspired by <code>org.objectweb.asm.depend.DependencyVisitor</code> in the ASM dependencies example.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @version $Id$
 */
public class SignatureVisitorAnalyzer extends SignatureVisitor
{
    private ClassNameCollector cc

    public SignatureVisitorAnalyzer(ClassNameCollector cc) {
        super(Opcodes.ASM5)
        this.cc = cc
    }

    public void visitClassType( final String name ) {
        cc.addName( name )
    }

    public void visitInnerClassType( final String name ) {
        cc.addName( name )
    }
}
