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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
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
import org.xml.sax.InputSource;

/**
 * @version $Id$
 */
public class FOPNGSerializer extends AbstractSerializer implements
  Configurable, CacheableProcessingComponent, Serviceable, URIResolver, Disposable {

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
     * It is used to make sure that default Options loaded only once.
     */
    private static boolean configured = false;

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
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }
/*
    public void dispose() {
        this.manager.release(this.resolver);
    }
*/
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
     * Recycle serializer by removing references
     */
    public void recycle() {
        super.recycle();
        this.fop = null;
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

            InputSource is = getInputSource(source);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("source = " + source + ", system id = " + source.getURI());
            }

            return new StreamSource(is.getByteStream(), is.getSystemId());
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
            resolver.release(source);
        }
	}
	
    /**
     * Return a new <code>InputSource</code> object that uses the
     * <code>InputStream</code> and the system ID of the <code>Source</code>
     * object.
     * 
     * @throws IOException
     *             if I/O error occured.
     */
    private static InputSource getInputSource(final Source source) throws IOException, SourceException {
        final InputSource newObject = new InputSource(source.getInputStream());
        newObject.setSystemId(source.getURI());
        return newObject;
    }
	
	public void dispose() {
		if (null!=manager) {
			this.manager.release(this.resolver);
			this.manager = null;
		}
		this.resolver = null;
		
	}

}
