/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Event;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This is the adapter to use pipelines as coplets. The result of the called 
 * pipeline is cached until a 
 * {@link org.apache.cocoon.portal.event.CopletInstanceEvent}
 * for that coplet is received. Configuration options of super
 * classes apply.
 *
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 * 
 * @version CVS $Id: CachingURICopletAdapter.java,v 1.5 2004/04/25 20:09:34 haul Exp $
 */
public class CachingURICopletAdapter
    extends URICopletAdapter
    implements Parameterizable {
    
    /**
     * The cache for saving the coplet data
     */
    public static final String CACHE = "cacheData";

    /**
     * Marks the validity of the cached data
     */
    public static final String CACHE_VALIDITY = "cacheValidity";

    /**
     * Tells the adapter to not cache the current response
     */
    public static final String DO_NOT_CACHE = "doNotCache";
    
    /**
     * Marks cache valid.
     */
    public static final String CACHE_VALID = "1";

    /**
     * Marks cache invalid
     */
    public static final String CACHE_INVALID = "0";

    /**
     * Caching can be basically disabled with this parameter
     */
    public static final String PARAMETER_DISABLE_CACHING = "disable_caching";

    /**
     * instance variable, that shows, if caching is disabled
     */
    private boolean disableCaching = false;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters parameters) {
        if (parameters != null) {
            this.disableCaching = parameters.getParameterAsBoolean(PARAMETER_DISABLE_CACHING, false);
            if (this.disableCaching) {
                getLogger().info(this.getClass().getName() + " Caching is disabled.");
            } else {
                getLogger().info(this.getClass().getName() + " Caching is enabled.");
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.impl.AbstractCopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, org.xml.sax.ContentHandler)
     */
    public void streamContent(CopletInstanceData coplet, ContentHandler contentHandler)
    throws SAXException {
        this.streamContent( coplet, (String) coplet.getCopletData().getAttribute("uri"), contentHandler);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.adapter.impl.URICopletAdapter#streamContent(org.apache.cocoon.portal.coplet.CopletInstanceData, java.lang.String, org.xml.sax.ContentHandler)
     */
    public void streamContent( final CopletInstanceData coplet,
                               final String uri,
                               final ContentHandler contentHandler)
    throws SAXException {
        if (this.isValidCache(coplet)) {
            this.toSAXFromCache(coplet, contentHandler);
        } else {
            XMLByteStreamCompiler bc = new XMLByteStreamCompiler();

            super.streamContent(coplet, uri, bc);

            if ( coplet.getAttribute(DO_NOT_CACHE) != null ) {
                coplet.removeAttribute(DO_NOT_CACHE);
                this.setCacheInvalid(coplet);
                XMLByteStreamInterpreter bi = new XMLByteStreamInterpreter();
                bi.setContentHandler(contentHandler);

                bi.deserialize(bc.getSAXFragment());
            } else {
            this.toCache(coplet, bc.getSAXFragment());

            this.toSAXFromCache(coplet, contentHandler);
        }
    }
    }

    /**
     * Caches the data of the coplet resource in the coplet instance.
     * @param coplet the coplet instance data
     * @param data the data of the coplet resource
     */
    private void toCache(CopletInstanceData coplet, Object data) {
        coplet.setAttribute(CACHE, data);

        this.setCacheValid(coplet);
    }

    /**
     * Creates SAX events from the cached coplet data.
     * @param coplet the coplet instance data
     * @param contentHandler the handler, that should receive the SAX events
     * @throws SAXException
     */
    private void toSAXFromCache(CopletInstanceData coplet,
                                ContentHandler contentHandler)
    throws SAXException {
        XMLByteStreamInterpreter bi = new XMLByteStreamInterpreter();
        bi.setContentHandler(contentHandler);

        bi.deserialize(coplet.getAttribute(CACHE));
    }

    /**
     * Tests the cache for validity.
     * @param coplet the coplet instance data
     */
    public boolean isValidCache(CopletInstanceData coplet) {
        if (disableCaching) {
            return false;
        }
        String cacheValidity = (String) coplet.getAttribute(CACHE_VALIDITY);

        if (cacheValidity == null) {
            return false;
        }
        return CACHE_VALID.equals(cacheValidity);
    }

    /**
     * Sets the cache valid.
     * @param coplet the coplet instance data
     */
    public void setCacheValid(CopletInstanceData coplet) {
        coplet.setAttribute(CACHE_VALIDITY, CACHE_VALID);
    }

    /**
     * Sets the cache invalid.
     * @param coplet the coplet instance data
     */
    public void setCacheInvalid(CopletInstanceData coplet) {
        coplet.setAttribute(CACHE_VALIDITY, CACHE_INVALID);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event e) {
        if (e instanceof CopletInstanceEvent) {
            if ( this.getLogger().isInfoEnabled() ) {
                this.getLogger().info("CopletInstanceEvent " + e + " caught by CachingURICopletAdapter");
            }
            this.handleCopletInstanceEvent(e);
        }
        super.inform(e);
    }

    /**
     * This adapter listens for CopletInstanceEvents. Each event sets the cache invalid.
     */
    public void handleCopletInstanceEvent(Event e) {
        final CopletInstanceEvent event = (CopletInstanceEvent) e;

        final CopletInstanceData coplet = (CopletInstanceData) event.getTarget();

        this.setCacheInvalid(coplet);
    }

}
