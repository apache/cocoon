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
package org.apache.cocoon.webapps.session;

import java.io.IOException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.webapps.session.context.SessionContextProvider;
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
 * @version CVS $Id: ContextManager.java,v 1.1 2003/05/04 20:19:41 cziegeler Exp $
*/
public interface ContextManager {

    /** Avalon role */
    String ROLE = ContextManager.class.getName();;

    /**
     * Add a context provider.
     */
    void addSessionContextProvider(SessionContextProvider provider,
                                  String                 contextName)
    throws ProcessingException;

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
