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
package org.apache.cocoon.webapps.authentication.configuration;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.SourceParameters;

/**
 * This object stores information about an application configuration
 * inside a handler configuration.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ApplicationConfiguration.java,v 1.4 2004/03/19 13:59:22 cziegeler Exp $
*/
public final class ApplicationConfiguration
implements java.io.Serializable {

    /** The unique name of the handler */
    private String name;

    /** The load resource (optional) */
    private String loadResource;

    /** The save resource (optional) */
    private String saveResource;

    /** The load resource parameters (optional) */
    private SourceParameters loadResourceParameters;

    /** The save resource parameters (optional) */
    private SourceParameters saveResourceParameters;

    /** Is the application loaded on demand */
    private boolean loadOnDemand = false;

    /** The corresponding handler */
    private HandlerConfiguration handler;

    /** The configuration fragments */
    private Map configurations;

    /** Save the context on logout */
    private boolean saveOnLogout = false;

    /**
     * Construct a new application handler
     */
    public ApplicationConfiguration(HandlerConfiguration handler, String name)
    throws ProcessingException {
        this.handler = handler;
        this.name = name;
        if (name.indexOf('_') != -1
            || name.indexOf(':') != -1
            || name.indexOf('/') != -1) {
           throw new ProcessingException("application name must not contain one of the characters ':','_' or '/'.");
        }
        this.configurations = new HashMap(3, 2);
    }

    /**
     * Configure an application
     */
    public void configure(Configuration appconf)
    throws ConfigurationException {
        Configuration child = null;

        // test for loadondemand attribute
        this.loadOnDemand = appconf.getAttributeAsBoolean("loadondemand", false);

        // get load resource (optinal)
        child = appconf.getChild("load", false);
        if (child != null) {
            this.loadResource = child.getAttribute("uri");
            this.loadResourceParameters = SourceParameters.create(child);
        }

        // get save resource (optional)
        child =  appconf.getChild("save", false);
        if (child != null) {
            this.saveResource = child.getAttribute("uri");
            this.saveResourceParameters = SourceParameters.create(child);
            this.saveOnLogout = child.getAttributeAsBoolean("saveOnLogout", false);
        }

        // get configurations (optional)
        Configuration[] configurations = appconf.getChildren("configuration");
        if (configurations != null) {
            for(int i = 0; i < configurations.length; i++) {
                child = configurations[i];
                String value = child.getAttribute("name");
                if (this.getConfiguration(value) != null) {
                    throw new ConfigurationException("Configuration names must be unique for application " + this.name + ": " + value);
                }
                this.configurations.put(value, child);
            }
        }
    }

    /**
     * Get the application name.
     */
    public String getName() { 
        return this.name; 
    }

    /**
     * Get the handler
     */
    public HandlerConfiguration getHandler() { 
        return this.handler; 
    }

    /**
     * Get the load resource
     */
    public String getLoadResource() {
        return this.loadResource;
    }

    /**
     * Get the save resource
     */
    public String getSaveResource() {
        return this.saveResource;
    }

    /**
     * Get the load resource parameters
     */
    public SourceParameters getLoadResourceParameters() {
        return this.loadResourceParameters;
    }

    /**
     * Get the save resource parameters
     */
    public SourceParameters getSaveResourceParameters() {
        return this.saveResourceParameters;
    }

    /** Should we save on logout? */
    public boolean saveOnLogout() { 
        return this.saveOnLogout;
    }

    public boolean getLoadOnDemand() { 
        return loadOnDemand; 
    }

    /**
     * Get the configuration
     */
    public Configuration getConfiguration(String name) {
        return (Configuration)this.configurations.get(name);
    }

}
