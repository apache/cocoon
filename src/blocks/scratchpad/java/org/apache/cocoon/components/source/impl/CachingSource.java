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
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
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
import org.apache.excalibur.source.TraversableSource;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class implements a proxy like source that uses another source
 * to get the content. This implementation can cache the content for
 * a given period of time
 * 
 * <h2>Syntax for Protocol</h2>
 * <p>
 * cached:http://www.apache.org/[?cocoon:cache-expires=60&cocoon:cache-name=main]
 * </p>
 * <p>
 * The above examples show how the real source <code>http://www.apache.org</code>
 * is wrapped and the cached contents is used for <code>60</code> seconds.
 * The second querystring parameter instructs that the cache key be extended with the string 
 * <code>main</code>. This allows the use of multiple cache entries for the same source.
 * </p>
 * <p>
 * The value of the expires parameter holds some additional semantics. 
 * Specifying <code>-1</code> will yield the cached response to be considered valid
 * always. <code>0</code> can be used to achieve the exact opposite. That is to say, 
 * the cached contents will be thrown out and updated immediately and unconditionally.
 * <p>
 * @version CVS $Id: CachingSource.java,v 1.9 2004/03/24 15:19:20 unico Exp $
 */
public class CachingSource extends AbstractLogEnabled
implements Source, Serviceable, Initializable, XMLizable {
    
    /** The ServiceManager */
    protected ServiceManager manager;
    
    /** The SourceResolver to resolve the wrapped Source */
    protected SourceResolver resolver;
    
    /** The current cache */
    protected Cache cache;
    
    /** The refresher for asynchronous updates */
    protected Refresher refresher;
    
    /** The source object for the real content */
    protected Source source;
    
    /** The cached response (if any) */
    protected CachedSourceResponse response;
    
    /** Did we just update meta info? */
    protected boolean freshMeta;
    
    /** The full location string */
    final protected String uri;
    
    /** The used protocol */
    final protected String protocol;
    
    /** The key used in the store */
    final protected SimpleCacheKey cacheKey;
    
    /** number of seconds before cached object becomes invalid */
    final protected int expires;
    
    /** Parameters */
    final protected Parameters parameters;
    
    /** asynchronic refresh strategy ? */
    final protected boolean async;
    
    /**
     * Construct a new object.
     */
    public CachingSource(final String protocol,
                         final String uri,
                         final Source source,
                         final Parameters parameters,
                         final int expires,
                         final boolean async) {
        this.protocol = protocol;
        this.uri = uri;
        this.source = source;
        this.expires = expires;
        this.async = async;
        this.parameters = parameters;
        
        String key = "source:" + source.getURI();
        String cacheName = parameters.getParameter("cache-name", null);
        if (cacheName != null) {
            key += ":" + cacheName;
        }
        this.cacheKey = new SimpleCacheKey(key, false);
    }
        
    /**
     * Set the ServiceManager.
     */
    public void service(final ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }
    
    /**
     * Initialize the Source.
     */
    public void initialize() throws Exception {
        
        boolean checkValidity = true;
        if (this.expires == -1) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Using cached response if available.");
            }
            checkValidity = false;
        }
        
        if (this.async && this.expires != 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Not invalidating cached response " +                    "for asynch source " + getSourceURI());
            }
            checkValidity = false;
        }

        this.response = (CachedSourceResponse) this.cache.get(this.cacheKey);
        if (this.response == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No cached response found " +                    "for source " + getSourceURI());
            }
            checkValidity = false;
        }
        
        if (checkValidity) {
            
            final ExpiresValidity cacheValidity = (ExpiresValidity) this.response.getValidityObjects()[0];
            final SourceValidity sourceValidity = this.response.getValidityObjects()[1];
            
            boolean remove = false;
            if (this.expires == 0) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Force invalidation of cached response" +                        " of source " + getSourceURI());
                }
                remove = true;
            }
            else {
                boolean expired = cacheValidity.isValid() != SourceValidity.VALID;
                if (expired) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Cached response of source " 
                            + getSourceURI() + " is expired.");
                    }
                    boolean invalid = !isValid(sourceValidity, this.source);
                    if (invalid) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Cached response of source "
                                + getSourceURI() + " is invalid.");
                        }
                        remove = true;
                    }
                    else {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Cached response of source " 
                                + getSourceURI() + " is still valid.");
                        }
                        // set new expiration period
                        this.response.getValidityObjects()[0] = new ExpiresValidity(getExpiration());
                    }
                }
            }
            
            if (remove) {
                this.response = null;
                // remove it if it no longer exists
                if (!exists()) {
                    this.cache.remove(this.cacheKey);
                }
            }
        }
        if (this.async && this.expires > 0) {
            // schedule it with the refresher
            this.refresher.refresh(this.cacheKey,
                                   getSourceURI(),
                                   this.parameters.getParameter("cache-role", null),
                                   this.parameters);
        }
    }
    
    /**
     * Cleanup.
     */
    public void dispose() {
        if (this.source != null) {
            this.resolver.release(this.source);
            this.source = null;
        }
        this.manager = null;
        this.resolver = null;
        this.cache = null;
    }
    
    /**
     * Initialize the cached response with meta info.
     * 
     * @param refresh  whether to force refresh
     * @throws IOException  if an the binary response could not be initialized
     */
    protected void initMetaResponse() throws IOException {
        boolean storeResponse = false;
        CachedSourceResponse response = this.response;
        if (response == null) {
            if (this.expires != 0) {
                final SourceValidity cacheValidity = new ExpiresValidity(getExpiration());
                final SourceValidity sourceValidity = source.getValidity();
                response = new CachedSourceResponse(new SourceValidity[] {cacheValidity, sourceValidity});
                storeResponse = true;
            }
            else {
                response = new CachedSourceResponse(null);
            }
        }
        if (response.getExtra() == null) {
            response.setExtra(readMeta(this.source));
            this.freshMeta = true;
        }
        if (storeResponse) {
            this.response = response;
            try {
                this.cache.store(this.cacheKey, this.response);
            }
            catch(ProcessingException e) {
                throw new CascadingIOException("Failure storing response.", e);
            }
        }
    }
    
    /**
     * Initialize the cached response with binary contents.
     * 
     * @param refresh  whether to force refresh
     * @throws IOException  if an the binary response could not be initialized
     */
    protected void initBinaryResponse() throws IOException {
        boolean storeResponse = false;
        /* delay caching the response until we have a valid new one */
        CachedSourceResponse response = this.response;
        if (response == null) {
            if (this.expires != 0) {
                final SourceValidity cacheValidity = new ExpiresValidity(getExpiration());
                final SourceValidity sourceValidity = source.getValidity();
                response = new CachedSourceResponse(new SourceValidity[] {cacheValidity, sourceValidity});
                storeResponse = true;
            }
            else {
                response = new CachedSourceResponse(null);
            }
        }
        if (response.getBinaryResponse() == null) {
            response.setBinaryResponse(readBinaryResponse(this.source));
            if (!this.freshMeta) {
                /* always refresh meta in this case */
                response.setExtra(readMeta(this.source));
            }
        }
        if (storeResponse) {
            this.response = response;
            try {
                this.cache.store(this.cacheKey, this.response);
            }
            catch(ProcessingException e) {
                throw new CascadingIOException("Failure storing response.", e);
            }
        }
    }
    
    /**
     * Initialize the cached response with XML contents.
     * 
     * @param refresh  whether to force refresh.
     * @throws SAXException  if something happened during xml processing
     * @throws IOException  if an IO level error occured
     * @throws CascadingIOException  wraps all other exception types
     */
    protected void initXMLResponse(boolean refresh) throws SAXException, IOException, CascadingIOException {
        boolean storeResponse = false;
        /* delay caching the response until we have a valid new one */
        CachedSourceResponse response = this.response;
        if (response == null) {
            if (this.expires != 0) {
                final SourceValidity cacheValidity = new ExpiresValidity(getExpiration());
                final SourceValidity sourceValidity = source.getValidity();
                response = new CachedSourceResponse(new SourceValidity[] {cacheValidity, sourceValidity});
                storeResponse = true;
            }
            else {
                response = new CachedSourceResponse(null);
            }
        }
        if (response.getXMLResponse() == null || refresh) {
            byte[] binary = response.getBinaryResponse();
            response.setXMLResponse(readXMLResponse(this.source, binary, this.manager));
            if (!this.freshMeta) {
                /* always refresh meta in this case */
                response.setExtra(readMeta(this.source));
            }
        }
        if (storeResponse) {
            this.response = response;
            try {
                this.cache.store(this.cacheKey, this.response);
            }
            catch(ProcessingException e) {
                throw new CascadingIOException("Failure storing response.", e);
            }
        }
    }
    
    // ---------------------------------------------------- Source implementation
    
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
            initMetaResponse();
        } catch (IOException io) {
            return 0;
        }
        return ((SourceMeta) this.response.getExtra()).getLastModified();
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
    public String getMimeType() {
        try {
            initMetaResponse();
        } catch (IOException io) {
            return null;
        }
        return ((SourceMeta) this.response.getExtra()).getMimeType();
    }
    
    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
            initBinaryResponse();
        } catch (IOException se) {
            throw new SourceException("Failure getting input stream", se);
        }
        return new ByteArrayInputStream(this.response.getBinaryResponse());
    }

    /**
     * Return the unique identifer for this source
     */
    public String getURI() {
        return this.uri;
    }

    /**
     * @see org.apache.excalibur.source.Source#exists()
     */
    public boolean exists() {
		return this.source.exists();
    }
    
    /**
     *  Get the Validity object. This can either wrap the last modification
     *  date or the expires information or...
     *  If it is currently not possible to calculate such an information
     *  <code>null</code> is returned.
     */
    public SourceValidity getValidity() {
        long lastModified = getLastModified();
        if (lastModified > 0) {
            return new TimeStampValidity(lastModified);
        }
        return null;
    }
     
    /**
     * Refresh this object and update the last modified date
     * and content length.
     */
    public void refresh() {
        this.response = null;
        this.source.refresh();
    }
    
    // ---------------------------------------------------- XMLizable implementation
    
    /**
     * Generates SAX events representing the object's state.
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        XMLDeserializer deserializer = null;
        try {
            initXMLResponse(false);
            deserializer = (XMLDeserializer) this.manager.lookup(XMLDeserializer.ROLE);
            if (contentHandler instanceof XMLConsumer) {
                deserializer.setConsumer((XMLConsumer) contentHandler);
            } else {
                deserializer.setConsumer(new ContentHandlerWrapper(contentHandler));
            }
            deserializer.deserialize(this.response.getXMLResponse());
        } catch(CascadingIOException e) {
            throw new SAXException(e.getMessage(), (Exception) e.getCause());
        } catch(IOException e) {
            throw new SAXException("Failure reading SAX response.", e);
        } catch (ServiceException se ) {
            throw new SAXException("Missing service dependency: XMLdeserializer.", se);
        } finally {
            this.manager.release(deserializer);
        }
    }
    
    // ---------------------------------------------------- CachingSource specific accessors
    
    /**
     * Return the uri of the cached source.
     */
    protected String getSourceURI() {
        return this.source.getURI();
    }
    
    /**
     * Return the used key.
     */
    protected SimpleCacheKey getCacheKey() {
        return this.cacheKey;
    }
    
    /**
     * Expires (in milli-seconds)
     */
    protected long getExpiration() {
        return this.expires * 1000;
    }
    
    /**
     * Read XML content from source.
     * 
	 * @return content from source
	 * @throws SAXException
	 * @throws IOException
	 * @throws CascadingIOException
	 */
	protected static byte[] readXMLResponse(Source source, byte[] binary, ServiceManager manager) 
    throws SAXException, IOException, CascadingIOException {
        XMLSerializer serializer = null;
        XMLizer xmlizer = null;
        byte[] result = null;
		try {
		    serializer = (XMLSerializer) manager.lookup(XMLSerializer.ROLE);
            
            if (source instanceof XMLizable) {
                ((XMLizable) source).toSAX(serializer);
            }
            else {
                if (binary == null) {
                    binary = readBinaryResponse(source);
                }
                final String mimeType = source.getMimeType();
                if (mimeType != null) {
                    xmlizer = (XMLizer) manager.lookup(XMLizer.ROLE);
                    xmlizer.toSAX(new ByteArrayInputStream(binary),
                                  mimeType,
                                  source.getURI(),
                                  serializer);
                }
            }
		    result = (byte[]) serializer.getSAXFragment();
		} catch (ServiceException se) {
		    throw new CascadingIOException("Missing service dependency.", se);
		} finally {
            manager.release(xmlizer);
		    manager.release(serializer);
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
	protected static byte[] readBinaryResponse(Source source) 
    throws IOException, SourceNotFoundException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final byte[] buffer = new byte[2048];
		final InputStream inputStream = source.getInputStream();
		int length;
		while ((length = inputStream.read(buffer)) > -1) {
		    baos.write(buffer, 0, length);
		}
		baos.flush();
		inputStream.close();
		return baos.toByteArray();
	}
    
    /**
     * Read meta data from source.
     * 
     * @return source meta data
     * @throws IOException
     */
    protected static SourceMeta readMeta(Source source) throws IOException {
        SourceMeta meta;
        
        if (source instanceof TraversableSource) {
            
            final TraversableSourceMeta tmeta = new TraversableSourceMeta();
            final TraversableSource tsource = (TraversableSource) source;
            
            tmeta.setName(tsource.getName());
            tmeta.setIsCollection(tsource.isCollection());
            
            if (tmeta.isCollection()) {
                final Collection children = tsource.getChildren();
                if (children != null) {
                    final String[] names = new String[children.size()];
                    final Iterator iter = children.iterator();
                    int count = 0;
                    while(iter.hasNext()) {
                        TraversableSource child = (TraversableSource) iter.next();
                        names[count] = child.getName();
                        count++;
                    }
                    tmeta.setChildren(names);
                }
            }
            
            meta = tmeta;
        }
        else {
            meta = new SourceMeta();
        }
        
        final long lastModified = source.getLastModified();
        if (lastModified > 0) {
            meta.setLastModified(lastModified);
        }
        else {
            meta.setLastModified(System.currentTimeMillis());
        }
        meta.setMimeType(source.getMimeType());
        
        return meta;
    }
    
    protected static boolean isValid(SourceValidity validity, Source source) {
        if (validity == null) return false;
        return validity.isValid() == SourceValidity.VALID || 
              (validity.isValid() == SourceValidity.UNKNOWN && 
               validity.isValid(source.getValidity()) == SourceValidity.VALID);
    }
    
    /**
     * Data holder for caching Source meta info.
     */
    protected static class SourceMeta implements Serializable {
        
        private String m_mimeType;
        private long m_lastModified;
        private boolean m_exists;
        
        protected String getMimeType() {
            return m_mimeType;
        }
        
        protected void setMimeType(String mimeType) {
            m_mimeType = mimeType;
        }
        
        protected long getLastModified() {
            return m_lastModified;
        }
        
        protected void setLastModified(long lastModified) {
            m_lastModified = lastModified;
        }
        
        protected boolean exists() {
            return m_exists;
        }
        
        protected void setExists(boolean exists) {
            m_exists = exists;
        }
        
    }
    
    protected static class TraversableSourceMeta extends SourceMeta {
        private String   m_name;
        private boolean  m_isCollection;
        private String[] m_children;
        
        protected String getName() {
            return m_name;
        }
        
        protected void setName(String name) {
            m_name = name;
        }
        
        protected boolean isCollection() {
            return m_isCollection;
        }
        
        protected void setIsCollection(boolean isCollection) {
            m_isCollection = isCollection;
        }
        
        protected String[] getChildren() {
            return m_children;
        }
        
        protected void setChildren(String[] children) {
            m_children = children;
        }
    }

}
