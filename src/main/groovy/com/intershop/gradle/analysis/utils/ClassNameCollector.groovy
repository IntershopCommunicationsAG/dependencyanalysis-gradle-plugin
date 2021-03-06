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


package com.intershop.gradle.analysis.utils

import groovy.transform.CompileStatic
import org.objectweb.asm.Type

/**
 * Collects all used classes for check
 */
@CompileStatic
class ClassNameCollector {
	
	private Set<String> classes 
	
	ClassNameCollector() {
		classes = new HashSet<String>()
	}
	
	Set<String> getClasses() {
		return classes
	}
	
	void addName( String name ) {
		if (name == null) {
			return
		}

		// decode arrays
		if (name.startsWith("[L") && name.endsWith( ";" )) {
			name = name.substring( 2, name.length() - 1 )
		}
		
		if (name.startsWith( "L") && name.endsWith( ";" )) {
			name = name.substring( 1, name.length() - 1 )
		}

		// decode internal representation
		name = name.replace( '/', '.' )

		if(! name.startsWith('java.lang') || name.startsWith('java.')) {
			classes.add( "${name}.class".toString() )
		}
	}
	
	void addNames(final String[] names) {
		if (! names) {
			return
		}

		for (String name : names) {
			addName( name )
		}
	}
	
	void addDesc(final String desc) {
		addType( Type.getType( desc ) )
	}

	void addMethodDesc(final String desc) {
		addType(Type.getReturnType(desc))
		Type[] types = Type.getArgumentTypes(desc)

		for (Type type : types) {
			addType(type)
		}
	}
	
	void addType(final Type t) {
		switch (t.getSort()) {
			case Type.ARRAY:
				addType( t.getElementType() )
				break

			case Type.OBJECT:
				addName( t.getClassName().replace( '.', '/' ) )
				break
		}
	}

}
