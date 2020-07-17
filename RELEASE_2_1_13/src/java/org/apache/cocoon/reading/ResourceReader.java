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
package org.apache.cocoon.reading;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpResponse;
import org.apache.cocoon.util.ByteRange;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ResourceReader</code> component is used to serve binary data
 * in a sitemap pipeline. It makes use of HTTP Headers to determine if
 * the requested resource should be written to the <code>OutputStream</code>
 * or if it can signal that it hasn't changed.
 *
 * <p>Configuration:
 * <dl>
 *   <dt>&lt;expires&gt;</dt>
 *   <dd>This parameter is optional. When specified it determines how long
 *       in miliseconds the resources can be cached by any proxy or browser
 *       between Cocoon and the requesting visitor. Defaults to -1.
 *   </dd>
 *   <dt>&lt;quick-modified-test&gt;</dt>
 *   <dd>This parameter is optional. This boolean parameter controls the
 *       last modified test. If set to true (default is false), only the
 *       last modified of the current source is tested, but not if the
 *       same source is used as last time
 *       (see http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=102921894301915 )
 *   </dd>
 *   <dt>&lt;byte-ranges&gt;</dt>
 *   <dd>This parameter is optional. This boolean parameter controls whether
 *       Cocoon should support byterange requests (to allow clients to resume
 *       broken/interrupted downloads).
 *       Defaults to true.
 * </dl>
 *
 * <p>Default configuration:
 * <pre>
 *   &lt;expires&gt;-1&lt;/expires&gt;
 *   &lt;quick-modified-test&gt;false&lt;/quick-modified-test&gt;
 *   &lt;byte-ranges&gt;true&lt;/byte-ranges&gt;
 * </pre>
 *
 * <p>In addition to reader configuration, above parameters can be passed
 * to the reader at the time when it is used.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id$
 */
