/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: EnumType.java,v 1.5 2003/11/29 15:37:57 ugo Exp $
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
