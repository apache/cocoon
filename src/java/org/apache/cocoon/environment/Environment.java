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
 * @version CVS $Id: Environment.java,v 1.9 2004/05/25 07:28:24 cziegeler Exp $
 */
public interface Environment {

    /**
     * Get the URI to process. The prefix is stripped off.
     */
    String getURI();

    /**
     * Get the prefix of the URI in progress.
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
     * Is this environment external ? An external environment is one that 
     * is created in response to an external request (http, commandline, etc.). 
     * Environments created by the "cocoon:" protocol aren't external.
     * 
     * @return true if this environment is external
     */
    boolean isExternal();
    
    /**
     * Is this an internal redirect?
     * An environment is on internal redirect if it is an internal request
     * (via the cocoon: protocol) and used for a redirect.
     */
    boolean isInternalRedirect();
}

