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
package org.apache.cocoon.serialization;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Options;
import org.apache.fop.configuration.ConfigurationParser;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.render.Renderer;

import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.renderer.ExtendableRendererFactory;
import org.apache.cocoon.components.renderer.RendererFactory;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.core.container.spring.logger.LoggerUtils;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;

/**
 * FOP 0.20.5 (and older) based serializer.
 * 
 * @version $Id$
 */
public class FOPSerializer extends AbstractSerializer
                           implements Configurable, CacheableProcessingComponent, Serviceable {

    /**
     * The Renderer Factory to use
     */
    protected static final RendererFactory factory = ExtendableRendererFactory.getRendererFactoryImplementation();

    /**
     * The <code>Driver</code> which is FOP.
     */
    protected Driver driver;

    /**
     * The current <code>Renderer</code>.
     */
    protected Renderer renderer;

    /**
     * The current <code>mime-type</code>.
     */
    protected String mimetype;

    /**
     * The renderer name if configured
     */
    protected String rendererName;

    /**
     * Should we set the content length ?
     */
    protected boolean setContentLength = true;

    /**
     * This Avalon logger is created for FOP
     */
    protected Logger logger;

    /**
     * It is used to make sure that default Options loaded only once.
     */
    private static boolean configured;

    /**
     * Manager to get URLFactory from.
     */
    protected ServiceManager manager;

    /**
     * Set the component manager for this serializer.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.logger = new CLLoggerWrapper(LoggerUtils.getChildLogger(manager, "fop"));
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {

        MessageHandler.setScreenLogger(this.logger);

        // FIXME: VG: Initialize static FOP configuration with defaults, only once.
        // FOP has static config, but that's going to change in the near future.
        // Then this code should be reviewed.
        synchronized (FOPSerializer.class) {
            if (!configured) {
                try {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Loading default configuration");
                    }
                    new Options();
                } catch (Exception e) {
                    getLogger().error("Cannot load default configuration. Proceeding.", e);
                }
                configured = true;
            }
        }

        this.setContentLength = conf.getChild("set-content-length").getValueAsBoolean(true);

        // Old syntax: Attribute src of element user-config contains file
        String configUrl = conf.getChild("user-config").getAttribute("src", null);
        if (configUrl != null) {
            getLogger().warn("Attribute src of user-config element is deprecated. "
                             + "Provide Cocoon URI as value of the element instead");
            try {
                // VG: Old version of serializer supported only files
                configUrl = new File(configUrl).toURL().toExternalForm();
            } catch (MalformedURLException e) {
                getLogger().warn("Can not load config file " + configUrl, e);
                configUrl = null;
            }
        } else {
            // New syntax: Element user-config contains URL
            configUrl = conf.getChild("user-config").getValue(null);
        }

        if (configUrl != null) {
            Source configSource = null;
            SourceResolver resolver = null;
            try {
                resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
                configSource = resolver.resolveURI(configUrl);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Loading configuration from " + configSource.getURI());
                }
                SourceUtil.toSAX(configSource, new ConfigurationParser());
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

        // Get the mime type.
        this.mimetype = conf.getAttribute("mime-type");

        // Iterate through the parameters, looking for a renderer reference
        Configuration[] parameters = conf.getChildren("parameter");
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getAttribute("name");
            if ("renderer".equals(name)) {
                this.rendererName = parameters[i].getAttribute("value");
                try {
                    this.renderer = (Renderer)ClassUtils.newInstance(rendererName);
                } catch (Exception ex) {
                    getLogger().error("Cannot load  class " + rendererName, ex);
                    throw new ConfigurationException("Cannot load class " + rendererName, ex);
                }
            }
        }
        if (this.renderer == null) {
            // Using the Renderer Factory, get the default renderer
            // for this MIME type.
            this.renderer = factory.createRenderer(mimetype);
        }

        // Do we have a renderer yet?
        if (this.renderer == null ) {
            throw new ConfigurationException(
                "Could not autodetect renderer for FOPSerializer and "
                + "no renderer was specified in the sitemap configuration."
            );
        }

        Configuration confRenderer = conf.getChild("renderer-config");
        if (confRenderer != null) {
            parameters = confRenderer.getChildren("parameter");
            if (parameters.length > 0) {
                Map rendererOptions = new HashMap(); 
                for (int i = 0; i < parameters.length; i++) {
                    String name = parameters[i].getAttribute("name");
                    String value = parameters[i].getAttribute("value");
                
                    if (getLogger().isDebugEnabled()) {
                    	getLogger().debug("renderer " + String.valueOf(name) + " = " + String.valueOf(value));
                    }
                    rendererOptions.put(name,value);
                }
                this.renderer.setOptions(rendererOptions);
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
     */
    public void setOutputStream(OutputStream out) {
        
        // Give the source resolver to Batik which is used by FOP
        //SourceProtocolHandler.setup(this.resolver);

        // load the fop driver
        this.driver = new Driver();
        this.driver.setLogger(this.logger);
        if (this.rendererName == null) {
            this.renderer = factory.createRenderer(mimetype);
        } else {
            try {
                this.renderer = (Renderer)ClassUtils.newInstance(this.rendererName);
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("Cannot load class " + this.rendererName, e);
                }
                throw new CascadingRuntimeException("Cannot load class " + this.rendererName, e);
            }
        }
        this.driver.setRenderer(this.renderer);
        this.driver.setOutputStream(out);
        setContentHandler(this.driver.getContentHandler());
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
        this.driver = null;
        this.renderer = null;
    }

    /**
     * Test if the component wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return this.setContentLength;
    }

}
