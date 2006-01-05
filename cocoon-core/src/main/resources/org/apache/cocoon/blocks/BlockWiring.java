/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.excalibur.source.Source;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public class BlockWiring
    extends AbstractLogEnabled
    implements Configurable, Contextualizable { 

    private Context context;
    private URL contextRootURL;
    private String id;
    private String location;
    private Map connections = new HashMap();
    private Map properties = new HashMap();
    private Vector connectionNames;

    private String mountPath;
    private String sitemapPath;

    private Configuration componentConfiguration;
    private Configuration processorConfiguration;
    
    private boolean core = false;

    // Life cycle

    public void contextualize(Context context) throws ContextException {
        this.context = context;
        this.contextRootURL = (URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL);
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.id = config.getAttribute("id");
        this.location = config.getAttribute("location");
        this.mountPath = config.getChild("mount").getAttribute("path", null);

        getLogger().debug("BlockWiring configure: " +
                          " id=" + this.id +
                          " location=" + this.location +
                          " mountPath=" + this.mountPath);

        Configuration[] connections =
            config.getChild("connections").getChildren("connection");
        this.connectionNames = new Vector(connections.length);
        for (int i = 0; i < connections.length; i++) {
            Configuration connection = connections[i];
            String name = connection.getAttribute("name");
            this.connectionNames.add(name);
            String block = connection.getAttribute("block");
            this.connections.put(name, block);
            getLogger().debug("connection: " + " name=" + name + " block=" + block);
        }

        Configuration[] properties =
            config.getChild("properties").getChildren("property");
        for (int i = 0; i < properties.length; i++) {
            Configuration property = properties[i];
            String name = property.getAttribute("name");
            String value = property.getAttribute("value");
            this.properties.put(name, value);
            getLogger().debug("property: " + " name=" + name + " value=" + value);
        }

        // Read the block.xml file
        String blockPath = this.location + "COB-INF/block.xml";
        Source source = null;
        Configuration block = null;

        SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(this.getLogger());

        try {
            resolver.contextualize(this.context);
            source = resolver.resolveURI(blockPath);
	    // FIXME: Have used different locations for block.xml in the OSGi and the block stuff.
	    if (!source.exists()) {
		blockPath = this.location + "WEB-INF/block.xml";
		source = resolver.resolveURI(blockPath);
	    }
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            block = builder.build(source.getInputStream(), source.getURI());
        } catch (IOException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (SAXException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (ContextException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } finally {
            if (resolver != null) {
                resolver.release(source);
            }
        }

        properties =
            block.getChild("properties").getChildren("property");
        for (int i = 0; i < properties.length; i++) {
            Configuration property = properties[i];
            String name = property.getAttribute("name");
            String defaultVal = property.getChild("default").getValue(null);
            getLogger().debug("listing property: " + " name=" + name + " default=" + defaultVal);
            if (this.properties.get(name) == null && defaultVal != null) {
                // add default properties for those not set. This will
                // override values from the extended class, question
                // is if that is what we intend.
                this.properties.put(name, defaultVal);
                getLogger().debug("property: " + " name=" + name + " default=" + defaultVal);
            }
        }

        this.sitemapPath = block.getChild("sitemap").getAttribute("src", null);
        getLogger().debug("sitemapPath=" + this.sitemapPath);
        this.processorConfiguration = block.getChild("sitemap", false);

        this.componentConfiguration = block.getChild("components", false);
        this.core = block.getChild("components").getAttributeAsBoolean("core", false);
    }

    /**
     * Get the identifier of the block
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the URL of the root of the block
     */
    public URL getContextURL() throws MalformedURLException {
        URL contextURL = null;
        try {
            contextURL = ((new URI(this.contextRootURL.toExternalForm())).resolve(this.location)).toURL();
            getLogger().debug("Root URL " + contextRootURL);
            getLogger().debug("Block Root URL " + contextURL.toString());
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Couldn't create context URL from " + this.contextRootURL.toExternalForm() +
                " and " + this.location + " error: " + e.getMessage());
        }
        return contextURL;
    }

    /**
     * Get the names for the connections from this block. The name (super) of the super block is not included.
     */
    public Enumeration getConnectionNames() {
        return this.connectionNames.elements();
    }
        
    /**
     * Get a block id from the blockname.
     */
    public String getBlockId(String blockName) {
        String blockId = (String)this.connections.get(blockName);
        getLogger().debug("Resolving block: " + blockName + " to " + blockId);
        return blockId;
    }

    /**
     * Get a block property
     */
    public String getProperty(String name) {
        String value = (String)this.properties.get(name);
        getLogger().debug("Accessing property=" + name + " value=" + value + " block=" + this.id);
        return value;
    }

    /**
     * Get path where the block should be mounted
     */
    String getMountPath() {
        return this.mountPath;
    }

    /**
     * Get the component configuration of the block
     */
    Configuration getComponentConfiguration() {
        return this.componentConfiguration;
    }

    /**
     * Get the processor configuration of the block
     */
    Configuration getProcessorConfiguration() {
        return this.processorConfiguration;
    }

    /**
     * Is it the block containing the Core object.
     */
    public boolean isCore() {
        return this.core;
    }
}
