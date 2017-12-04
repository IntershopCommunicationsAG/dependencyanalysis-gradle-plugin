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

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * A small parser to read the constant pool directly, in case it contains references
 * ASM does not support.
 *
 * Adapted from http://stackoverflow.com/a/32278587/23691
 * See also http://svn.apache.org/viewvc/maven/shared/trunk/maven-dependency-analyzer/?pathrev=1717974
 */
class ConstantPoolParser
{
    static final int HEAD = 0xcafebabe;

    // Constant pool types
    static final byte CONSTANT_UTF8 = 1
    static final byte CONSTANT_INTEGER = 3
    static final byte CONSTANT_FLOAT = 4
    static final byte CONSTANT_LONG = 5
    static final byte CONSTANT_DOUBLE = 6
    static final byte CONSTANT_CLASS = 7
    static final byte CONSTANT_STRING = 8
    static final byte CONSTANT_FIELDREF = 9
    static final byte CONSTANT_METHODREF = 10
    static final byte CONSTANT_INTERFACEMETHODREF = 11
    static final byte CONSTANT_NAME_AND_TYPE = 12
    static final byte CONSTANT_METHODHANDLE = 15
    static final byte CONSTANT_METHOD_TYPE = 16
    static final byte CONSTANT_INVOKE_DYNAMIC = 18
    private static final int OXF0 = 0xf0
    private static final int OXE0 = 0xe0
    private static final int OX3F = 0x3F

    static Set<String> getConstantPoolClassReferences( byte[] b ) {
        return parseConstantPoolClassRefereces( ByteBuffer.wrap( b ) )
    }

    static Set<String> parseConstantPoolClassRefereces( ByteBuffer buf ) {
        if ( buf.order( ByteOrder.BIG_ENDIAN ).getInt() != HEAD ) {
            return Collections.emptySet();
        }
        // minor + ver
        buf.getChar()
        buf.getChar()
        Set<Integer> classes = new HashSet<Integer>()

        Map<Integer, String> stringConstants = new HashMap<Integer, String>()
        int ix = 1
        for (int num = buf.getChar(); ix < num; ix++ ) {
            byte tag = buf.get()
            switch ( tag ) {
                case CONSTANT_UTF8:
                    stringConstants.put( ix, decodeString( buf ) )
                    continue;
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                case CONSTANT_METHOD_TYPE:
                    classes.add( (int) buf.getChar() )
                    break
                case CONSTANT_FIELDREF:
                case CONSTANT_METHODREF:
                case CONSTANT_INTERFACEMETHODREF:
                case CONSTANT_NAME_AND_TYPE:
                    buf.getChar()
                    buf.getChar()
                    break
                case CONSTANT_INTEGER:
                    buf.getInt()
                    break
                case CONSTANT_FLOAT:
                    buf.getFloat()
                    break
                case CONSTANT_DOUBLE:
                    buf.getDouble()
                    break
                case CONSTANT_LONG:
                    buf.getLong()
                    break
                case CONSTANT_METHODHANDLE:
                    buf.get()
                    buf.getChar()
                    break
                case CONSTANT_INVOKE_DYNAMIC:
                    buf.getChar()
                    buf.getChar()
                    break
            }
        }
        Set<String> result = new HashSet<String>()
        classes.each {
            result.add( stringConstants.get( it ) )
        }
        return result;
    }

    private static String decodeString( ByteBuffer buf )
    {
        int size = buf.getChar(), oldLimit = buf.limit();
        buf.limit( buf.position() + size );
        StringBuilder sb = new StringBuilder( size + ( size >> 1 ) + 16 );
        while ( buf.hasRemaining() )
        {
            byte b = buf.get();
            if ( b > 0 )
            {
                sb.append( (char) b );
            }
            else
            {
                int b2 = buf.get();
                if ( ( b & OXF0 ) != OXE0 )
                {
                    sb.append( (char) ( ( b & 0x1F ) << 6 | b2 & OX3F ) );
                }
                else
                {
                    int b3 = buf.get();
                    sb.append( (char) ( ( b & 0x0F ) << 12 | ( b2 & OX3F ) << 6 | b3 & OX3F ) );
                }
            }
        }
        buf.limit( oldLimit );
        return sb.toString();
    }
}
