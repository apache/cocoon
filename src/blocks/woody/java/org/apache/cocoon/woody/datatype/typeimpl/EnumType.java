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
package org.apache.cocoon.woody.datatype.typeimpl;

/**
 * A {@link org.apache.cocoon.woody.datatype.Datatype Datatype} implementation for
 * types implementing Joshua Bloch's <a href="http://developer.java.sun.com/developer/Books/shiftintojava/page1.html#replaceenums">
 * typesafe enum</a> pattern.
 * <p>See the following code for an example:</p>
 * <pre>
 * package com.example;
 * 
 * public class Sex {
 *
 *   public static final Sex MALE = new Sex("M");
 *   public static final Sex FEMALE = new Sex("F");
 *   private String code;
 *
 *   private Sex(String code) { this.code = code; }
 * }
 * </pre>
 * <p>If your enumerated type does not provide a {@link java.lang.Object#toString()}
 * method, the enum convertor will use the fully qualified class name,
 * followed by the name of the public static final field referring to
 * each instance, i.e. "com.example.Sex.MALE", "com.example.Sex.FEMALE"
 * and so on.</p>
 * <p>If you provide a toString() method which returns something
 * different, you should also provide a fromString(String, Locale)
 * method to convert those strings back to instances.
 *  
 * @version CVS $Id: EnumType.java,v 1.8 2004/03/09 13:53:54 reinhard Exp $
 */
public class EnumType extends AbstractDatatype {
    
    public EnumType() {
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.Datatype#getTypeClass()
     */
    public Class getTypeClass() {
        return this.getConvertor().getTypeClass();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.Datatype#getDescriptiveName()
     */
    public String getDescriptiveName() {
        return this.getConvertor().getTypeClass().getName();
    }
}
