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
package org.apache.cocoon.servlet.multipart;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.cocoon.environment.ValueHolder;

/**
 * Servlet request wrapper for multipart parser.
 *
 * @version $Id$
 */
public class MultipartHttpServletRequest extends HttpServletRequestWrapper implements ValueHolder {

    /** The submitted parts */
    private Hashtable values;

    /**
     * Create this wrapper around the given request and including the given
     * parts.
     */
    public MultipartHttpServletRequest(HttpServletRequest request, Hashtable values) {
        super(request);
        this.values = values;
    }

    /**
     * Cleanup eventually uploaded parts that were saved on disk
     */
    public void cleanup() throws IOException {
        Enumeration e = getParameterNames();
        while (e.hasMoreElements()) {
            Object o = get( (String)e.nextElement() );
            if (o instanceof Part) {
                Part part = (Part)o;
                if (part.disposeWithRequest()) {
                    part.dispose();
                }
            }
        }
    }

    /**
     * @see org.apache.cocoon.environment.ValueHolder#get(java.lang.String)
     */
    public Object get(String name) {
        Object result = null;

        if (values != null) {
            result = values.get(name);

            if (result instanceof Vector) {
                Vector v = (Vector) result;
                if (v.size() == 1) {
                    return v.elementAt(0);
                }
                return result;
            }
        } else {
            String[] array = this.getRequest().getParameterValues(name);

            if (array != null) {
                if (array.length == 1) {
                    result = array[0];
                } else {
                    Vector vec = new Vector();
                    for (int i = 0; i < array.length; i++) {
                        vec.addElement(array[i]);
                    }
                    result = vec;
                }
            }
        }

        return result;
    }

    /**
     * Method getParameterNames
     */
    public Enumeration getParameterNames() {
        if (values != null) {
            return values.keys();
        }
        return this.getRequest().getParameterNames();
    }

    /**
     * Method getParameter
     *
     * @param name
     */
    public String getParameter(String name) {
        String result = null;

        Object value = get(name);
        if (value instanceof Vector && !((Vector)value).isEmpty()) {
            value = ((Vector) value).elementAt(0);
        }

        if (value != null) {
            result = value.toString();
        }

        return result;
    }

    /**
     * Method getParameterValues
     *
     * @param name
     */
    public String[] getParameterValues(String name) {
        // null check and so else path are just optimizations
        if (values != null) {
            Object value = get(name);

            if (value == null) {
                return null;
            } else if (value instanceof Vector) {
                Vector v = (Vector)value;
                String[] results = new String[v.size()];
                for (int i = 0; i < v.size(); i++) {
                    results[i] = v.elementAt(i).toString();
                }
                return results;
            }
            return new String[]{value.toString()};
        }
        return this.getRequest().getParameterValues(name);
    }

}
