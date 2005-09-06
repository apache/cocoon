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

import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;
import org.apache.cocoon.util.Deprecation;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This adapter extends the {@link org.apache.cocoon.portal.coplet.adapter.impl.URICopletAdapter}
 * by a caching mechanism. The result of the called uri/pipeline is cached until a 
 * {@link org.apache.cocoon.portal.event.CopletInstanceEvent} for that coplet instance
 * is received.
 * The content can eiter be cached in the user session or globally. The default is
 * the user session.
 *
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 * @author <a href="mailto:cziegeler.at.apache.dot.org">Carsten Ziegeler</a>
 * @version $Id$
 */
public class CachingURICopletAdapter
    extends URICopletAdapter
    implements Parameterizable {

    /** The configuration name for enabling/disabling the cache. */
    public static final String CONFIGURATION_ENABLE_CACHING = "cache-enabled";

    /** The configuration name for using the global cache. */
    public static final String CONFIGURATION_CACHE_GLOBAL= "cache-global";

    /** The configuration name for ignoring sizing events to clear the cache. */
    public static final String CONFIGURATION_IGNORE_SIZING_EVENTS = "ignore-sizing-events";

    /** The temporary attribute name for the storing the cached coplet content. */
    public static final String CACHE = "cacheData";

    /** This temporary attribute can be set on the instance to not cache the current response. */
    public static final String DO_NOT_CACHE = "doNotCache";

    /** 
     * Caching can be basically disabled with this boolean parameter.
     * @deprecated Use coplet base data configuration.
     */
    public static final String PARAMETER_DISABLE_CACHING = "disable_caching";

    /** Is caching enabled? */
    protected Boolean enableCaching = Boolean.TRUE;

    /** The cache to use for global caching. */
    protected Cache cache;

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) {
        if ( parameters.getParameter(PARAMETER_DISABLE_CACHING, null) != null ) {
            Deprecation.logger.info("The 'disable_caching' parameter on the caching uri coplet adapter is deprecated. "
                                   +"Use the configuration of the base coplet data instead.");
        }
        boolean disableCaching = parameters.getParameterAsBoolean(PARAMETER_DISABLE_CACHING,
                                                                  !this.enableCaching.booleanValue());
        this.enableCaching = new Boolean(!disableCaching);
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info(this.getClass().getName() + ": enable-caching=" + this.enableCaching);
        }
    }

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
        boolean cachingEnabled = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_ENABLE_CACHING, this.enableCaching)).booleanValue();
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
        // and now stream the data
        if ( data != null ) {
            XMLByteStreamInterpreter bi = new XMLByteStreamInterpreter();
            bi.setContentHandler(contentHandler);
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
        super.inform(e, service);
    }

    /**
     * This adapter listens for CopletInstanceEvents. Each event sets the cache invalid.
     */
    public void handleCopletInstanceEvent(CopletInstanceEvent event) {
        final CopletInstanceData coplet = (CopletInstanceData) event.getTarget();

        // do we ignore SizingEvents
        boolean ignoreSizing = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_IGNORE_SIZING_EVENTS, Boolean.TRUE)).booleanValue();

        if ( !ignoreSizing || !isSizingEvent(event)) {
            // do we cache globally?
            boolean cacheGlobal = ((Boolean)this.getConfiguration(coplet, CONFIGURATION_CACHE_GLOBAL, Boolean.FALSE)).booleanValue();
            if ( cacheGlobal ) {
                final String key = this.getCacheKey(coplet,
                                                    (String) coplet.getCopletData().getAttribute("uri"));
                this.cache.remove(key);
            } else {
                coplet.removeTemporaryAttribute(CACHE);
            }
        }
    }

    /**
     * Tests if the event is a sizing event for the coplet.
     */
    protected boolean isSizingEvent(CopletInstanceEvent event) {
        if ( event instanceof ChangeCopletInstanceAspectDataEvent ) {
            if (((ChangeCopletInstanceAspectDataEvent)event).getAspectName().equals("size")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build the key for the global cache.
     */
    protected String getCacheKey(CopletInstanceData coplet, String uri) {
        return "coplet:" + coplet.getCopletData().getId() + '/' + uri;
    }
}
