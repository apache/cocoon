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
package org.apache.cocoon.webapps.session;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.xml.sax.SAXException;

/**
 *  This is the context manager.
 *
 *  The main purpose of this component is maintaining contexts. Each
 *  application can have one or more session contexts.
 *  A context is a data container that can hold arbitrary information.
 *  The contained information can either be an XML tree or custom
 *  objects.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ContextManager.java,v 1.3 2004/03/05 13:02:22 bdelacretaz Exp $
*/
public interface ContextManager {

    /** Avalon role */
    String ROLE = ContextManager.class.getName();;

    /**
     *  Create a new public context in the session.
     *  Create a new public session context for this user. If this context
     *  already exists no new context is created and the old one will be used
     *  instead.
     */
    SessionContext createContext(String name, String loadURI, String saveURI)
    throws IOException, SAXException, ProcessingException;

    /**
     *  Delete a public context in the session.
     *  If the context exists for this user, it and all of its information
     *  is deleted.
     */
    void deleteContext(String name)
    throws ProcessingException;

    /**
     *  Get a public context.
     *  The session context with the given name is returned. If the context does
     *  not exist <CODE>null</CODE> is returned.
     */
    SessionContext getContext(String name)
    throws ProcessingException;

    /**
     * Check if a context exists
     */
    boolean hasSessionContext() 
    throws ProcessingException;

    /**
     *  Check if a public context exists.
     *  If the session context with the given name exists, <CODE>true</CODE> is
     *  returned.
     */
    boolean existsContext(String name) 
    throws ProcessingException;
}
