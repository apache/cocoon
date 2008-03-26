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
package org.apache.cocoon.blocks.fop;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.serialization.AbstractSerializer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;

/**
 * FOP 0.93 (and newer) based serializer.
 *  
 * @version $Id$
 */
public class FOPNGSerializer extends AbstractSerializer
                             implements Configurable, CacheableProcessingComponent,
                                        Serviceable, URIResolver, Disposable {

    protected SourceResolver resolver;

    /**
     * Factory to create fop objects
     */
    protected FopFactory fopfactory = FopFactory.newInstance();

    /**
     * The FOP instance.
     */
    protected Fop fop;

    /**
     * The current <code>mime-type</code>.
     */
    protected String mimetype;

    /**
     * Should we set the content length ?
     */
    protected boolean setContentLength = true;

    /**
     * Manager to get URLFactory from.
     */
    protected ServiceManager manager;
    private Map rendererOptions;


    /**
     * Set the component manager for this serializer.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        //should the content length be set
        this.setContentLength = conf.getChild("set-content-length").getValueAsBoolean(true);

        String configUrl = conf.getChild("user-config").getValue(null);


        if (configUrl != null) {
            Source configSource = null;
            SourceResolver resolver = null;
            try {
                resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
                configSource = resolver.resolveURI(configUrl);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Loading configuration from " + configSource.getURI());
                }
                SAXConfigurationHandler configHandler = new SAXConfigurationHandler();
                SourceUtil.toSAX(configSource, configHandler);
                fopfactory.setUserConfig(configHandler.getConfiguration());
            } catch (Exception e) {
                getLogger().warn("Cannot load configuration from " + configUrl);
                throw new ConfigurationException("Cannot load configuration from " + configUrl, e);
            } finally {
                if (resolver != null) {
                    resolver.release(configSource);
                    manager.release(resolver);
                }
            }
        }

        fopfactory.setURIResolver(this);

        // Get the mime type.
        this.mimetype = conf.getAttribute("mime-type");

        Configuration confRenderer = conf.getChild("renderer-config");
        if (confRenderer != null) {
            Configuration[] parameters = confRenderer.getChildren("parameter");
            if (parameters.length > 0) {
                rendererOptions = new HashMap();
                for (int i = 0; i < parameters.length; i++) {
                    String name = parameters[i].getAttribute("name");
                    String value = parameters[i].getAttribute("value");

                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("renderer " + String.valueOf(name) + " = " + String.valueOf(value));
                    }
                }
            }
        }
    }

    /**
     * Recycle serializer by removing references
     */
    public void recycle() {
        super.recycle();
        this.fop = null;
    }

    public void dispose() {
        if (this.resolver != null) {
            this.manager.release(this.resolver);
            this.resolver = null;
        }
        this.manager = null;
    }

    // -----------------------------------------------------------------

    /**
     * Return the MIME type.
     */
    public String getMimeType() {
        return mimetype;
    }

    /**
     * Create the FOP driver
     * Set the <code>OutputStream</code> where the XML should be serialized.
     * @throws IOException
     */
    public void setOutputStream(OutputStream out) throws IOException {

        // Give the source resolver to Batik which is used by FOP
        //SourceProtocolHandler.setup(this.resolver);

        FOUserAgent userAgent = fopfactory.newFOUserAgent();
        if (this.rendererOptions != null) {
            userAgent.getRendererOptions().putAll(this.rendererOptions);
        }
        try {
            this.fop = fopfactory.newFop(getMimeType(), userAgent, out);
            setContentHandler(this.fop.getDefaultHandler());
        } catch (FOPException e) {
            getLogger().error("FOP setup failed", e);
            throw new IOException("Unable to setup fop: " + e.getLocalizedMessage());
        }
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     * This method must be invoked before the generateValidity() method.
     *
     * @return The generated key or <code>0</code> if the component
     *              is currently not cacheable.
     */
    public Serializable getKey() {
        return "1";
    }

    /**
     * Generate the validity object.
     * Before this method can be invoked the generateKey() method
     * must be invoked.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return this.setContentLength;
    }

    //From URIResolver, copied from TraxProcessor
    public javax.xml.transform.Source resolve(String href, String base) throws TransformerException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("resolve(href = " + href + ", base = " + base + "); resolver = " + resolver);
        }

        StreamSource streamSource = null;
        Source source = null;
        try {
            if (base == null || href.indexOf(":") > 1) {
                // Null base - href must be an absolute URL
                source = resolver.resolveURI(href);
            } else if (href.length() == 0) {
                // Empty href resolves to base
                source = resolver.resolveURI(base);
            } else {
                // is the base a file or a real m_url
                if (!base.startsWith("file:")) {
                    int lastPathElementPos = base.lastIndexOf('/');
                    if (lastPathElementPos == -1) {
                        // this should never occur as the base should
                        // always be protocol:/....
                        return null; // we can't resolve this
                    } else {
                        source = resolver.resolveURI(base.substring(0, lastPathElementPos) + "/" + href);
                    }
                } else {
                    File parent = new File(base.substring(5));
                    File parent2 = new File(parent.getParentFile(), href);
                    source = resolver.resolveURI(parent2.toURL().toExternalForm());
                }
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("source = " + source + ", system id = " + source.getURI());
            }

            streamSource = new StreamSource(new ReleaseSourceInputStream(source.getInputStream(), source, resolver), source.getURI());
        } catch (SourceException e) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", e);
            }

            // CZ: To obtain the same behaviour as when the resource is
            // transformed by the XSLT Transformer we should return null here.
            return null;
        } catch (java.net.MalformedURLException mue) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", mue);
            }

            return null;
        } catch (IOException ioe) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Failed to resolve " + href + "(base = " + base + "), return null", ioe);
            }

            return null;
        } finally {
            // If streamSource is not null, the source should only be released when the input stream
            // is not needed anymore.
            if (streamSource == null)
                resolver.release(source);
        }
        return streamSource;
    }

    /**
     * An InputStream which releases the Cocoon/Avalon source from which the InputStream
     * has been retrieved when the stream is closed.
     */
    public static class ReleaseSourceInputStream extends InputStream {
        private InputStream delegate;
        private Source source;
        private SourceResolver sourceResolver;

        private ReleaseSourceInputStream(InputStream delegate, Source source, SourceResolver sourceResolver) {
            this.delegate = delegate;
            this.source = source;
            this.sourceResolver = sourceResolver;
        }

        public void close() throws IOException {
            delegate.close();
            sourceResolver.release(source);
        }

        public int read() throws IOException {
            return delegate.read();
        }

        public int read(byte b[]) throws IOException {
            return delegate.read(b);
        }

        public int read(byte b[], int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        public long skip(long n) throws IOException {
            return delegate.skip(n);
        }

        public int available() throws IOException {
            return delegate.available();
        }

        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        public synchronized void reset() throws IOException {
            delegate.reset();
        }

        public boolean markSupported() {
            return delegate.markSupported();
        }
    }
}
