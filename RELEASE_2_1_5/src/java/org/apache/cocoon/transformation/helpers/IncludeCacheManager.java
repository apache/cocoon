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
package org.apache.cocoon.transformation.helpers;

import java.io.IOException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.SAXException;

/**
 * The include cache manager is a component that can manage included content.
 * It can eiter load them in parallel or pre-emptive and cache the content
 * for a given period of time.
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: IncludeCacheManager.java,v 1.3 2004/03/05 13:03:00 bdelacretaz Exp $
 *  @since   2.1
 */
public interface IncludeCacheManager {

    /** Avalon role */
    String ROLE = IncludeCacheManager.class.getName();
    
    /**
     * Create a session for this request.
     * This should be invoked first and only one per request. It is required
     * to terminate the session with {@link #terminateSession(IncludeCacheManagerSession)}
     * @param pars The configuration
     * @return CacheManagerSession The session that should be used with all other commands.
     */
    IncludeCacheManagerSession getSession(Parameters pars);
    
    /**
     * This informs the manager that a URI should be "loaded".
     * @param uri     The URI to load (maybe relative)
     * @param session The corresponding session created by {@link #getSession(Parameters)}
     * @return String The absolute URI that must be used for {@link #stream(String, IncludeCacheManagerSession, XMLConsumer)}
     * @throws IOException
     * @throws SourceException
     */
    String load(String  uri, 
                IncludeCacheManagerSession session)
    throws IOException, SourceException;
              
    /**
     * Stream the content of the absolute URI.
     * Depending on the configuration and state of the cache, the
     * content is either taken from the cache, fetched etc.
     * @param uri     The absolute URI returned by {@link #load(String, IncludeCacheManagerSession)}
     * @param session The current session
     * @param handler The receiver of the SAX events
     * @throws IOException
     * @throws SourceException
     * @throws SAXException
     */
    void stream(String uri,
                 IncludeCacheManagerSession session,
                 XMLConsumer handler)
    throws IOException, SourceException, SAXException;
                 
    /**
     * Terminate the session. This method must be executed at the end of the
     * request.
     * @param session The caching session.
     */
    void terminateSession(IncludeCacheManagerSession session);
}
