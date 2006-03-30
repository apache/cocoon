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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.Cache;
import org.apache.cocoon.caching.EventAware;
import org.apache.cocoon.caching.IdentifierCacheKey;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.caching.validity.NamedEvent;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
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
 * @version $Id$
 */
public class CachingSource extends AbstractLogEnabled
implements Source, Serviceable, Initializable, XMLizable {

    // ---------------------------------------------------- Constants
    
    public static final String CACHE_EXPIRES_PARAM = "cache-expires";
    public static final String CACHE_NAME_PARAM = "cache-name";
    
    // ---------------------------------------------------- Instance variables
    
    /** The ServiceManager */
    protected ServiceManager manager;

    /** The current cache */
    protected Cache cache;

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
    final protected IdentifierCacheKey cacheKey;
    
    /** number of seconds before cached object becomes invalid */
    final protected int expires;
    
    /** cache key extension */
    final protected String cacheName;
    
    /** asynchronic refresh strategy ? */
    final protected boolean async;

    final protected boolean eventAware;

    /**
     * Construct a new object.
     */
    public CachingSource(final String protocol,
                         final String uri,
                         final Source source,
                         final int expires,
                         final String cacheName,
                         final boolean async,
                         final boolean eventAware) {
        this.protocol = protocol;
        this.uri = uri;
        this.source = source;
        this.expires = expires;
        this.cacheName = cacheName;
        this.async = async;
        this.eventAware = eventAware;

        String key = "source:" + getSourceURI();
        if (cacheName != null) {
            key += ":" + cacheName;
        }
        this.cacheKey = new IdentifierCacheKey(key, false);
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
                getLogger().debug("Using cached response if available.");
            }
            checkValidity = false;
        }

        this.response = (CachedSourceResponse) this.cache.get(this.cacheKey);

        if (this.response == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No cached response found.");
            }
            checkValidity = false;
        }

        if (this.expires == 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Not using cached response.");
            }
            this.response = null;
            checkValidity = false;
        }

        if (checkValidity && !checkValidity()) {
            this.response = null;
            // remove it if it no longer exists
            if (!this.source.exists()) {
                remove();
            }
        }

    }

    /**
     * Cleanup.
     */
    public void dispose() {
        this.source = null;
        this.manager = null;
        this.cache = null;
    }

    /**
     * Initialize the cached response with meta info.
     *
     * @throws IOException  if an the binary response could not be initialized
     */
    protected void initMetaResponse() throws IOException {
        boolean storeResponse = false;
        CachedSourceResponse response = this.response;
        if (response == null) {
            response = new CachedSourceResponse(getCacheValidities());
            storeResponse = true;
        }
        if (response.getExtra() == null) {
            response.setExtra(readMeta(this.source));
            this.freshMeta = true;
        }
        this.response = response;
        if (storeResponse) {
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
     * @throws IOException  if an the binary response could not be initialized
     */
    protected void initBinaryResponse() throws IOException {
        boolean storeResponse = false;
        /* delay caching the response until we have a valid new one */
        CachedSourceResponse response = this.response;
        if (response == null) {
            response = new CachedSourceResponse(getCacheValidities());
            storeResponse = true;
        }
        if (response.getBinaryResponse() == null) {
            response.setBinaryResponse(readBinaryResponse(this.source));
            if (!this.freshMeta) {
                /* always refresh meta in this case */
                response.setExtra(readMeta(this.source));
                this.freshMeta = true;
            }
        }
        this.response = response;
        if (storeResponse) {
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
            response = new CachedSourceResponse(getCacheValidities());
            storeResponse = true;
        }
        if (response.getXMLResponse() == null || refresh) {
            byte[] binary = response.getBinaryResponse();
            response.setXMLResponse(readXMLResponse(this.source, binary, this.manager));
            if (!this.freshMeta) {
                /* always refresh meta in this case */
                response.setExtra(readMeta(this.source));
                this.freshMeta = true;
            }
        }
        this.response = response;
        if (storeResponse) {
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
     * and content length. This method will try to refresh the
     * cached contents.
     */
    public void refresh() {
        this.source.refresh();
        if (response != null && checkValidity()) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Cached response is still valid for source " + this.uri + ".");
            }
        }
        else {
            if (this.source.exists()) {
                CachedSourceResponse response = this.response;
                try {
                    if (response == null) {
                        // create a new cached response
                        response = new CachedSourceResponse(new SourceValidity[] { 
                                new ExpiresValidity(getExpiration()), source.getValidity()});
                    }
                    // only create objects that are cached
                    if (response.getBinaryResponse() != null) {
                        response.setBinaryResponse(readBinaryResponse(source));
                    }
                    if (response.getXMLResponse() != null) {
                        response.setXMLResponse(readXMLResponse(
                                source, response.getBinaryResponse(), this.manager));
                    }
                    // always refresh meta data
                    response.setExtra(readMeta(source));
                    this.response = response;
                    cache.store(this.cacheKey, response);
                }
                catch (Exception e) {
                    getLogger().warn("Error refreshing source " + this.uri +
                        "Cached response (if any) may be stale.", e);
                }
            }
            else if (this.response != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Source " + this.uri + " no longer exists." +
                        " Throwing out cached response.");
                }
                remove();
            }
        }
    }
    
    protected void remove() {
        this.cache.remove(this.cacheKey);
    }
    
    // ---------------------------------------------------- XMLizable implementation

    /**
     * Generates SAX events representing the object's state.
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        try {
            initXMLResponse(false);
            XMLByteStreamInterpreter deserializer = new XMLByteStreamInterpreter();
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
    protected IdentifierCacheKey getCacheKey() {
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
    protected byte[] readXMLResponse(Source source, byte[] binary, ServiceManager manager)
    throws SAXException, IOException, CascadingIOException {
        XMLizer xmlizer = null;
        byte[] result = null;
        try {
            XMLByteStreamCompiler serializer = new XMLByteStreamCompiler();

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
    protected byte[] readBinaryResponse(Source source)
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
    protected final SourceMeta readMeta(Source source) throws IOException {
        SourceMeta meta = createMeta();
        initMeta(meta, source);
        return meta;
    }

    protected SourceMeta createMeta() {
        return new SourceMeta();
    }

    protected void initMeta(SourceMeta meta, Source source) throws IOException {
        final long lastModified = source.getLastModified();
        if (lastModified > 0) {
            meta.setLastModified(lastModified);
        }
        else {
            meta.setLastModified(System.currentTimeMillis());
        }
        meta.setMimeType(source.getMimeType());
    }

    private boolean checkValidity() {
        if (this.response == null) return false;

        final SourceValidity[] validities = this.response.getValidityObjects();
        boolean valid = true;
        if (! eventAware) {
            final ExpiresValidity expiresValidity = (ExpiresValidity) validities[0];
            final SourceValidity sourceValidity = validities[1];

            if (expiresValidity.isValid() != SourceValidity.VALID) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Cached response of source " + getSourceURI() + " is expired.");
                }
                if (!isValid(sourceValidity, source.getValidity())) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Cached response of source " + getSourceURI() + " is invalid.");
                    }
                    valid = false;
                }
                else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Cached response of source " + getSourceURI() + " is still valid.");
                    }
                    // set new expiration period
                    this.response.getValidityObjects()[0] = new ExpiresValidity(getExpiration());
                }
            }
            else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Cached response of source " + getSourceURI() + " is NOT expired.");
                }
            }
        } else {
            // assert(validities.length == 1 && validities[0] instanceof EventValidity)
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Cached response of source does not expire");
            }
        }
        return valid;
    }

    protected SourceValidity[] getCacheValidities() {
        if (this.cache instanceof EventAware) {
            // use event caching strategy, the associated event is the source uri
            return new SourceValidity[] { new EventValidity(new NamedEvent(this.source.getURI())) };
        }
        else {
            // we need to store both the cache expiration and the original source validity
            // the former is to determine whether to recheck the latter (see checkValidity)
            return new SourceValidity[] { new ExpiresValidity(getExpiration()), source.getValidity() };
        }
    }

    private static boolean isValid(SourceValidity oldValidity, SourceValidity newValidity) {
        return (oldValidity.isValid() == SourceValidity.VALID ||
                (oldValidity.isValid() == SourceValidity.UNKNOWN &&
                 oldValidity.isValid(newValidity) == SourceValidity.VALID));
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

	public void setCache(Cache cache) {
		this.cache = cache;
	}
}
