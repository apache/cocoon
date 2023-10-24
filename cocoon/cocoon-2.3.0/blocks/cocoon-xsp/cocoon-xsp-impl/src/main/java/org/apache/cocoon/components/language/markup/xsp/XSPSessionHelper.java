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
package org.apache.cocoon.components.language.markup.xsp;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * The <code>Session</code> object helper
 *
 * @version $Id$
 */
public class XSPSessionHelper {

    /**
     * Return the given session attribute value or a user-provided default if
     * none was specified.
     *
     * @param session The Session object
     * @param name The parameter name
     * @param defaultValue Value to substitute in absence of a parameter value
     */
    public static Object getSessionAttribute(HttpSession session, String name,
                                             Object defaultValue) {
        Object value = null;
        if (session != null) {
            value = session.getAttribute(name);
        }

        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Get the session attribute names.
     *
     * @param session The Session object
     */
    public static List getSessionAttributeNames(HttpSession session) {
        ArrayList v = new ArrayList();
        if (session == null) {
            return v;
        }
        Enumeration e = session.getAttributeNames();
        while (e.hasMoreElements()) {
            v.add(e.nextElement());
        }
        return v;
    }
}
