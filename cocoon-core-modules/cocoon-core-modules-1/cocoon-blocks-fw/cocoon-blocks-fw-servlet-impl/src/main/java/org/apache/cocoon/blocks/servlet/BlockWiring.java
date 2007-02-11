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
package org.apache.cocoon.blocks.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public class BlockWiring
    extends AbstractLogEnabled
    implements Configurable{ 

    private URL contextURL;
    private String id;
    private Dictionary connections = new Hashtable();
    private Dictionary properties = new Hashtable();
    private Vector connectionNames;

    private String mountPath;

    private Configuration componentConfiguration;
    private boolean hasServlet;
    private String servletClass;
    private Configuration servletConfiguration;
        
    /**
     * @param contextURL The contextURL to set.
     */
    public void setContextURL(URL contextURL) {
        this.contextURL = contextURL;
    }

    
    // Life cycle

    public void configure(Configuration config)
        throws ConfigurationException {
        this.id = config.getAttribute("id");
        this.mountPath = config.getChild("mount").getAttribute("path", null);

        getLogger().debug("BlockWiring configure: " +
                          " id=" + this.id +
                          " location=" + this.contextURL +
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
        URL blockURL;
        String blockPath = this.contextURL + BlockConstants.BLOCK_CONF.substring(1);
        Configuration block = null;

        try {
            blockURL = new URL(this.contextURL, BlockConstants.BLOCK_CONF.substring(1));
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            block = builder.build(blockURL.openStream(), blockURL.toExternalForm());
        } catch (IOException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (SAXException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
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

        Configuration servletElement = block.getChild("servlet", false);
        this.hasServlet = servletElement != null;
        if (this.hasServlet) {
            this.servletClass = servletElement.getAttribute("class");
            this.servletConfiguration = servletElement.getChildren()[0];            
        }

        this.componentConfiguration = block.getChild("components", false);
    }

    /**
     * Get the identifier of the block
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Get the names for the connections from this block. The name (super) of the super block is not included.
     */
    public Enumeration getConnectionNames() {
        return this.connectionNames.elements();
    }
        
    /**
     * Get connections
     */
    public Dictionary getConnections() {
        return this.connections;
    }

    /**
     * Get the block properties
     */
    public Dictionary getProperties() {
        return this.properties;
    }

    /**
     * Get path where the block should be mounted
     */
    public String getMountPath() {
        return this.mountPath;
    }

    /**
     * Get the component configuration of the block
     */
    public Configuration getComponentConfiguration() {
        return this.componentConfiguration;
    }

    /**
     * Is there a servlet in the block
     * 
     * @return Returns whether there is a servlet in the block.
     */
    public boolean hasServlet() {
        return this.hasServlet;
    }


    /**
     * The class of the block servlet
     * 
     * @return Returns the servletClass.
     */
    public String getServletClass() {
        return this.servletClass;
    }


    /**
     * Get the servlet configuration of the block
     */
    public Configuration getServletConfiguration() {
        return this.servletConfiguration;
    }
}
