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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.ProcessingException;

import org.apache.cocoon.webapps.session.SessionManager;

import org.w3c.dom.DocumentFragment;

/**
 * The <code>Session-fw</code> object helper
 *
 * @version $Id$
 * @deprecated This class is deprecated and will be removed in future versions.
 * @since 2.1.1
 */
public class XSPSessionFwHelper {

    /** GetXML Fragment from the given session context and path
     *
     *
     * @param cm The ServiceManager
     * @param context The Session context tha define where to search
     * @param path The parameter path
    **/
    public static DocumentFragment getXML(ServiceManager cm, String context, String path) throws ProcessingException {

        SessionManager sessionManager = null;
        try {
            // Start looking up the manager
            sessionManager = (SessionManager)cm.lookup(SessionManager.ROLE);
            // Get the fragment
            DocumentFragment df = sessionManager.getContextFragment(context, path);
            return df;
        } catch (ServiceException ce) {
            throw new ProcessingException("Error during lookup of SessionManager component.", ce);
        } finally {
            // End releasing the sessionmanager
		    cm.release(sessionManager);
	    }
     }
    
    /** GetXML Fragment from the given session context and path
     *
     *
     * @param cm The ServiceManager
     * @param context The Session context tha define where to search
     * @param path The parameter path
     **/
    public static String getXMLAsString(ServiceManager cm, String context, String path) throws ProcessingException {
        DocumentFragment df = getXML(cm, context, path);
        return df != null ? df.getFirstChild().getNodeValue() : "";
    }
}

