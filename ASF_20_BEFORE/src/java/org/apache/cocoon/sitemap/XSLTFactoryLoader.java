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
package org.apache.cocoon.sitemap;

import org.w3c.dom.NodeList;

/**
 * This class is used as a XSLT extension class.
 *
 * @deprecated This class has been used by the old sitemap engine
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: XSLTFactoryLoader.java,v 1.3 2004/02/21 15:46:39 cziegeler Exp $
 */
public class XSLTFactoryLoader {

    public String getClassSource(String className, String prefix, String pattern, NodeList conf) throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, Exception {

        throw new UnsupportedOperationException("CodeFactory is no longer supported.");
    }

    public String getParameterSource(String className, NodeList conf) throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, Exception {

        throw new UnsupportedOperationException("CodeFactory is no longer supported.");
    }

    public String getMethodSource(String className, NodeList conf) throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, Exception {

        throw new UnsupportedOperationException("CodeFactory is no longer supported.");
    }

    public boolean isFactory(String className) {

        throw new UnsupportedOperationException("Factories are no longer supported.");
    }

    /**
     * Escapes '"' and '\' characters in a String (add a '\' before them) so that it can
     * be inserted in java source.
     */
    public String escape(String string) {
        if (string.indexOf('\\') == -1 && string.indexOf('"') == -1) {
            // Nothing to escape
            return string;
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch == '\\' || ch == '"') {
                buf.append('\\');
            }
            buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * Escapes like {@link #escape(String)} after having removed any '\' preceding a '{'.
     * This is used to insert a pattern with escaped subsitution syntax in Java source.
     */
    public String escapeBraces(String string) {
        if (string.indexOf("\\{") == -1)
        {
            return escape(string);
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (ch != '\\' || i >= (string.length() - 1) || string.charAt(i+1) != '{') {
                buf.append(ch);
            }
        }
        return escape(buf.toString());
    }

    public boolean hasSubstitutions(String pattern) {
        if (pattern.length() == 0) {
            return false;
        }
        // Does it start by a substitution ?
        if (pattern.charAt(0) == '{') {
            return true;
        }

        // Search for an unescaped '{'
        int i = 1;
        while ((i = pattern.indexOf('{', i)) != -1) {
            if (pattern.charAt(i-1) != '\\') {
                return true;
            }
            i++; // Pass '{'
        }

        return false;
    }
}
