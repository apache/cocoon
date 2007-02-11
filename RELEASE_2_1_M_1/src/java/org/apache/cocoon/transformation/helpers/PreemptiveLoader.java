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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;

/**
 * The preemptive loader is a singleton that runs in the background
 * and loads content into the cache.
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: PreemptiveLoader.java,v 1.3 2003/03/12 15:35:11 cziegeler Exp $
 *  @since   2.1
 */
public final class PreemptiveLoader {

    private static final PreemptiveLoader instance = new PreemptiveLoader();
    
    /** The list of proxies currently used for caching */
    private Map   cacheStorageProxyMap = new HashMap(20);
    /** The list of URIs to load */
    private List  loadList = new ArrayList(50);
    /** Is this thread still alive? */
    boolean alive = false;
    
    /**
     * Return singleton.
     * @return PreemptiveLoader
     */
    static PreemptiveLoader getInstance() {
        return instance;
    }
    
    /**
     * Add a new task
     * @param proxy   The cache to store the content
     * @param uri     The absolute URI to load
     * @param expires The expires information used for the cache
     */
    public void add(IncludeCacheStorageProxy proxy, String uri, long expires) {
        boolean addItem = true;
        List uriList = (List)this.cacheStorageProxyMap.get(proxy);
        if ( null == uriList ) {
             uriList = new ArrayList(50);
             this.cacheStorageProxyMap.put(proxy, uriList);
        } else {
            synchronized (uriList) {
                // nothing to do: uri is alredy in list
               if (uriList.contains(uri)) {
                   addItem = false;
               } 
            }
        }
        if ( addItem ) {
            uriList.add(uri);
            this.loadList.add(new Object[] {proxy, uri, new Long(expires), uriList});
        }

        synchronized (this.cacheStorageProxyMap) {
            this.cacheStorageProxyMap.notify();
        }
    }
    
    /**
     * Start the preemptive loading
     * @param manager   A component manager
     * @param resolver  A source resolver
     * @param logger    A logger
     */
    public void process(ComponentManager manager,
                         SourceResolver  resolver,
                         Logger          logger) {
        this.alive = true;
        if (logger.isDebugEnabled()) {
            logger.debug("PreemptiveLoader: Starting preemptive loading");
        }

        while (this.alive) {
            while (this.loadList.size() > 0) {
                Object[] object = (Object[])this.loadList.get(0);
                final String uri = (String)object[1];
                this.loadList.remove(0);
                synchronized (object[3]) {
                    ((List)object[3]).remove(uri);
                }
                
                Source source = null;
                XMLSerializer serializer = null;

                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("PreemptiveLoader: Loading " + uri);
                    }

                    source = resolver.resolveURI(uri);
                    serializer = (XMLSerializer)manager.lookup(XMLSerializer.ROLE);
                
                    SourceUtil.toSAX(source, serializer);
                
                    SourceValidity[] validities = new SourceValidity[1];
                    validities[0] = new ExpiresValidity(((Long)object[2]).longValue() * 1000); // milliseconds!
                    CachedResponse response = new CachedResponse(validities,
                                                                 (byte[])serializer.getSAXFragment());
                    ((IncludeCacheStorageProxy)object[0]).put(uri, response);
                     
                } catch (Exception ignore) {
                    // all exceptions are ignored!
                } finally {
                    resolver.release( source );
                    manager.release( serializer );
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("PreemptiveLoader: Finished loading " + uri);
                }
            }
            synchronized (this.cacheStorageProxyMap) {
                try {
                    this.cacheStorageProxyMap.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("PreemptiveLoader: Finished preemptive loading");
        }
    }
    
    /**
     * Stop the loading. 
     * The loader stops when all tasks from the queue are processed.
     */
    synchronized public void stop() {
        this.alive = false;
        synchronized (this.cacheStorageProxyMap) {
            this.cacheStorageProxyMap.notify();
        }
    }
}
