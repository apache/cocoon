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
import java.net.URL;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
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
 * Default implementation of a {@link IncludeCacheManager}.
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
 *  @version CVS $Id: DefaultIncludeCacheManager.java,v 1.8 2004/03/18 07:42:12 cziegeler Exp $
 *  @since   2.1
 */
public final class DefaultIncludeCacheManager
    extends AbstractLogEnabled
    implements IncludeCacheManager, 
                ThreadSafe, 
                Serviceable, 
                Disposable,
                Parameterizable, 
                Component {

    private ServiceManager manager;
    
    private SourceResolver   resolver;
    
    private Store            store;
    
    private IncludeCacheStorageProxy defaultCacheStorage;
    
    private String            preemptiveLoaderURI;
    
    /**
     * @see IncludeCacheManager#getSession(org.apache.avalon.framework.parameters.Parameters)
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
     * @see IncludeCacheManager#load(java.lang.String, IncludeCacheManagerSession)
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
            } catch (ServiceException ce) {
                throw new SourceException("Unable to lookup thread pool or xml serializer.", ce);
            } catch (Exception e) {
                throw new SourceException("Unable to get pooled thread.", e);
            }
        }
        return uri;
    }

    /**
     * @see IncludeCacheManager#stream(java.lang.String, IncludeCacheManagerSession, XMLConsumer)
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
            } catch (ServiceException ce) {
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
                    } catch (ServiceException ce) {
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
        } catch (ServiceException e) {
            throw new SAXException("Unable to lookup xml serializer.", e);
        } finally {
            this.manager.release(serializer);
        }
    }

    /**
     * @see IncludeCacheManager#terminateSession(IncludeCacheManagerSession)
     */
    public void terminateSession(IncludeCacheManagerSession session) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("Terminating cache manager session " + session);
        }
        session.cleanup(this.resolver);
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
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
        final String storeRole = parameters.getParameter("use-store", Store.ROLE);
        try {
            this.store = (Store)this.manager.lookup(storeRole);
        } catch (ServiceException e) {
            throw new ParameterException("Unable to lookup store with role " + storeRole, e);
        }
        this.defaultCacheStorage = new StoreIncludeCacheStorageProxy(this.store, this.getLogger());
    }

}

final class LoaderThread implements Runnable {
    
    private  Source source;
    private  XMLSerializer serializer;
    boolean  finished;
    Exception exception;
    byte[]    content;
    ServiceManager manager;
    
    public LoaderThread(Source source, 
                        XMLSerializer serializer,
                        ServiceManager manager) {
        this.source = source;
        this.serializer = serializer;
        this.finished = false;
        this.manager = manager;
    }
    
    public void run() {
        try {
            SourceUtil.toSAX(this.source, this.serializer);
            this.content = (byte[])this.serializer.getSAXFragment();
        } catch (Exception local) {
            this.exception = local;
        } finally {
            this.manager.release( this.serializer );
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