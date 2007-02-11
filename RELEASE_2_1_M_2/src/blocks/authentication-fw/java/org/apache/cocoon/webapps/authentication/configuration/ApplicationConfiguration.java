/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.webapps.authentication.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

/**
 * This object stores information about an application configuration
 * inside a handler configuration.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ApplicationConfiguration.java,v 1.1 2003/04/27 12:52:53 cziegeler Exp $
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
    public void configure(SourceResolver resolver, Configuration appconf)
    throws ProcessingException, SAXException, IOException, ConfigurationException {
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
    public String getName() { return name; }

    /**
     * Get the handler
     */
    public HandlerConfiguration getHandler() { return handler; }

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

    public boolean getLoadOnDemand() { return loadOnDemand; }

    /**
     * Get the configuration
     */
    public Configuration getConfiguration(String name) {
        return (Configuration)this.configurations.get(name);
    }

}
