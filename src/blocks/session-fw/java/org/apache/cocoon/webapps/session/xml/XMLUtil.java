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
package org.apache.cocoon.webapps.session.xml;



/**
 * A utility class which will soon be removed...
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: XMLUtil.java,v 1.1 2003/03/09 00:06:12 pier Exp $
*/
public final class XMLUtil {

    /**
     * Convert umlaute to entities
     */
    public static String encode(String value) {
        StringBuffer buffer = new StringBuffer(value);
        for(int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) > 127) {
                buffer.replace(i, i+1, "__"+((int)buffer.charAt(i))+";");
            }
        }
        return buffer.toString();
    }

    /**
     * Convert entities to umlaute
     */
    public static String decode(String value) {
        StringBuffer buffer = new StringBuffer(value);
        int pos;
        boolean found;
        for(int i = 0; i < buffer.length(); i++) {
            if (buffer.charAt(i) == '_' &&
                buffer.charAt(i+1) == '_') {
                pos = i + 2;
                found = false;
                while (buffer.charAt(pos) >= '0'
                       && buffer.charAt(pos) <= '9') {
                    found = true;
                    pos++;
                }
                if (found == true
                    && pos > i + 2
                    && buffer.charAt(pos) == ';') {
                    int ent = new Integer(buffer.substring(i+2, pos)).intValue();
                    buffer.replace(i, pos+1, ""+ (char)ent);
                }
            }
        }
        return buffer.toString();
    }

}
