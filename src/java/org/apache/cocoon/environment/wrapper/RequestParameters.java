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
package org.apache.cocoon.environment.wrapper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class is used by the <code>RequestWrapper</code>. It parses
 * a query string and creates a parameter representation required
 * for the <code>Request</code> object.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: RequestParameters.java,v 1.4 2004/03/01 03:50:59 antonio Exp $
 */
public final class RequestParameters
implements Serializable {

    /** The parameter names are the keys and the value is a List object */
    private Map names;

    /**
     * Decode the string
     */
    private String parseName(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    break;
                case '%':
                    try {
                        sb.append((char) Integer.parseInt(s.substring(i+1, i+3),
                              16));
                        i += 2;
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException e) {
                        String rest  = s.substring(i);
                        sb.append(rest);
                        if (rest.length()==2)
                            i++;
                    }

                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Construct a new object from a queryString
     */
    public RequestParameters(String queryString) {
        this.names = new HashMap(5);
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                int pos = pair.indexOf('=');
                if (pos != -1) {
                    this.setParameter(this.parseName(pair.substring(0, pos)),
                                      this.parseName(pair.substring(pos+1, pair.length())));
                }
            }
        }
    }

    /**
     * Add a parameter.
     * The parameter is added with the given value.
     * @param name   The name of the parameter.
     * @param value  The value of the parameter.
     */
    private void setParameter(String name, String value) {
        ArrayList list;
        if (names.containsKey(name)) {
            list = (ArrayList)names.get(name);
        } else {
            list = new ArrayList(3);
            names.put(name, list);
        }
        list.add(value);
    }

    /**
     * Get the value of a parameter.
     * @param name   The name of the parameter.
     * @return       The value of the first parameter with the name
     *               or <CODE>null</CODE>
     */
    public String getParameter(String name) {
        if (names.containsKey(name)) {
            return (String)((ArrayList)names.get(name)).get(0);
        }
        return null;
    }

    /**
     * Get the value of a parameter.
     * @param name   The name of the parameter.
     * @param defaultValue The default value if the parameter does not exist.
     * @return       The value of the first parameter with the name
     *               or <CODE>defaultValue</CODE>
     */
    public String getParameter(String name, String defaultValue) {
        if (names.containsKey(name)) {
            return (String)((ArrayList)names.get(name)).get(0);
        }
        return defaultValue;
    }

    /**
     * Get all values of a parameter.
     * @param name   The name of the parameter.
     * @return       Array of the (String) values or null if the parameter
     *               is not defined.
     */
    public String[] getParameterValues(String name) {
        if (names.containsKey(name)) {
            String values[] = null;
            ArrayList list = (ArrayList)names.get(name);
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                if (values == null) {
                    values = new String[1];
                } else {
                    String[] copy = new String[values.length+1];
                    System.arraycopy(values, 0, copy, 0, values.length);
                    values = copy;
                }
                values[values.length-1] = (String)iter.next();
            }
            return values;
        }
        return null;
    }

    /**
     * Get all parameter names.
     * @return  Enumeration for the (String) parameter names.
     */
    public Enumeration getParameterNames() {
        return new EnumerationFromIterator(names.keySet().iterator());
    }

    final class EnumerationFromIterator implements Enumeration {
        private Iterator iter;
        EnumerationFromIterator(Iterator iter) {
            this.iter = iter;
        }

        public boolean hasMoreElements() {
            return iter.hasNext();
        }
        public Object nextElement() { return iter.next(); }
    }

}
