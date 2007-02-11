/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * limitations under the License.
 */

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;

/**
 * GTK type codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: GTKTypes.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class GTKTypes {
    public static final int GTK_TYPE_INVALID = 0;
    public static final int GTK_TYPE_NONE = 1;
    public static final int GTK_TYPE_CHAR = 2;
    public static final int GTK_TYPE_UCHAR = 3;
    public static final int GTK_TYPE_BOOL = 4;
    public static final int GTK_TYPE_INT = 5;
    public static final int GTK_TYPE_UINT = 6;
    public static final int GTK_TYPE_LONG = 7;
    public static final int GTK_TYPE_ULONG = 8;
    public static final int GTK_TYPE_FLOAT = 9;
    public static final int GTK_TYPE_DOUBLE = 10;
    public static final int GTK_TYPE_STRING = 11;
    public static final int GTK_TYPE_ENUM = 12;
    public static final int GTK_TYPE_FLAGS = 13;
    public static final int GTK_TYPE_BOXED = 14;
    public static final int GTK_TYPE_POINTER = 15;
    public static final int GTK_TYPE_SIGNAL = 16;
    public static final int GTK_TYPE_ARGS = 17;
    public static final int GTK_TYPE_CALLBACK = 18;
    public static final int GTK_TYPE_C_CALLBACK = 19;
    public static final int GTK_TYPE_FOREIGN = 20;
    public static final int GTK_TYPE_OBJECT = 21;

    private GTKTypes() { /* VOID */}

    /**
     * Is this a valid GTK value?
     * @param val value to be checked
     * @return true if valid, false otherwise
     */
    public static boolean isValid(int val) {
        return (val >= GTK_TYPE_INVALID && val <= GTK_TYPE_OBJECT);
    }
} // end public class GTKTypes
