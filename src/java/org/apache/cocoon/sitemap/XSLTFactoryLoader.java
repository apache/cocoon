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
package org.apache.cocoon.sitemap;

import org.w3c.dom.NodeList;

/**
 * This class is used as a XSLT extension class.
 *
 * @deprecated This class has been used by the old sitemap engine
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: XSLTFactoryLoader.java,v 1.4 2004/03/05 13:02:58 bdelacretaz Exp $
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
