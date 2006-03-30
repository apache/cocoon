/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplet.adapter.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.CopletInstanceDataFeatures;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.util.NetUtils;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This adapter extends the {@link org.apache.cocoon.portal.coplet.adapter.impl.URICopletAdapter}
 * by a caching mechanism. The result of the called uri/pipeline is cached until a 
 * {@link org.apache.cocoon.portal.event.CopletInstanceEvent} for that coplet instance
 * is received.
 * The content can eiter be cached in the user session or globally. The default is
 * the user session.
 *
 * @version $Id$
 */
public class CachingURICopletAdapter
    extends URICopletAdapter
    implements Receiver {

    /** The configuration name for enabling/disabling the cache. */
    public static final String CONFIGURATION_ENABLE_CACHING = "cache-enabled";

    /** The configuration name for using the global cache. */
    public static final String CONFIGURATION_CACHE_GLOBAL= "cache-global";

    /** The configuration name for querying instance attributes to generate the key
     * for the global cache. */
    public static final String CONFIGURATION_CACHE_GLOBAL_USE_ATTRIBUTES= "cache-global-use-attributes";

    /** The configuration name for ignoring all sizing events to clear the cache. */
    public static final String CONFIGURATION_IGNORE_SIZING_EVENTS = "ignore-sizing-events";

    /** The configuration name for ignoring just min/normal sizing events to clear the cache. */
    public static final String CONFIGURATION_IGNORE_SIMPLE_SIZING_EVENTS = "ignore-simple-sizing-events";

    /** The temporary attribute name for the storing the cached coplet content. */
    public static final String CACHE = "cacheData";

    /** This temporary attribute can be set on the instance to not cache the current response. */
    public static final String DO_NOT_CACHE = "doNotCache";
    
    /** The cache to use for global caching. */
    protected Cache cache;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.cache = (Cache)this.manager.lookup(Cache.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.cache);
            this.cache = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, org.xml.sax.ContentHandler)
     */
    public void streamContent(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
        this.streamContent( coplet, 
                            (String) coplet.getCopletData().getAttribute("uri"),
                            contentHandler);
    }

    /**
     * @see org.apache.cocoon.portal.coplet.adapter.impl.URICopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, java.lang.String, org.xml.sax.ContentHandler)
     */
    public void streamContent( final CopletInstanceData coplet,
                               final String uri,
                               final ContentHandler contentHandler)
    throws SAXException {
        // Is caching enabled?
        boolean cachingEnabled = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_ENABLE_CACHING, Boolean.TRUE)).booleanValue();
        // do we cache globally?
        boolean cacheGlobal = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL, Boolean.FALSE)).booleanValue();

        Object data = null;
        // If caching is enabed and the cache is still valid, then use the cache
        if (cachingEnabled) {
            if ( cacheGlobal ) {
                final String key = this.getCacheKey(coplet, uri);
                CachedResponse response = this.cache.get(key);
                if (response != null ) {
                    data = response.getResponse();
                }
            } else {
                data = coplet.getTemporaryAttribute(CACHE);
            }
        }
        if (data == null) {
            // if caching is permanently or temporary disabled, flush the cache and invoke coplet
            if ( !cachingEnabled || coplet.getTemporaryAttribute(DO_NOT_CACHE) != null ) {
                coplet.removeTemporaryAttribute(DO_NOT_CACHE);
                if ( cacheGlobal ) {
                    final String key = this.getCacheKey(coplet, uri);
                    this.cache.remove(key); 
                } else {
                    coplet.removeTemporaryAttribute(CACHE);
                }
                super.streamContent(coplet, uri, contentHandler);                
            } else {

                XMLByteStreamCompiler bc = new XMLByteStreamCompiler();

                super.streamContent(coplet, uri, bc);
                data = bc.getSAXFragment();
                if (coplet.removeTemporaryAttribute(DO_NOT_CACHE) == null) {
                    if ( cacheGlobal ) {
                        CachedResponse response = new CachedResponse((SourceValidity[])null, (byte[])data);
                        try {
                            final String key = this.getCacheKey(coplet, uri);
                            this.cache.store(key, response);
                        } catch (ProcessingException pe) {
                            // we ignore this
                            this.getLogger().warn("Exception during storing response into cache.", pe);
                        }
                    } else {
                        coplet.setTemporaryAttribute(CACHE, data);
                    }
                }
            }
        }
        // and now stream the data
        if ( data != null ) {
            XMLByteStreamInterpreter bi = new XMLByteStreamInterpreter();
            bi.setContentHandler(contentHandler);
            if ( contentHandler instanceof LexicalHandler ) {
                bi.setLexicalHandler((LexicalHandler)contentHandler);
            }
            bi.deserialize(data);
        }
    }

    /**
     * @see org.apache.cocoon.portal.event.Receiver
     */
    public void inform(CopletInstanceEvent e, PortalService service) {
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("CopletInstanceEvent " + e + " caught by CachingURICopletAdapter");
        }
        this.handleCopletInstanceEvent(e);
    }

    /**
     * This adapter listens for CopletInstanceEvents. Each event sets the cache invalid.
     */
    public void handleCopletInstanceEvent(CopletInstanceEvent event) {
        final CopletInstanceData coplet = event.getTarget();

        // do we ignore SizingEvents
        boolean ignoreSizing = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_IGNORE_SIZING_EVENTS, Boolean.TRUE)).booleanValue();

        if ( !ignoreSizing || !CopletInstanceDataFeatures.isSizingEvent(event)) {
            boolean cleanupCache = true;
            boolean ignoreSimpleSizing = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_IGNORE_SIMPLE_SIZING_EVENTS, Boolean.FALSE)).booleanValue();
            if ( ignoreSimpleSizing && CopletInstanceDataFeatures.isSizingEvent(event) ) {
                int newSize = CopletInstanceDataFeatures.getSize(event);
                int oldSize = coplet.getSize();
                if (  (oldSize == CopletInstanceData.SIZE_NORMAL || oldSize == CopletInstanceData.SIZE_MINIMIZED )
                   && (newSize == CopletInstanceData.SIZE_NORMAL || newSize == CopletInstanceData.SIZE_MINIMIZED )) {
                    cleanupCache = false;
                }
            }
            if ( cleanupCache ) {
                // do we cache globally?
                boolean cacheGlobal = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL, Boolean.FALSE)).booleanValue();
                boolean cacheGlobalUseAttributes = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL_USE_ATTRIBUTES, Boolean.FALSE)).booleanValue();
                if ( cacheGlobal ) {
                    if ( !cacheGlobalUseAttributes ) {
                        final String key = this.getCacheKey(coplet,
                                                            (String) coplet.getCopletData().getAttribute("uri"));
                        this.cache.remove(key);
                    }
                } else {
                    coplet.removeTemporaryAttribute(CACHE);
                }
            }
        }
    }

    /**
     * Build the key for the global cache.
     */
    protected String getCacheKey(CopletInstanceData coplet, String uri) {
        final Boolean useAttributes = (Boolean)this.getConfiguration(coplet,
                                                            CONFIGURATION_CACHE_GLOBAL_USE_ATTRIBUTES,
                                                            Boolean.FALSE);
        if ( !useAttributes.booleanValue() ) {
            return "coplet:" + coplet.getCopletData().getId() + '/' + uri;
        }
        final StringBuffer buffer = new StringBuffer("coplet:");
        buffer.append(coplet.getCopletData().getId());
        buffer.append('/');
        buffer.append(uri);
        boolean hasParams = false;
        // first add attributes:
        // sort the keys
        List keyList = new ArrayList(coplet.getAttributes().keySet());
        Collections.sort(keyList);
        Iterator i = keyList.iterator();
        while ( i.hasNext() ) {
            final Object name = i.next();
            final Object value = coplet.getAttribute(name.toString());
            if ( hasParams ) {
                buffer.append('&');
            } else {
                buffer.append('?');
                hasParams = true;
            }
            buffer.append(name.toString());
            buffer.append('=');
            if ( value != null ) {
                try {
                    buffer.append(NetUtils.encode(value.toString(), "utf-8"));
                } catch (UnsupportedEncodingException ignore) {
                    // we ignore this
                }
            }
        }
        // second add temporary attributes
        keyList = new ArrayList(coplet.getTemporaryAttributes().keySet());
        Collections.sort(keyList);
        i = keyList.iterator();
        while ( i.hasNext() ) {
            final Object name = i.next();
            final Object value = coplet.getTemporaryAttribute(name.toString());
            if ( hasParams ) {
                buffer.append('&');
            } else {
                buffer.append('?');
                hasParams = true;
            }
            buffer.append(name.toString());
            buffer.append('=');
            if ( value != null ) {
                try {
                    buffer.append(NetUtils.encode(value.toString(), "utf-8"));
                } catch (UnsupportedEncodingException ignore) {
                    // we ignore this
                }
            }
        }
        return buffer.toString();            
    }
}
