/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
 * This is the adapter to use pipelines as coplets. The result of the called pipeline is cached until
 * the coplet gets a new CopletLinkEvent.
 *
 * @author <a href="mailto:gerald.kahrer@rizit.at">Gerald Kahrer</a>
 * 
 * @version CVS $Id: CachingURICopletAdapter.java,v 1.2 2004/02/09 13:39:42 cziegeler Exp $
 */
public class CachingURICopletAdapter
    extends URICopletAdapter
    implements Parameterizable {
    
    /**
     * The cache for saving the coplet data
     */
    private static final String CACHE = "cacheData";

    /**
     * Marks the validity of the cached data
     */
    private static final String CACHE_VALIDITY = "cacheValidity";

    /**
     * Marks cache valid.
     */
    private static final String CACHE_VALID = "1";

    /**
     * Marks cache invalid
     */
    private static final String CACHE_INVALID = "0";

    /**
     * Caching can be basically disabled with this parameter
     */
    private static final String PARAMETER_DISABLE_CACHING = "disable_caching";

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
            if (disableCaching) {
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

            this.toCache(coplet, bc.getSAXFragment());

            this.toSAXFromCache(coplet, contentHandler);
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
