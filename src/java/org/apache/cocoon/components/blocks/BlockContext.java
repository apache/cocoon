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
package org.apache.cocoon.components.blocks;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class BlockContext
    extends AbstractLogEnabled
    implements Configurable, Contextualizable, Disposable, Serviceable { 

    private ServiceManager serviceManager;
    private Context context;
    private BlocksManager blocksManager;

    private String id;
    private String location;
    private String superId;
    private Map connections = new HashMap();
    private Map properties = new HashMap();

    private String mountPath;
    private String sitemapPath;

    private Configuration componentConfiguration;
    private Configuration processorConfiguration;

    // Life cycle

    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.id = config.getAttribute("id");
        this.location = config.getAttribute("location");
        this.mountPath = config.getChild("mount").getAttribute("path", null);

        getLogger().debug("BlockContext configure: " +
                          " id=" + this.id +
                          " location=" + this.location +
                          " mountPath=" + this.mountPath);

        Configuration[] connections =
            config.getChild("connections").getChildren("connection");
        for (int i = 0; i < connections.length; i++) {
            Configuration connection = connections[i];
            String name = connection.getAttribute("name");
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
        SourceResolver resolver = null;
        Source source = null;
        Configuration block = null;

        try {
            resolver = 
                (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(blockPath);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            block = builder.build(source.getInputStream(), source.getURI());
        } catch (ServiceException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (IOException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (SAXException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } finally {
            if (resolver != null) {
                resolver.release(source);
                this.serviceManager.release(resolver);
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

        this.sitemapPath = block.getChild("sitemap").getAttribute("src");
        getLogger().debug("sitemapPath=" + this.sitemapPath);
        this.processorConfiguration = block.getChild("sitemap");

        this.componentConfiguration = block.getChild("components");
    }

    public void dispose() {
        this.serviceManager = null;
    }

    // Block methods

    // The blocks manager should not be available within a block so I
    // didn't want to make it part of the parent manager. But this is
    // a little bit clumsy. Question is what components, if any, the
    // blocks should have in common.
    public void setBlocksManager(BlocksManager blocksManager) {
        this.blocksManager = blocksManager;
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
    public String getContextURL() throws URISyntaxException, ContextException {
        String contextRootURL = ((URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL)).toExternalForm();
        getLogger().debug("Root URL " + contextRootURL);
        String contextURL = ((new URI(contextRootURL)).resolve(this.location)).toString();
        getLogger().debug("Block Root URL " + contextURL);

        return contextURL;
    }

    /**
     * Get a block from the blockname.
     */
    public Block getBlock(String blockName) {
        String blockId = (String)this.connections.get(blockName);
        getLogger().debug("Resolving block: " + blockName + " to " + blockId);
        return blockId != null ? (Block)this.blocksManager.getBlock(blockId) : null;
    }


    /**
     * Get a block property
     */
    public String getProperty(String name) {
        String value = (String)this.properties.get(name);
        getLogger().debug("Accessing property=" + name + " value=" + value + " block=" + this.id);
        if (value == null) {
            // Ask the super block for the property
            getLogger().debug("Try super property=" + name + " block=" + this.superId);
            Block block = this.getBlock(Block.SUPER);
            if (block != null) {
                value =  block.getProperty(name);
            }
        }
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
}
