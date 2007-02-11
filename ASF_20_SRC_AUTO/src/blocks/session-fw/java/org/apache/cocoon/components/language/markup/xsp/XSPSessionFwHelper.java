/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;

import org.apache.cocoon.ProcessingException;

import org.apache.cocoon.webapps.session.SessionManager;

import org.w3c.dom.DocumentFragment;

/**
 * The <code>Session-fw</code> object helper
 *
 * @author <a href="mailto:antonio@apache.org">Antonio Gallardo</a>
 * @version CVS $Id: XSPSessionFwHelper.java,v 1.8 2004/03/05 13:02:22 bdelacretaz Exp $
 * @since 2.1.1
 */
public class XSPSessionFwHelper {

    /** GetXML Fragment from the given session context and path
     *
     *
     * @param cm The ComponentManager
     * @param context The Session context tha define where to search
     * @param path The parameter path
    **/
    public static DocumentFragment getXML(ComponentManager cm, String context, String path) throws ProcessingException {

        SessionManager sessionManager = null;
        try {
            // Start looking up the manager
            sessionManager = (SessionManager)cm.lookup(SessionManager.ROLE);
            // Get the fragment
            DocumentFragment df = sessionManager.getContextFragment(context, path);
            return df;
        } catch (ComponentException ce) {
            throw new ProcessingException("Error during lookup of SessionManager component.", ce);
        } finally {
            // End releasing the sessionmanager
		    cm.release((Component)sessionManager);
	    }
     }
    
    /** GetXML Fragment from the given session context and path
     *
     *
     * @param cm The ComponentManager
     * @param context The Session context tha define where to search
     * @param path The parameter path
     **/
    public static String getXMLAsString(ComponentManager cm, String context, String path) throws ProcessingException {
        DocumentFragment df = getXML(cm, context, path);
        return df != null ? df.getFirstChild().getNodeValue() : "";
    }
}

