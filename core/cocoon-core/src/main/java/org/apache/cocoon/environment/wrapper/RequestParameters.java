/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * @version $Id$
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
