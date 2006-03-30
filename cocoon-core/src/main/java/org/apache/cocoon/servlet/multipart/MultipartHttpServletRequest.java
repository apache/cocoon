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
package org.apache.cocoon.servlet.multipart;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Servlet request wrapper for multipart parser.
 *
 * @version $Id$
 */
public class MultipartHttpServletRequest extends HttpServletRequestWrapper {

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
     * Method get
     *
     * @param name
     *
     */
    public Object get(String name) {
        Object result = null;

        if (values != null) {
            result = values.get(name);

            if (result instanceof Vector) {
                if (((Vector) result).size() == 1) {
                    return ((Vector) result).elementAt(0);
                }
                return result;
            }
        } else {
            String[] array = this.getRequest().getParameterValues(name);
            Vector vec = new Vector();

            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    vec.addElement(array[i]);
                }

                if (vec.size() == 1) {
                    result = vec.elementAt(0);
                } else {
                    result = vec;
                }
            }
        }

        return result;
    }

    /**
     * Method getParameterNames
     *
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
     *
     */
    public String getParameter(String name) {
        if (values != null) {
            Object value = get(name);
            String result = null;
    
            if (value != null) {
                if (value instanceof Vector) {
                    value = ((Vector) value).elementAt(0);
                }
    
                result = value.toString();
            }
            return result;
        } else {
            return super.getParameter(name);
        }
    }

    /**
     * Method getParameterValues
     *
     * @param name
     *
     */
    public String[] getParameterValues(String name) {
        if (values != null) {
            Object value = get(name);

            if (value != null) {
                if (value instanceof Vector) {
                    String[] results = new String[((Vector)value).size()];
                    for (int i=0;i<((Vector)value).size();i++) {
                        results[i] = ((Vector)value).elementAt(i).toString();
                    }
                    return results;

                }
                return new String[]{value.toString()};
            }

            return null;
        }
        return this.getRequest().getParameterValues(name);
    }

}
