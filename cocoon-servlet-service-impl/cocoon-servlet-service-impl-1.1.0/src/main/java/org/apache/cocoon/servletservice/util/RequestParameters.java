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
package org.apache.cocoon.servletservice.util;

import org.apache.commons.collections.iterators.IteratorEnumeration;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class is used by the <code>RequestWrapper</code>. It parses
 * a query string, decodes request parameters, and creates a parameter map
 * ready for use by the <code>Request</code> object.
 *
 * @version $Id$
 * @since 1.0.0
 */
public final class RequestParameters implements Serializable {

    /**
     * The parameter names are the keys and the value is a String array object
     */
    private final Map values;

    /**
     * Construct a new object from a queryString
     *
     * @param queryString request query string
     */
    public RequestParameters(String queryString) {
        this.values = new HashMap(5);

        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String pair = st.nextToken();
                int pos = pair.indexOf('=');
                if (pos != -1) {
                    setParameter(decode(pair.substring(0, pos)),
                                 decode(pair.substring(pos + 1, pair.length())));
                } else {
                    setParameter(decode(pair), "");
                }
            }
        }
    }

    /**
     * Add a parameter.
     * The parameter is added with the given value.
     *
     * @param name   The name of the parameter.
     * @param value  The value of the parameter.
     */
    private void setParameter(String name, String value) {
        String[] values = (String[]) this.values.get(name);
        if (values == null) {
            values = new String[] { value };
        } else {
            String[] v2 = new String[values.length + 1];
            System.arraycopy(values, 0, v2, 0, values.length);
            v2[values.length] = value;
            values = v2;
        }

        this.values.put(name, values);
    }

    /**
     * Get the value of a parameter.
     *
     * @param name   The name of the parameter.
     * @return       The value of the first parameter with the name
     *               or <CODE>null</CODE>
     */
    public String getParameter(String name) {
        String[] values = (String[]) this.values.get(name);
        if (values != null) {
            return values[0];
        }

        return null;
    }

    /**
     * Get the value of a parameter.
     *
     * @param name   The name of the parameter.
     * @param defaultValue The default value if the parameter does not exist.
     * @return       The value of the first parameter with the name
     *               or <CODE>defaultValue</CODE>
     */
    public String getParameter(String name, String defaultValue) {
        String[] values = (String[]) this.values.get(name);
        if (values != null) {
            return values[0];
        }

        return defaultValue;
    }

    /**
     * Get all values of a parameter.
     *
     * @param name   The name of the parameter.
     * @return       Array of the (String) values or null if the parameter
     *               is not defined.
     */
    public String[] getParameterValues(String name) {
        return (String[]) values.get(name);
    }

    /**
     * Get all parameter names.
     *
     * @return  Enumeration for the (String) parameter names.
     */
    public Enumeration getParameterNames() {
        return new IteratorEnumeration(values.keySet().iterator());
    }

    /**
     * Get map of parameter names to String array values
     *
     * @return map of parameter values
     */
    public Map getParameterMap() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * Decodes URL encoded string. Supports decoding of '+', '%XX' encoding,
     * and '%uXXXX' encoding.
     *
     * @param s URL encoded string
     * @return decoded string
     */
    private static String decode(String s) {
        byte[] decoded = new byte[s.length() / 3 + 1];
        int decodedLength = 0;

        final int length = s.length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; ) {
            char c = s.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    i++;
                    break;

                case '%':
                    // Check if this is a non-standard %u9999 encoding
                    // (see COCOON-1950)
                    try {
                        if (s.charAt(i + 1) == 'u') {
                            sb.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
                            i += 6;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid query string. " +
                                                           "Illegal hex characters in pattern %" + s.substring(i + 1, i + 6));
                    } catch (StringIndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("Invalid query string. " +
                                                           "% character should be followed by 2 hexadecimal characters.");
                    }

                    // Process sequence of %XX encoded bytes in one go since several bytes can represent
                    // single character.
                    while (i < length && s.charAt(i) == '%') {
                        if (i + 2 >= length) {
                            throw new IllegalArgumentException("Invalid query string. " +
                                                               "% character should be followed by 2 hexadecimal characters.");
                        }

                        // If found %u9999, rollback '%' and finish this loop
                        if (s.charAt(i + 1) == 'u') {
                            i--;
                            break;
                        }

                        try {
                            decoded[decodedLength++] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Invalid query string. " +
                                                               "Illegal hex characters in pattern %" + s.substring(i + 1, i + 3));
                        }
                        i += 3;
                    }
                    if (decodedLength > 0) {
                        try {
                            sb.append(new String(decoded, 0, decodedLength, "UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            // The situation that UTF-8 is not supported is quite theoretical, so throw a runtime exception
                            throw new RuntimeException("Problem in decode: UTF-8 encoding not supported.");
                        }
                        decodedLength = 0;
                    }

                    break;

                default:
                    sb.append(c);
                    i++;
                    break;
            }
        }

        return sb.toString();
    }
}
