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
package org.apache.cocoon.environment;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

/**
 * Base interface for an environment abstraction
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Environment.java,v 1.12 2004/02/05 13:58:51 cziegeler Exp $
 */
public interface Environment {

    /**
     * Get the URI to process. The prefix is stripped off.
     */
    String getURI();

    /**
     * Get the URI prefix
     */
    String getURIPrefix();
    
    /**
     * Set the URI and the prefix to process.
     */
    void setURI(String prefix, String value);
    
    /**
     * Get the view to process
     */
    String getView();

    /**
     * Get the action to process
     */
    String getAction();

    /**
     * Redirect to the given URL
     */
    void redirect(String url, boolean global, boolean permanent) 
    throws IOException;

    /**
     * Set the content type of the generated resource
     */
    void setContentType(String mimeType);

    /**
     * Get the content type of the resource
     */
    String getContentType();

    /**
     * Set the length of the generated content
     */
    void setContentLength(int length);

    /**
     * Set the response status code
     */
    void setStatus(int statusCode);

    /**
     * Get the output stream where to write the generated resource.
     * The returned stream is buffered by the environment. If the
     * buffer size is -1 then the complete output is buffered.
     * If the buffer size is 0, no buffering takes place.
     */
    OutputStream getOutputStream(int bufferSize) throws IOException;

    /**
     * Get the underlying object model
     */
    Map getObjectModel();

    /**
     * Check if the response has been modified since the same
     * "resource" was requested.
     * The caller has to test if it is really the same "resource"
     * which is requested.
     * @return true if the response is modified or if the
     *         environment is not able to test it
     */
    boolean isResponseModified(long lastModified);

    /**
     * Mark the response as not modified.
     */
    void setResponseIsNotModified();

    /**
     * Binds an object to this environment, using the name specified. This allows
     * the pipeline assembly engine to store for its own use objects that souldn't
     * be exposed to other components (generators, selectors, etc) and therefore
     * cannot be put in the object model.
     * <p>
     * If an object of the same name is already bound, the object is replaced.
     *
     * @param name  the name to which the object is bound
     * @param value the object to be bound
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the object bound with the specified name, or <code>null</code>
     * if no object is bound under the name.
     *
     * @param name                a string specifying the name of the object
     * @return                    the object with the specified name
     */
    Object getAttribute(String name);

    /**
     * Removes the object bound with the specified name from
     * this environment. If the environment does not have an object
     * bound with the specified name, this method does nothing.
     *
     * @param name the name of the object to remove
     */
    void removeAttribute(String name);

    /**
     * Returns an <code>Enumeration</code> of <code>String</code> objects
     * containing the names of all the objects bound to this environment.
     *
     * @return an <code>Enumeration</code> of <code>String</code>s.
     */
    Enumeration getAttributeNames();

    /**
     * Reset the response if possible. This allows error handlers to have
     * a higher chance to produce clean output if the pipeline that raised
     * the error has already output some data.
     * If a buffered output stream is used, resetting is always successful.
     *
     * @return true if the response was successfully reset
     */
    boolean tryResetResponse() throws IOException;


    /**
     * Commit the response
     */
    void commitResponse() throws IOException;
    
    /**
     * Notify that the processing starts.
     */
    void startingProcessing();
    
    /**
     * Notify that the processing is finished
     * This can be used to cleanup the environment object
     */
    void finishingProcessing();
    
    /**
     * Is this environment external ? An external environment is one that is created in response
     * to an external request (http, commandline, etc.). Environments created by the "cocoon:"
     * protocol aren't external.
     * 
     * @return true is this environment is external
     */
    boolean isExternal();
}

