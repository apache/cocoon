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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.environment.Session;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * The <code>Session</code> object helper
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: XSPSessionHelper.java,v 1.2 2004/03/05 13:02:47 bdelacretaz Exp $
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
    public static Object getSessionAttribute(Session session, String name,
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
    public static List getSessionAttributeNames(Session session) {
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
