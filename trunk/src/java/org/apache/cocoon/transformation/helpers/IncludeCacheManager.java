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
 *  @version CVS $Id: IncludeCacheManager.java,v 1.2 2003/03/11 16:33:37 vgritsenko Exp $
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
