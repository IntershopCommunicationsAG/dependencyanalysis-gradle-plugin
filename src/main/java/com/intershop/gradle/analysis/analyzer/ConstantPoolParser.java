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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * A small parser to read the constant pool directly, in case it contains references
 * ASM does not support.
 * <p>
 * Adapted from http://stackoverflow.com/a/32278587/23691
 * See also http://svn.apache.org/viewvc/maven/shared/trunk/maven-dependency-analyzer/?pathrev=1717974
 */

public class ConstantPoolParser {

    private static final int HEAD = 0xcafebabe;

    // Constant pool types
    private static final byte CONSTANT_UTF8 = 1;
    private static final byte CONSTANT_INTEGER = 3;
    private static final byte CONSTANT_FLOAT = 4;
    private static final byte CONSTANT_LONG = 5;
    private static final byte CONSTANT_DOUBLE = 6;
    private static final byte CONSTANT_CLASS = 7;
    private static final byte CONSTANT_STRING = 8;
    private static final byte CONSTANT_FIELDREF = 9;
    private static final byte CONSTANT_METHODREF = 10;
    private static final byte CONSTANT_INTERFACEMETHODREF = 11;
    private static final byte CONSTANT_NAME_AND_TYPE = 12;
    private static final byte CONSTANT_METHODHANDLE = 15;
    private static final byte CONSTANT_METHOD_TYPE = 16;
    private static final byte CONSTANT_INVOKE_DYNAMIC = 18;

    private static final int OXF0 = 0xf0;
    private static final int OXE0 = 0xe0;
    private static final int OX3F = 0x3F;

    public static Set<String> getConstantPoolClassReferences(byte[] b) {
        return parseConstantPoolClassReferences(ByteBuffer.wrap(b));
    }

    private static Set<String> parseConstantPoolClassReferences(ByteBuffer buf) {
        if (buf.order(ByteOrder.BIG_ENDIAN)
                .getInt() != HEAD) {
            return Collections.emptySet();
        }
        buf.getChar();
        buf.getChar(); // minor + ver
        Set<Integer> classes = new HashSet<>();
        Map<Integer, String> stringConstants = new HashMap<>();
        for (int ix = 1, num = buf.getChar(); ix < num; ix++) {
            byte tag = buf.get();
            switch (tag) {
                default:
                    throw new RuntimeException("Unknown constant pool type");
                case CONSTANT_UTF8:
                    stringConstants.put(ix, decodeString(buf));
                    continue;
                case CONSTANT_CLASS:
                case CONSTANT_STRING:
                case CONSTANT_METHOD_TYPE:
                    classes.add((int) buf.getChar());
                    break;
                case CONSTANT_FIELDREF:
                case CONSTANT_METHODREF:
                case CONSTANT_INTERFACEMETHODREF:
                case CONSTANT_NAME_AND_TYPE:
                    buf.getChar();
                    buf.getChar();
                    break;
                case CONSTANT_INTEGER:
                    buf.getInt();
                    break;
                case CONSTANT_FLOAT:
                    buf.getFloat();
                    break;
                case CONSTANT_DOUBLE:
                    buf.getDouble();
                    ix++;
                    break;
                case CONSTANT_LONG:
                    buf.getLong();
                    ix++;
                    break;
                case CONSTANT_METHODHANDLE:
                    buf.get();
                    buf.getChar();
                    break;
                case CONSTANT_INVOKE_DYNAMIC:
                    buf.getChar();
                    buf.getChar();
                    break;
            }
        }
        Set<String> result = new HashSet<>();
        for (Integer aClass : classes) {
            result.add(stringConstants.get(aClass));
        }
        return result;
    }

    private static String decodeString(ByteBuffer buf) {
        int size = buf.getChar(), oldLimit = buf.limit();
        buf.limit(buf.position() + size);
        StringBuilder sb = new StringBuilder(size + (size >> 1) + 16);
        while (buf.hasRemaining()) {
            byte b = buf.get();
            if (b > 0) {
                sb.append((char) b);
            } else {
                int b2 = buf.get();
                if ((b & OXF0) != OXE0) {
                    sb.append((char) ((b & 0x1F) << 6 | b2 & OX3F));
                } else {
                    int b3 = buf.get();
                    sb.append((char) ((b & 0x0F) << 12 | (b2 & OX3F) << 6 | b3 & OX3F));
                }
            }
        }
        buf.limit(oldLimit);
        return sb.toString();
    }
}
