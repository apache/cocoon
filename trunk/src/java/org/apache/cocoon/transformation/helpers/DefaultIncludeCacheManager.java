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
import java.net.URL;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.components.sax.XMLTeePipe;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;
import org.xml.sax.SAXException;

/**
 * Default implementation of a {@link CacheManager}.
 * 
 * This implementation requires a configuration, if preemptive
 * loading is used:
 * &lt;parameter name="preemptive-loader-url" value="some url"/&gt;
 * 
 * This is a url inside cocoon, that contains the preemptive loader
 * url; it must be specified absolute (with http://...)
 * If this loader cannot be started, only an error is logged into the
 * log, so actually cached content is never updated!
 * 
 *  @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *  @version CVS $Id: DefaultIncludeCacheManager.java,v 1.1 2003/03/09 00:09:41 pier Exp $
 *  @since   2.1
 */
public final class DefaultIncludeCacheManager
    extends AbstractLogEnabled
    implements IncludeCacheManager, 
                ThreadSafe, 
                Composable, 
                Disposable,
                Parameterizable, 
                Component {

    private ComponentManager manager;
    
    private SourceResolver   resolver;
    
    private Store            store;
    
    private IncludeCacheStorageProxy defaultCacheStorage;
    
    private String            preemptiveLoaderURI;
    
    /**
     * @see de.sundn.prod.sunshine.transformation.CacheManager#getSession(org.apache.avalon.framework.parameters.Parameters)
     */
    public IncludeCacheManagerSession getSession(Parameters pars) {
        String sourceURI = pars.getParameter("source", null);
        IncludeCacheManagerSession session;
        if ( null == sourceURI ) {
            session = new IncludeCacheManagerSession(pars, this.defaultCacheStorage);
        } else {
            Source source = null;
            try {
                source = this.resolver.resolveURI(sourceURI);
                IncludeCacheStorageProxy proxy = new ModifiableSourceIncludeCacheStorageProxy(this.resolver, source.getURI(), this.getLogger());
                session = new IncludeCacheManagerSession(pars, proxy);
            } catch (Exception local) {
                session = new IncludeCacheManagerSession(pars, this.defaultCacheStorage);
                this.getLogger().warn("Error creating writeable source.", local);
            } finally {
                this.resolver.release(source);
            }
        }
        if (session.isPreemptive()) {
            if ( null == this.preemptiveLoaderURI ) {
                this.getLogger().error("Preemptive loading is turned off because the preemptive-loader-url is not configured.");
                session.setPreemptive(false);
            } else {
                if ( !PreemptiveLoader.getInstance().alive ) {

                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Booting preemptive loader: " + this.preemptiveLoaderURI);
                    }
                    PreemptiveBooter thread = new PreemptiveBooter();
                    thread.setURI(this.preemptiveLoaderURI);
                    thread.start();
                    Thread.yield();
                }
            }
        } 
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Creating cache manager session: " + session);
        }
        return session;
    }

    /**
     * @see de.sundn.prod.sunshine.transformation.CacheManager#load(java.lang.String, boolean, de.sundn.prod.sunshine.transformation.CacheManagerSession)
     */
    public String load(String uri,
                        IncludeCacheManagerSession session) 
    throws IOException, SourceException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Load " + uri + " for session " + session);
        }
        
        // first make the URI absolute
        if ( uri.indexOf("://") == -1) {
            final Source source = session.resolveURI(uri, this.resolver);
            uri = source.getURI();
        }
        
        // if we are not processing in parallel (or do preemptive)
        // then we don't have to do anything in this method - everything
        // is done in the stream method.
        
        // if we are processing in parallel (and not preemptive) then....
        if ( session.isParallel() && !session.isPreemptive()) {
            
            // first look-up if we have a valid stored response
            IncludeCacheStorageProxy storage = session.getCacheStorageProxy();
            CachedResponse response = (CachedResponse)storage.get(uri);
            if ( null != response) {
                SourceValidity[] validities = response.getValidityObjects();
                
                // if we are valid and do not purging
                if ( !session.isPurging() 
                      && validities[0].isValid() == SourceValidity.VALID) {
                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Using cached response for parallel processing.");
                    }
                    session.add(uri, response.getResponse());
                    return uri;
                } else {
                    // response is not used
                    storage.remove(uri);
                }
            }

            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Starting parallel thread for loading " + uri);
            }
            // now we start a parallel thread, this thread gets all required avalon components
            // so it does not have to lookup them by itself
            try {
                XMLSerializer serializer = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
                Source source = session.resolveURI(uri, this.resolver);

                LoaderThread loader = new LoaderThread(source, serializer, this.manager);
                Thread thread = new Thread(loader);
                session.add(uri, loader);
                thread.start();
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Thread started for " + uri);
                }
            } catch (ComponentException ce) {
                throw new SourceException("Unable to lookup thread pool or xml serializer.", ce);
            } catch (Exception e) {
                throw new SourceException("Unable to get pooled thread.", e);
            }
        }
        return uri;
    }

    /**
     * @see de.sundn.prod.sunshine.transformation.CacheManager#stream(java.lang.String, de.sundn.prod.sunshine.transformation.CacheManagerSession, org.xml.sax.ContentHandler)
     */
    public void stream(String uri,
                        IncludeCacheManagerSession session,
                        XMLConsumer handler) 
    throws IOException, SourceException, SAXException {

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Stream " + uri + " for session " + session);
        }

        // if we are processing in parallel (and not preemptive) then....
        if ( session.isParallel() && !session.isPreemptive()) {
            
            // get either the cached content or the pooled thread
            Object object = session.get(uri);
            
            if ( null == object ) {
                // this should never happen!
                throw new SAXException("No pooled thread found for " + uri);
            }
            byte[] result;
            
            // is this a pooled thread?
            if (object instanceof LoaderThread) {
                LoaderThread loader = (LoaderThread)object;
                
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Waiting for pooled thread to finish loading.");
                }

                // wait
                while (!loader.finished) {                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                }

                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Pooled thread finished loading.");
                }
                
                // did an exception occur? Then reraise it
                if ( null != loader.exception) {
                    if ( loader.exception instanceof SAXException ) {
                        throw (SAXException)loader.exception;
                    } else if (loader.exception instanceof SourceException ) {
                        throw (SourceException)loader.exception;
                    } else if (loader.exception instanceof IOException) {
                        throw (IOException)loader.exception;
                    } else {
                        throw new SAXException("Exception.", loader.exception);
                    }
                }
                
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Streaming from pooled thread.");
                }
                result = loader.content;

                // cache the response (remember preemptive is off)
                if (session.getExpires() > 0) {
                    SourceValidity[] validities = new SourceValidity[1];
                    validities[0] = session.getExpiresValidity();
                    CachedResponse response = new CachedResponse(validities, result);
                    session.getCacheStorageProxy().put(uri, response);
                }
            } else {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Streaming from cached response.");
                }

                // use the response from the cache
                result = (byte[])object;
            }
            
            // stream the content
            XMLDeserializer deserializer = null;
            try {
                deserializer = (XMLDeserializer)this.manager.lookup( XMLDeserializer.ROLE );
                deserializer.setConsumer(handler);
                deserializer.deserialize(result);
            } catch (ComponentException ce) {
                throw new SAXException("Unable to lookup xml deserializer.", ce);
            } finally {
                this.manager.release( deserializer );
            }
            return;
            
        } else {
            // we are not processing parallel
            
            // first: test for a cached response
            IncludeCacheStorageProxy storage = session.getCacheStorageProxy();
            CachedResponse response = (CachedResponse)storage.get(uri);
            if ( null != response) {
                SourceValidity[] validities = response.getValidityObjects();
                // if purging is turned off and either the cached response is valid or
                // we are loading preemptive, then use the cached response
                if ( !session.isPurging() 
                      && (session.isPreemptive() || validities[0].isValid() == SourceValidity.VALID)) {

                    // stream the content                    
                    if (this.getLogger().isDebugEnabled()) {
                        this.getLogger().debug("Streaming from cached response.");
                    }
                    XMLDeserializer deserializer = null;
                    try {
                        deserializer = (XMLDeserializer)this.manager.lookup( XMLDeserializer.ROLE );
                        deserializer.setConsumer(handler);
                        deserializer.deserialize(response.getResponse());
                    } catch (ComponentException ce) {
                        throw new SAXException("Unable to lookup xml deserializer.", ce);
                    } finally {
                        this.manager.release( deserializer );
                    }
                    
                    // load preemptive if the response is not valid
                    if ( session.getExpires() > 0
                         && session.isPreemptive() 
                         && validities[0].isValid() != SourceValidity.VALID) {
                        if (this.getLogger().isDebugEnabled()) {
                            this.getLogger().debug("Add uri to preemptive loader list " + uri);
                        }
                        if (!PreemptiveLoader.getInstance().alive) {
                            this.getLogger().error("Preemptive loader has not started yet.");
                        }
                        PreemptiveLoader.getInstance().add(session.getCacheStorageProxy(), uri, session.getExpires());
                    }
                    return;
 
                } else {
                    // cached response is not valid
                    storage.remove(uri);
                }
            }
        }

        // we are not processing in parallel and have no (valid) cached response
        XMLSerializer serializer = null;
        try {
            final Source source = session.resolveURI(uri, this.resolver);
            
            // stream directly (and cache the response)
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Streaming directly from source.");
            }
            if (session.getExpires() > 0) {
                serializer = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
                XMLTeePipe tee = new XMLTeePipe(handler, serializer);
                
                SourceUtil.toSAX(source, tee);
                
                SourceValidity[] validities = new SourceValidity[1];
                validities[0] = session.getExpiresValidity();
                CachedResponse response = new CachedResponse(validities,
                                                             (byte[])serializer.getSAXFragment());
                session.getCacheStorageProxy().put(uri, response);
            } else {
                SourceUtil.toSAX(source, handler);
            }
            
        } catch (ProcessingException pe) {
            throw new SAXException("ProcessingException", pe);
        } catch (ComponentException e) {
            throw new SAXException("Unable to lookup xml serializer.", e);
        }
    }

    /**
     * @see de.sundn.prod.sunshine.transformation.CacheManager#terminateSession(de.sundn.prod.sunshine.transformation.CacheManagerSession)
     */
    public void terminateSession(IncludeCacheManagerSession session) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Terminating cache manager session " + session);
        }
        session.cleanup(this.resolver);
    }

    /**
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
        this.store = (Store)this.manager.lookup(Store.ROLE);
        this.defaultCacheStorage = new StoreIncludeCacheStorageProxy(this.store, this.getLogger());
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // stop preemptive loader (if running)
        PreemptiveLoader.getInstance().stop();
        if ( null != this.manager ) {
            this.manager.release( this.resolver);
            this.manager.release(this.store);
            this.store = null;
            this.resolver = null;
            this.manager = null;
            this.defaultCacheStorage = null;
        }
    }

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
        this.preemptiveLoaderURI = parameters.getParameter("preemptive-loader-url", null);
        if ( null != this.preemptiveLoaderURI 
             && this.preemptiveLoaderURI.indexOf("://") == -1) {
            throw new ParameterException("The preemptive-loader-url must be absolute: " + this.preemptiveLoaderURI);
        }
    }

}

final class LoaderThread implements Runnable {
    
    private  Source source;
    private  XMLSerializer serializer;
    private  ComponentManager manager;
    boolean  finished;
    Exception exception;
    byte[]    content;

    public LoaderThread(Source source, 
                         XMLSerializer serializer, 
                         ComponentManager manager) {
        this.source = source;
        this.serializer = serializer;
        this.manager = manager;
        this.finished = false;
    }
    
    public void run() {
        try {
            SourceUtil.toSAX(this.source, this.serializer);
            this.content = (byte[])this.serializer.getSAXFragment();
        } catch (Exception local) {
            this.exception = local;
        } finally {
            this.finished = true;
        }
    }
    
}

final class PreemptiveBooter extends Thread {

    private String uri;
    
    void setURI(String uri) {
        this.uri = uri;
    }
    
    public void run() {
        try {
            URL url = new URL(this.uri);
            url.getContent();
        } catch (Exception ignore) {
        }
    }
}