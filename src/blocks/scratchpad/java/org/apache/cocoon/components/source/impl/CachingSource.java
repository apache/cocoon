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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.ExtendedCachedResponse;
import org.apache.cocoon.caching.SimpleCacheKey;
import org.apache.cocoon.components.sax.XMLDeserializer;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class implements a proxy like source that uses another source
 * to get the content. This implementation can cache the content for
 * a given period of time
 * 
 * <h2>Syntax for Protocol</h2>
 * <p>
 * The URL needs to contain the URL of the cached source, an expiration
 * period in second, and optionally a cache key: <code>cached://60@http://www.s-und-n.de</code>
 * or <code>cached://60@main@http://www.s-und-n.de</code> 
 * </p>
 * <p>
 * The above examples show how the real source <code>http://www.s-und-n.de</code>
 * is wrapped and the cached contents is used for <code>60</code> seconds.
 * The second example extends the cache key with the string <code>main</code>
 * allowing multiple cache entries for the same source.
 * </p>
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CachingSource.java,v 1.6 2004/03/06 21:00:39 haul Exp $
 */
public class CachingSource
extends AbstractLogEnabled
implements Source, Serviceable, Initializable, Disposable, XMLizable {

    /** The current ServiceManager */
    protected ServiceManager manager;

    /** The current source resolver */
    protected SourceResolver resolver;

    /** The current cache */
    protected Cache cache;

    /** The uri */
    final protected String uri;

    /** The used protocol */
    final protected String protocol;

    /** The uri of the real source*/
    final protected String sourceURI;
    
    /** Parameters for the source */
    final protected Map parameters;
    
    /** The expires information */
    final protected long expires;
   
    /** The key used in the store */
    final protected SimpleCacheKey streamKey;
    
    /** The cached response (if any) */
    protected ExtendedCachedResponse cachedResponse;
    
    /** The source object for the real content */
    protected Source source;
    
    /**
     * Construct a new object. Syntax for cached source:
     * "<code>cache-protocol</code>" + "<code>://</code>" + "<code>seconds</code>"
     * + "<code>@</code>" + "<code>wrapped-URL</code>".
     * 
     */
    public CachingSource( String location,
                          Map    parameters) 
    throws MalformedURLException {
        int separatorPos = location.indexOf('@');
        if (separatorPos == -1) {
            throw new MalformedURLException("@ required in URI: " + location);
        }
        int protocolEnd = location.indexOf("://");
        if (protocolEnd == -1)
            throw new MalformedURLException("URI does not contain '://' : " + location);

        final String expiresText = location.substring(protocolEnd+3, separatorPos);
        this.expires = Long.valueOf(expiresText).longValue() * 1000;
        this.protocol = location.substring(0, protocolEnd);
        String endString = location.substring(separatorPos+1);
        separatorPos = endString.indexOf('@');
        if ( separatorPos == -1 ) {
            this.sourceURI = endString;
            this.streamKey = new SimpleCacheKey("source:" + this.sourceURI, false);
        } else {
            this.sourceURI = endString.substring(separatorPos+1);
            this.streamKey = new SimpleCacheKey("source:" + endString.substring(0, separatorPos), false);
        }
        this.uri = location;
        this.parameters = parameters;
    }

    /**
     * Return the used key
     */
    public SimpleCacheKey getCacheKey() {
        return this.streamKey;
    }
    
    /**
     * Expires (in milli-seconds)
     */
    public long getExpiration() {
        return this.expires;
    }
    
    /**
     * Return the protocol identifier.
     */
    public String getScheme() {
        return this.protocol;
    }

    /**
     * Get the content length of the source or -1 if it
     * is not possible to determine the length.
     */
    public long getContentLength() {
        return -1;
    }

    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        try {
            this.initSource();
        } catch (IOException io) {
            return 0;
        }

        return this.source.getLastModified();
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream()
    throws IOException, SourceException {
        try {
            this.initCache(false);
        } catch (SAXException se) {
            throw new SourceException("Unable to init source", se);
        }   
        return new ByteArrayInputStream(this.cachedResponse.getResponse());
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * Return the unique identifer for the cached source
     */
    public String getSourceURI() {
        return this.sourceURI;
    }

    /**
     * 
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
        try {
            this.initSource();
        } catch (IOException io) {
            return true;
        }
        return this.source.exists();
    }
    
    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        try {
            this.initCache(true);
        } catch (SAXException se) {
            return null;
        } catch (IOException io) {
            return null;
        }
        return this.cachedResponse.getValidityObjects()[0];
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
     public String getMimeType() {
         return null;
     }

    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        if ( this.source != null) {
            this.source.refresh();
        }
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public String getParameter(String name) {
        return null;
    }

    /**
     * Get the value of a parameter.
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public long getParameterAsLong(String name) {
        return 0;
    }

    /**
     * Get parameter names
     * Using this it is possible to get custom information provided by the
     * source implementation, like an expires date, HTTP headers etc.
     */
    public Iterator getParameterNames() {
        return java.util.Collections.EMPTY_LIST.iterator();
    }


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Set the required components
     * This is done for performance reasons, the components are only looked up
     * once by the factory
     */
    public void init(SourceResolver resolver, Cache cache) {
        this.resolver = resolver;
        this.cache = cache;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        // check if the source is cached
        this.cachedResponse = (ExtendedCachedResponse)this.cache.get( this.streamKey );
        if ( this.cachedResponse != null ) {
            SourceValidity expiresValidity = this.cachedResponse.getValidityObjects()[0];
            if ( this.expires != -1
                 && (this.expires == 0 || expiresValidity.isValid() != SourceValidity.VALID)) {
                // TODO: according to SourceValidity.isValid() validity should be checked against 
                //       new validity object if UNKNOWN is returned
                //       maybe this is too expensive?
                
                // remove from cache if not valid anymore
                this.cache.remove( this.streamKey );
                this.cachedResponse = null;
            }
        }
    }

    /**
     * Initialize the source
     */
    protected void initSource() 
    throws IOException{
        if ( this.source == null ) {
            this.source = this.resolver.resolveURI(this.sourceURI, null, this.parameters);
        }
    }
    
    /** 
     * Initialize the cache
     */
    protected boolean initCache(boolean alternative)
    throws IOException, SAXException {
        this.initSource();
        boolean storeResponse = false;
        
        if ( this.cachedResponse == null
             && (!alternative || !(this.source instanceof XMLizable)) ) {
            
			this.cachedResponse = new ExtendedCachedResponse(
					new ExpiresValidity(this.expires), this.readBinaryResponse());
            storeResponse = true;                                                             
        } else if ( this.cachedResponse == null ) {
            this.cachedResponse = new ExtendedCachedResponse(new ExpiresValidity(this.expires), null);                                                            
        }
        
        // we cache both
        if ( alternative && this.cachedResponse.getAlternativeResponse() == null ) {
            this.cachedResponse.setAlternativeResponse(this.readXMLResponse());
            storeResponse = true;
        }
        
        if ( storeResponse && this.expires > 0 ) {
            try {
                this.cache.store(this.streamKey, this.cachedResponse);
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Storing response for "+this.streamKey.getKey());
                }
            } catch (ProcessingException ignore) {
                if (this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug("Ignoring exception when storing response.", ignore) ;
                }
            }
        }
        return storeResponse;
    }
    
    /**
     * Read XML content from source.
     * 
	 * @return content from source
	 * @throws SAXException
	 * @throws IOException
	 * @throws CascadingIOException
	 */
	protected byte[] readXMLResponse() throws SAXException, IOException, CascadingIOException {
		XMLSerializer serializer = null;
		SAXParser parser = null;
        byte[] result = null;
		try {
		    serializer = (XMLSerializer)this.manager.lookup(XMLSerializer.ROLE);
		    if (this.source instanceof XMLizable) {
		        ((XMLizable)this.source).toSAX(serializer);
		    } else {
		        parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);
		        
		        final InputSource inputSource = new InputSource(new ByteArrayInputStream(this.cachedResponse.getResponse()));
		        inputSource.setSystemId(this.source.getURI());
		        
		        parser.parse(inputSource, serializer);
		    }
		    
		    result = (byte[])serializer.getSAXFragment();
		} catch (ServiceException se) {
		    throw new CascadingIOException("Unable to lookup xml serializer.", se);
		} finally {
		    this.manager.release(parser);
		    this.manager.release(serializer);
		}
		return result;
	}

	/**
     * Read binary content from source.
     * 
	 * @return content from source
	 * @throws IOException
	 * @throws SourceNotFoundException
	 */
	protected byte[] readBinaryResponse() throws IOException, SourceNotFoundException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[2048];
		final InputStream inputStream = this.source.getInputStream();
		int length;
      
		while ((length = inputStream.read(buffer)) > -1) {
		    baos.write(buffer, 0, length);
		}
		baos.flush();
		inputStream.close();
		return baos.toByteArray();
	}

	/* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.source != null ) {
            this.resolver.release( this.source );
            this.source = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.xml.sax.XMLizable#toSAX(org.xml.sax.ContentHandler)
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
       XMLDeserializer deserializer = null;
       try {
           deserializer = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
           if ( contentHandler instanceof XMLConsumer) {
               deserializer.setConsumer((XMLConsumer)contentHandler);
           } else {
               deserializer.setConsumer(new ContentHandlerWrapper(contentHandler));
           }
           deserializer.deserialize( this.cachedResponse.getAlternativeResponse() );
       } catch (ServiceException se ) {
           throw new SAXException("Unable to lookup xml deserializer.", se);
       } finally {
           this.manager.release(deserializer);
       }
    }

}
