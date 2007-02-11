/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.ExpiresValidity;
import org.apache.excalibur.source.impl.validity.TimeStampValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.xmlizer.XMLizer;

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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This class implements a proxy like source that uses another source
 * to get the content. This implementation can cache the content for
 * a given period of time.
 *
 * <h2>Syntax for Protocol</h2>
 * <pre>
 *   cached:http://www.apache.org/[?cocoon:cache-expires=60&cocoon:cache-name=main]
 * </pre>
 *
 * <p>The above examples show how the real source <code>http://www.apache.org</code>
 * is wrapped and the cached contents is used for <code>60</code> seconds.
 * The second querystring parameter instructs that the cache key be extended with the string
 * <code>main</code>. This allows the use of multiple cache entries for the same source.</p>
 *
 * <p>The value of the expires parameter holds some additional semantics.
 * Specifying <code>-1</code> will yield the cached response to be considered valid
 * always. Value <code>0</code> can be used to achieve the exact opposite. That is to say,
 * the cached contents will be thrown out and updated immediately and unconditionally.<p>
 *
 * @version $Id$
 */
public class CachingSource extends AbstractLogEnabled
                           implements Serviceable, Initializable, XMLizable,
                                      Source {

    // TODO: Decouple from eventcache block.

    // ---------------------------------------------------- Constants

    public static final String CACHE_EXPIRES_PARAM = "cache-expires";
    public static final String CACHE_NAME_PARAM = "cache-name";

    private static final SourceMeta DUMMY = new SourceMeta();

    // ---------------------------------------------------- Instance variables

    /** The used protocol */
    final protected String protocol;

    /** The full URI string */
    final protected String uri;

    /** The full URI string of the underlying source */
    final protected String sourceUri;

    /** The source object for the real content */
    protected Source source;


    /** The ServiceManager */
    protected ServiceManager manager;

    /** The current cache */
    protected Cache cache;


    /** The cached response (if any) */
    private CachedSourceResponse response;

    /** Did we just update meta info? */
    private boolean freshMeta;

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
                         final String sourceUri,
                         final Source source,
                         final int expires,
                         final String cacheName,
                         final boolean async,
                         final boolean eventAware) {
        this.protocol = protocol;
        this.uri = uri;
        this.sourceUri = sourceUri;
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

    // ---------------------------------------------------- Lifecycle

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
        if (this.async && this.expires > 0 || this.expires == -1) {
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
        } else if (this.expires == 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Not using cached response.");
            }
            this.response = null;
            checkValidity = false;
        }

        if (checkValidity && !checkValidity()) {
            // remove invalid response
            clearResponse();
        }
    }

    /**
     * Cleanup.
     */
    public void dispose() {
        this.response = null;
        this.source = null;
        this.manager = null;
        this.cache = null;
    }

    // ---------------------------------------------------- CachedSourceResponse object management

    private CachedSourceResponse getResponse() {
        CachedSourceResponse response = this.response;
        if (response == null) {
            response = new CachedSourceResponse(getCacheValidities());
        }
        return response;
    }

    private void setResponse(CachedSourceResponse response) throws IOException {
        this.response = response;
        if (this.expires != 0) {
            try {
                this.cache.store(this.cacheKey, this.response);
            } catch (ProcessingException e) {
                throw new CascadingIOException("Failure storing response.", e);
            }
        }
    }

    private void clearResponse() {
        this.response = null;
        this.cache.remove(this.cacheKey);
    }

    /**
     * Initialize the cached response with meta info.
     *
     * @throws IOException  if an the binary response could not be initialized
     */
    protected SourceMeta getResponseMeta() throws IOException {
        CachedSourceResponse response = getResponse();

        if (response.getExtra() == null) {
            response.setExtra(readMeta(this.source));
            this.freshMeta = true;
            setResponse(response);
        }

        return (SourceMeta) response.getExtra();
    }

    /**
     * Initialize the cached response with meta and binary contents.
     *
     * @throws IOException  if an the binary response could not be initialized
     */
    protected byte[] getBinaryResponse() throws IOException {
        CachedSourceResponse response = getResponse();

        if (response.getBinaryResponse() == null) {
            if (!this.freshMeta) {
                /* always refresh meta in this case */
                response.setExtra(readMeta(this.source));
                this.freshMeta = true;
            }
            if (((SourceMeta) response.getExtra()).exists()) {
                response.setBinaryResponse(readBinaryResponse(this.source));
            }
            setResponse(response);
        }

        return response.getBinaryResponse();
    }

    /**
     * Initialize the cached response with meta, binary, and XML contents.
     *
     * @throws SAXException  if something happened during xml processing
     * @throws IOException  if an IO level error occured
     * @throws CascadingIOException  wraps all other exception types
     */
    protected byte[] getXMLResponse() throws SAXException, IOException, CascadingIOException {
        CachedSourceResponse response = getResponse();

        if (response.getXMLResponse() == null) {
            if (!this.freshMeta) {
                /* always refresh meta in this case */
                response.setExtra(readMeta(this.source));
                this.freshMeta = true;
            }
            if (((SourceMeta) response.getExtra()).exists()) {
                if (response.getBinaryResponse() == null) {
                    response.setBinaryResponse(readBinaryResponse(this.source));
                }
                response.setXMLResponse(readXMLResponse(this.source, response.getBinaryResponse(), this.manager));
            }
            setResponse(response);
        }

        return response.getXMLResponse();
    }

    private SourceMeta getMeta() {
        try {
            return getResponseMeta();
        } catch (IOException e) {
            // Could not initialize meta. Return default meta values.
            return DUMMY;
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
        return getMeta().getContentLength();
    }

    /**
     * Get the last modification date.
     * @return The last modification in milliseconds since January 1, 1970 GMT
     *         or 0 if it is unknown
     */
    public long getLastModified() {
        return getMeta().getLastModified();
    }

    /**
     * The mime-type of the content described by this object.
     * If the source is not able to determine the mime-type by itself
     * this can be null.
     */
    public String getMimeType() {
        return getMeta().getMimeType();
    }

    /**
     * Return an <code>InputStream</code> object to read from the source.
     */
    public InputStream getInputStream() throws IOException, SourceException {
        try {
            return new ByteArrayInputStream(getBinaryResponse());
        } catch (IOException e) {
            throw new SourceException("Failure getting input stream", e);
        }
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
        return getMeta().exists();
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
     *
     * This method will try to refresh the cached meta data
     * and content only if cached content is expired.
     */
    public void refresh() {
        if (response != null && checkValidity()) {
            return;
        }

        this.source.refresh();

        CachedSourceResponse response = getResponse();
        try {
            // always refresh meta data
            SourceMeta meta = readMeta(source);
            response.setExtra(meta);

            if (meta.exists()) {
                // only create objects that are cached
                if (response.getBinaryResponse() != null) {
                    response.setBinaryResponse(readBinaryResponse(source));
                }
                if (response.getXMLResponse() != null) {
                    response.setXMLResponse(readXMLResponse(source, response.getBinaryResponse(), this.manager));
                }
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Source " + this.uri + " does not exist.");
                }
                // clear cached data
                response.setBinaryResponse(null);
                response.setXMLResponse(null);
            }

            // Even if source does not exist, cache that fact.
            setResponse(response);
        } catch (Exception e) {
            getLogger().warn("Error refreshing source " + this.uri +
                             ". Cached response (if any) may be stale.", e);
        }
    }

    // ---------------------------------------------------- XMLizable implementation

    /**
     * Generates SAX events representing the object's state.
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        try {
            XMLByteStreamInterpreter deserializer = new XMLByteStreamInterpreter();
            if (contentHandler instanceof XMLConsumer) {
                deserializer.setConsumer((XMLConsumer) contentHandler);
            } else {
                deserializer.setConsumer(new ContentHandlerWrapper(contentHandler));
            }
            deserializer.deserialize(getXMLResponse());
        } catch (CascadingIOException e) {
            throw new SAXException(e.getMessage(), (Exception) e.getCause());
        } catch (IOException e) {
            throw new SAXException("Failure reading SAX response.", e);
        }
    }

    // ---------------------------------------------------- CachingSource specific accessors

    /**
     * Return the uri of the cached source.
     */
    protected String getSourceURI() {
        return this.sourceUri;
    }

    /**
     * Return the used key.
     */
    protected String getCacheKey() {
        return this.cacheKey.getKey();
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
        try {
            XMLByteStreamCompiler serializer = new XMLByteStreamCompiler();

            if (source instanceof XMLizable) {
                ((XMLizable) source).toSAX(serializer);
            } else {
                final String mimeType = source.getMimeType();
                if (mimeType != null) {
                    xmlizer = (XMLizer) manager.lookup(XMLizer.ROLE);
                    xmlizer.toSAX(new ByteArrayInputStream(binary),
                                  mimeType,
                                  source.getURI(),
                                  serializer);
                }
            }

            return (byte[]) serializer.getSAXFragment();
        } catch (ServiceException e) {
            throw new CascadingIOException("Missing service dependency.", e);
        } finally {
            if (xmlizer != null) {
                manager.release(xmlizer);
            }
        }
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
     */
    protected SourceMeta readMeta(Source source) throws SourceException {
        return new SourceMeta(source);
    }

    private boolean checkValidity() {
        if (this.response == null) {
            return false;
        }

        if (eventAware) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Cached response of source does not expire");
            }
            return true;
        }

        final SourceValidity[] validities = this.response.getValidityObjects();
        boolean valid = true;

        final ExpiresValidity expiresValidity = (ExpiresValidity) validities[0];
        final SourceValidity sourceValidity = validities[1];

        if (expiresValidity.isValid() != SourceValidity.VALID) {
            int validity = sourceValidity != null? sourceValidity.isValid() : SourceValidity.INVALID;
            if (validity == SourceValidity.INVALID ||
                    validity == SourceValidity.UNKNOWN &&
                            sourceValidity.isValid(source.getValidity()) != SourceValidity.VALID) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Response expired, invalid for " + getSourceURI());
                }
                valid = false;
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Response expired, still valid for " + getSourceURI());
                }
                // set new expiration period
                validities[0] = new ExpiresValidity(getExpiration());
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Response not expired for " + getSourceURI());
            }
        }

        return valid;
    }

    protected SourceValidity[] getCacheValidities() {
        if (this.cache instanceof EventAware) {
            // use event caching strategy, the associated event is the source uri
            return new SourceValidity[] { new EventValidity(new NamedEvent(this.source.getURI())) };
        } else {
            // we need to store both the cache expiration and the original source validity
            // the former is to determine whether to recheck the latter (see checkValidity)
            return new SourceValidity[] { new ExpiresValidity(getExpiration()), source.getValidity() };
        }
    }

    /**
     * Data holder for caching Source meta info.
     */
    protected static class SourceMeta implements Serializable {
        private boolean exists;
        private long contentLength;
        private String mimeType;
        private long lastModified;

        public SourceMeta() {
        }

        public SourceMeta(Source source) {
            setExists(source.exists());
            if (exists()) {
                setContentLength(source.getContentLength());
                final long lastModified = source.getLastModified();
                if (lastModified > 0) {
                    setLastModified(lastModified);
                } else {
                    setLastModified(System.currentTimeMillis());
                }
                setMimeType(source.getMimeType());
            } else {
                contentLength = -1;
            }
        }

        protected boolean exists() {
            return exists;
        }

        protected void setExists(boolean exists) {
            this.exists = exists;
        }

        protected long getContentLength() {
            return contentLength;
        }

        protected void setContentLength(long contentLength) {
            this.contentLength = contentLength;
        }

        protected long getLastModified() {
            return lastModified;
        }

        protected void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        protected String getMimeType() {
            return mimeType;
        }

        protected void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }
}
