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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class BlockManager
    extends AbstractLogEnabled
    implements Configurable, Disposable, Serviceable { 

    public static String ROLE = BlockManager.class.getName();

    private ServiceManager manager;
    private SourceResolver resolver;

    private String id;
    private String location;
    private String mountPath;
    private String sitemapPath;
    private Map connections = new HashMap();
    private Map properties = new HashMap();

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.id = config.getAttribute("id");
        this.location = config.getAttribute("location");
        this.mountPath = config.getChild("mount").getAttribute("path", null);

        getLogger().debug("BlockManager configure: " +
                          " id=" + this.id +
                          " location=" + this.location +
                          " mountPath=" + this.mountPath);

        Configuration[] connections =
            config.getChild("connections").getChildren("connection");
        for (int i = 0; i < connections.length; i++) {
            Configuration connection = connections[i];
            this.connections.put(connection.getAttribute("name"),
                                 connection.getAttribute("block"));
            getLogger().debug("connection: " +
                              " name=" + connection.getAttribute("name") +
                              " block=" + connection.getAttribute("block"));
        }

        Configuration[] properties =
            config.getChild("properties").getChildren("property");
        for (int i = 0; i < properties.length; i++) {
            Configuration property = properties[i];
            this.properties.put(property.getAttribute("name"),
                                 property.getAttribute("value"));
            getLogger().debug("property: " +
                              " name=" + property.getAttribute("name") +
                              " value=" + property.getAttribute("value"));
        }

        // Read the block.xml file
        String blockPath = this.location + "/COB-INF/block.xml";
        Source source = null;
        Configuration block = null;

        try {
            source = this.resolver.resolveURI(blockPath);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            block = builder.build( source.getInputStream() );
        } catch (SAXException se) {
            String msg = "SAXException while reading " + blockPath + ": " + se.getMessage();
            throw new ConfigurationException(msg, se);
        } catch (IOException ie) {
              String msg = "IOException while reading " + blockPath + ": " + ie.getMessage();
              throw new ConfigurationException(msg, ie);
        } finally {
            this.resolver.release(source);
        }
        this.sitemapPath = block.getChild("sitemap").getAttribute("src");
        getLogger().debug("sitemapPath=" + this.sitemapPath);
    }

    public void dispose() {
        if (this.manager != null) {
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager = null;
        }
    }
}
