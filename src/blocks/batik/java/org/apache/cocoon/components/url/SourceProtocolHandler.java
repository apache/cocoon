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
package org.apache.cocoon.components.url;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.batik.ext.awt.image.spi.ImageTagRegistry;
import org.apache.batik.util.AbstractParsedURLProtocolHandler;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.ParsedURLData;
import org.apache.batik.util.ParsedURLProtocolHandler;
import org.apache.cocoon.CascadingIOException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * A Batik protocol handler that handles all Cocoon sources. This allows
 * &lt;svg:image xlink:href="..."/> to use any of the protocols handled by Cocoon.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: SourceProtocolHandler.java,v 1.3 2004/03/28 05:29:04 antonio Exp $
 */
public class SourceProtocolHandler extends AbstractParsedURLProtocolHandler {

    /** Thread-local source resolver */
    protected static InheritableThreadLocal localResolver = new InheritableThreadLocal();

    /** Batik's original default handler */        
    protected static ParsedURLProtocolHandler defaultHandler;

    /**
     * Change the default handler used by Batik to resolve URLs to a handler
     * based on <code>SourceResolver</code> and <code>SourceHandler</code>.
     * <p>
     * Note : Batik handlers are defined statically, and are thus shared by
     * all its users. However, this shouldn't be a problem since different
     * web applications live in different classloaders.
     *
     * @param manager the component manager used to get the <code>SourceHandler</code>
     * @param logger the logger for logging.
     */
    static {        
        // Keep the default handler, if any
        SourceProtocolHandler.defaultHandler = ParsedURL.getHandler(null);

        // Set the default handler to our handler
        ParsedURL.registerHandler(new SourceProtocolHandler(null));

        // Add a new image registry entry to handle image streams
        ImageTagRegistry.getRegistry().register(new StreamJDKRegistryEntry());
    }

    /**
     * Set the resolver to be used within the current thread.
     */
    public static void setup(SourceResolver resolver) {
        localResolver.set(resolver);
    }

    /**
     * Get the thread-local resolver.
     */
    public static SourceResolver getSourceResolver()
    {
        SourceResolver resolver = (SourceResolver)localResolver.get();
        return resolver;
    }

    //-------------------------------------------------------------------------

    public SourceProtocolHandler(String protocol) {
        super(protocol);
    }

    public ParsedURLData parseURL(ParsedURL baseURL, String urlStr) {
        SourceResolver resolver = (SourceResolver)localResolver.get();
        if (resolver == null) {
            // Fall back to the previous default handler
            return defaultHandler == null ? null : defaultHandler.parseURL(baseURL, urlStr);
        } else {
            return new SourceParsedURLData(urlStr, resolver);
        }
    }

    public ParsedURLData parseURL(String urlStr) {
        SourceResolver resolver = (SourceResolver)localResolver.get();
        if (resolver == null) {
            return defaultHandler == null ? null : defaultHandler.parseURL(urlStr);
        } else {
            return new SourceParsedURLData(urlStr, resolver);
        }
    }

    /**
     * Reimplementation of some methods of ParsedURLData since we cannot use <code>java.net.URL</code>.
     */
    static class SourceParsedURLData extends ParsedURLData {
        public String url;

        private Source source;
        private SourceResolver resolver;

        public SourceParsedURLData(String urlStr, SourceResolver resolver) {
            this.url = urlStr;
            this.resolver = resolver;
            
            // ParsedURLData has some public members which seems to be required to
            // have a value. This sucks.
            int pidx=0, idx;

            idx = urlStr.indexOf(':');
            if (idx != -1) {
                // May have a protocol spec...
                this.protocol = urlStr.substring(pidx, idx);
                if (this.protocol.indexOf('/') == -1) {
                    pidx = idx+1;
                } else {
                    // Got a slash in protocol probably means 
                    // no protocol given, (host and port?)
                    this.protocol = null;
                    pidx = 0;
                }
            }

            idx = urlStr.indexOf(',',pidx);
            if (idx != -1) {
                this.host = urlStr.substring(pidx, idx);
                pidx = idx+1;
            }
            if (pidx != urlStr.length()) { 
                this.path = urlStr.substring(pidx);
            }

            // Now do the real job

            // Setup source
            try {
                this.source = resolver.resolveURI(this.url);
            } catch(Exception e) {
                throw new CascadingRuntimeException("Cannot resolve " + this.url, e);
            }

            // Get Mime-type

            // First try the source itself
            this.contentType = this.source.getMimeType();

            if (this.contentType == null) {
                // Guess it from the URL extension
                if (url.endsWith(".gif")) {
                    this.contentType = "image/gif";
                } else if (url.endsWith(".jpeg") || url.endsWith(".jpg")) {
                    this.contentType = "image/jpeg";
                } else if (url.endsWith(".png")) {
                    this.contentType = "image/png";
                }
            }
        }

        public boolean complete() {
            return (this.url != null);
        }

        public String getPortStr() {
            String portStr = protocol+":";
            if (host != null) portStr += host;
            portStr += ",";
            return portStr;
        }

        public String toString() {
            return this.url;
        }

        /**
         * Open a stream for the data. If the thread-local <code>SourceResolver</code> exists,
         * then use it, otherwise fall back to <code>SourceHandler</code>.
         */
        protected InputStream openStreamInternal (String userAgent, Iterator mimeTypes, Iterator encodingTypes)
            throws IOException {

            try {
                return source.getInputStream();
            } catch(Exception e) {
                throw new CascadingIOException("Cannot open URL " + this.url, e);
            } finally {
                this.resolver.release(this.source);
            }
        }
    }
}
