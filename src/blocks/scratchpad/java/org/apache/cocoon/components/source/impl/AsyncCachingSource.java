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
package org.apache.cocoon.components.source.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.ExtendedCachedResponse;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;

/**
 * This class implements a proxy like source that uses another source
 * to get the content. This implementation can cache the content for
 * a given period of time
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AsyncCachingSource.java,v 1.5 2004/03/06 21:00:39 haul Exp $
 * @since 2.1.1
 */
public class AsyncCachingSource
extends CachingSource
implements Source, Serviceable, Initializable, Disposable, XMLizable {

    protected SourceValidity validity;
    
    /**
     * Construct a new object
     */
    public AsyncCachingSource( String location,
                          Map    parameters) 
    throws MalformedURLException {
        super(location, parameters );
    }

    public void setResponse(CachedResponse r) {
        this.cachedResponse = (ExtendedCachedResponse) r;
    }
    
    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        return 0;
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
    throws IOException, SourceException {
        byte[] response = this.cachedResponse.getResponse();
        if ( response == null ) {
            
            // the stream was not cached, so we *have* to get it here
            
            this.initSource();
            
            // update cache
            final byte[] xmlResponse = this.cachedResponse.getAlternativeResponse();
            this.cachedResponse = new ExtendedCachedResponse(this.cachedResponse.getValidityObjects(),
                                                             this.readBinaryResponse());
            this.cachedResponse.setAlternativeResponse(xmlResponse);
            try {
                this.cache.store(this.streamKey, this.cachedResponse);
            } catch (ProcessingException ignore) {
                if (this.getLogger().isDebugEnabled()) {
					this.getLogger().debug("Storing cached response, ignoring exception:",ignore);
				}
            }
            
            response = this.cachedResponse.getResponse();
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Using fresh response for "+this.streamKey.getKey());
            }
        } else {
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Using cached response for "+this.streamKey.getKey());
            }
        }
        return new ByteArrayInputStream(response);            
    }

    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        return this.validity;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.validity = this.cachedResponse.getValidityObjects()[0];
    }

}