public class ResourceReader extends AbstractReader
                            implements CacheableProcessingComponent, Configurable {

    /**
     * The list of generated documents
     */
    private static final Map documents = new HashMap();
    private static final Object documentsLock = new Object();

    protected long configuredExpires;
    protected boolean configuredQuickTest;
    protected int configuredBufferSize;
    protected boolean configuredByteRanges;

    protected long expires;
    protected boolean quickTest;
    protected int bufferSize;
    protected boolean byteRanges;

    protected Response response;
    protected Request request;
    protected Source inputSource;

    /**
     * Read reader configuration
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        // VG Parameters are deprecated as of 2.2.0-Dev/2.1.6-Dev
        final Parameters parameters = Parameters.fromConfiguration(configuration);
        this.configuredExpires = parameters.getParameterAsLong("expires", -1);
        this.configuredQuickTest = parameters.getParameterAsBoolean("quick-modified-test", false);
        this.configuredBufferSize = parameters.getParameterAsInteger("buffer-size", 8192);
        this.configuredByteRanges = parameters.getParameterAsBoolean("byte-ranges", true);

        // Configuration has precedence over parameters.
        this.configuredExpires = configuration.getChild("expires").getValueAsLong(configuredExpires);
        this.configuredQuickTest = configuration.getChild("quick-modified-test").getValueAsBoolean(configuredQuickTest);
        this.configuredBufferSize = configuration.getChild("buffer-size").getValueAsInteger(configuredBufferSize);
        this.configuredByteRanges = configuration.getChild("byte-ranges").getValueAsBoolean(configuredByteRanges);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(Parameters)
     */
    public void parameterize(Parameters parameters) throws ParameterException {
    }

    /**
     * Setup the reader.
     * The resource is opened to get an <code>InputStream</code>,
     * the length and the last modification date
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);

        this.expires = par.getParameterAsLong("expires", this.configuredExpires);
        this.quickTest = par.getParameterAsBoolean("quick-modified-test", this.configuredQuickTest);
        this.bufferSize = par.getParameterAsInteger("buffer-size", this.configuredBufferSize);
        this.byteRanges = par.getParameterAsBoolean("byte-ranges", this.configuredByteRanges);

        try {
            this.inputSource = resolver.resolveURI(src);
        } catch (SourceException e) {
            throw SourceUtil.handle("Error during resolving of '" + src + "'.", e);
        }
        setupHeaders();
    }

    /**
     * Setup the response headers: Accept-Ranges, Expires
     */
    protected void setupHeaders() {
        // Tell the client whether we support byte range requests or not
        if (byteRanges) {
            response.setHeader("Accept-Ranges", "bytes");
        } else {
            response.setHeader("Accept-Ranges", "none");
        }

        if (expires > 0) {
            response.setDateHeader("Expires", System.currentTimeMillis() + expires);
        } else if (expires == 0) {
            response.setDateHeader("Expires", 0);
        }
    }

    /**
     * Recyclable
     */
    public void recycle() {
        this.request = null;
        this.response = null;
        if (this.inputSource != null) {
            super.resolver.release(this.inputSource);
            this.inputSource = null;
        }
        super.recycle();
    }

    /**
     * @return True if byte ranges support is enabled and request has range header.
     */
    protected boolean hasRanges() {
        return this.byteRanges && this.request.getHeader("Range") != null;
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public Serializable getKey() {
        return inputSource.getURI();
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        if (hasRanges()) {
            // This is a byte range request so we can't use the cache, return null.
            return null;
        } else {
            return inputSource.getValidity();
        }
    }

    /**
     * @return the time the read source was last modified or 0 if it is not
     *         possible to detect
     */
    public long getLastModified() {
        if (hasRanges()) {
            // This is a byte range request so we can't use the cache, return null.
            return 0;
        }

        if (quickTest) {
            return inputSource.getLastModified();
        }

        synchronized (documentsLock) {
            final String systemId = (String) documents.get(request.getRequestURI());
            if (systemId == null || inputSource.getURI().equals(systemId)) {
                return inputSource.getLastModified();
            }

            documents.remove(request.getRequestURI());
        }
        return 0;
    }

    protected void processStream(InputStream inputStream)
    throws IOException, ProcessingException {
        byte[] buffer = new byte[bufferSize];
        int length = -1;

        String ranges = request.getHeader("Range");

        ByteRange byteRange;
        if (byteRanges && ranges != null) {
            try {
                ranges = ranges.substring(ranges.indexOf('=') + 1);
                byteRange = new ByteRange(ranges);
            } catch (NumberFormatException e) {
                byteRange = null;

                // TC: Hm.. why don't we have setStatus in the Response interface ?
                if (response instanceof HttpResponse) {
                    // Respond with status 416 (Request range not satisfiable)
                    ((HttpResponse)response).setStatus(416);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("malformed byte range header [" + String.valueOf(ranges) + "]");
                    }
                }
            }
        } else {
            byteRange = null;
        }

        long contentLength = inputSource.getContentLength();

        if (byteRange != null) {
            ByteRange actualByteRange = byteRange;
            String entityLength;
            String entityRange;
            if (contentLength != -1) {
                entityLength = "" + contentLength;
                actualByteRange = byteRange.intersection(new ByteRange(0, contentLength - 1));
                entityRange = actualByteRange.toString();
            } else {
                entityLength = "*";
                entityRange = byteRange.toString();
            }

            response.setHeader("Content-Range", "bytes " + entityRange + "/" + entityLength);

            if (actualByteRange.length() != -1) {
                response.setHeader("Content-Length", String.valueOf(actualByteRange.length()));
            }
            if (response instanceof HttpResponse) {
                // Response with status 206 (Partial content)
                ((HttpResponse)response).setStatus(206);
            }

            int pos = 0;
            int posEnd;
            while ((length = inputStream.read(buffer)) > -1) {
                posEnd = pos + length - 1;
                ByteRange intersection = byteRange.intersection(new ByteRange(pos, posEnd));
                if (intersection != null) {
                    out.write(buffer, (int) intersection.getStart() - pos, (int) intersection.length());
                }
                pos += length;
            }
        } else {
            if (contentLength != -1) {
                response.setHeader("Content-Length", Long.toString(contentLength));
            }

            while ((length = inputStream.read(buffer)) > -1) {
                out.write(buffer, 0, length);
            }
        }

        out.flush();
    }

    /**
     * Generates the requested resource.
     */
    public void generate()
    throws IOException, ProcessingException {
        try {
            InputStream inputStream;
            try {
                inputStream = inputSource.getInputStream();
            } catch (SourceException e) {
                throw SourceUtil.handle("Error during resolving of the input stream", e);
            }

            // Bugzilla Bug #25069: Close inputStream in finally block.
            try {
                processStream(inputStream);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }

            if (!quickTest) {
                // if everything is ok, add this to the list of generated documents
                // (see http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=102921894301915 )
                synchronized (documentsLock) {
                    documents.put(request.getRequestURI(), inputSource.getURI());
                }
            }
        } catch (IOException e) {
            // COCOON-2307: if the client severed the connection, no matter for it that we rethrow the exception as it will never receive it
            getLogger().debug("Received an IOException, assuming client severed connection on purpose");
            throw e;
        }
    }

    /**
     * Returns the mime-type of the resource in process.
     */
    public String getMimeType() {
        Context ctx = ObjectModelHelper.getContext(objectModel);
        if (ctx != null) {
            final String mimeType = ctx.getMimeType(source);
            if (mimeType != null) {
                return mimeType;
            }
        }
        return inputSource.getMimeType();
    }
}
