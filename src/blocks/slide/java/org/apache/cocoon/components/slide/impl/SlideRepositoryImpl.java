/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:   "This product includes software
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

package org.apache.cocoon.components.slide.impl;

import java.util.Hashtable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.slide.SlideRepository;
import org.apache.cocoon.environment.Context;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.slide.common.Domain;
import org.apache.slide.common.EmbeddedDomain;
import org.apache.slide.common.NamespaceAccessToken;
import org.xml.sax.InputSource;

/**
 * The class represent a manger for slide repositories
 * 
 * IMPORTANT: do not remove Component interface as this cause ECM to
 * throw an exception during startup. (Due to dynamic proxy generation 
 * and the fact that JTA is not on the classpath).
 * 
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SlideRepositoryImpl.java,v 1.3 2003/12/08 18:07:42 unico Exp $
 */
public class SlideRepositoryImpl
implements SlideRepository, LogEnabled, Serviceable, Configurable,
Contextualizable, Disposable, Component, ThreadSafe  {

    /** The service manager instance */
    protected ServiceManager manager = null;

    /**
     * The SlideRepository will handle the domain lifecycle only,
     * if it is not already initialzed.
     */
    private EmbeddedDomain domain = null;

    private Logger logger;
    private String file;
    private boolean initialized = false;
    private String contextpath = null;

    /**
     * Provide service with a logger.
     *
     * @param logger the logger
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     *
     * @param manager Service manager.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get the context
     *
     * @param context
     */
    public void contextualize(org.apache.avalon.framework.context.Context context)
      throws ContextException {
        this.contextpath = ((Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT)).getRealPath("/");
    }

    /**
     * Pass the Configuration to the Configurable class. This method must
     * always be called after the constructor and before any other method.
     *
     * @param configuration the class configurations.
     */
    public void configure(Configuration configuration)
      throws ConfigurationException {

        this.file = configuration.getAttribute("file", "WEB-INF/slide.xconf");
    }

    /**
     * Initialize the service. Initialization includes
     * allocating any resources required throughout the
     * service's lifecycle.
     */
    public void initialize() throws Exception {

        SourceResolver resolver = null;
        SAXParser parser = null;
        Source source = null;
        Configuration configuration = null;

        if (Domain.isInitialized()) {
            this.logger.info("Domain already initialized.");
            return;
        }

        this.logger.info("Initializing domain.");

        this.domain = new EmbeddedDomain();
        // FIXME Could not remove deprecated method, because some important
        // messages were thrown over the domain logger
        domain.setLogger(new SlideLoggerAdapter(this.logger));

        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            SAXConfigurationHandler confighandler = new SAXConfigurationHandler();

            source = resolver.resolveURI(this.file);

            parser.parse(new InputSource(source.getInputStream()),
                         confighandler);

            configuration = confighandler.getConfiguration();

        } catch (Exception e) {
            this.logger.error("Could not load slide configuration file", e);
            return;
        } finally {
            if (source!=null) {
                resolver.release(source);
            }
            if (parser!=null) {
                this.manager.release(parser);
            }
            if (resolver!=null) {
                this.manager.release(resolver);
            }
        }

        try {
            
            domain.setDefaultNamespace(configuration.getAttribute("default","slide"));

            this.logger.info("Initializing Domain");

            Configuration[] namespaceDefinitions = configuration.getChildren("namespace");

            for (int i = 0; i<namespaceDefinitions.length; i++) {
                // Initializes a new namespace based on the given configuration data.

                this.logger.info("Initializing namespace : "+
                                 namespaceDefinitions[i].getAttribute("name"));

                String name = namespaceDefinitions[i].getAttribute("name");
                Configuration namespaceDefinition = namespaceDefinitions[i].getChild("definition");
                Configuration namespaceConfigurationDefinition = namespaceDefinitions[i].getChild("configuration");
                Configuration namespaceBaseDataDefinition = namespaceDefinitions[i].getChild("data");
                
                domain.addNamespace(name,
                                    new SlideLoggerAdapter(this.logger.getChildLogger(name)),
                                    new SlideConfigurationAdapter(namespaceDefinition),
                                    new SlideConfigurationAdapter(namespaceConfigurationDefinition),
                                    new SlideConfigurationAdapter(namespaceBaseDataDefinition));

                this.logger.info("Namespace configuration complete");
            }
            
            Configuration[] parameters = configuration.getChildren("parameter");
            Hashtable table = new Hashtable();

            for (int i = 0; i<parameters.length; i++) {
                String name = parameters[i].getAttribute("name");
                table.put(name, parameters[i].getValue(""));
            }
            table.put("contextpath", this.contextpath);
            this.domain.setParameters(table);
            
        } catch (ConfigurationException ce) {
            this.logger.error("Could not configure Slide domain", ce);
            return;
        }

        initialized = true;

        if (initialized) {
            try {
                domain.start();
            } catch (Exception e) {
                this.logger.error("Could not start domain", e);
            }
        }
    }

    /**
     * The dispose operation is called at the end of a service's lifecycle.
     * This method will be called after Startable.stop() method (if implemented
     * by service). Services use this method to release and destroy any
     * resources that the service owns.
     */
    public void dispose() {
        if (initialized) {
            try {
                domain.stop();
            } catch (Exception e) {
                this.logger.error("Could not stop domain", e);
            }
        }
    }

    /**
     * Returns a token for the access of the default namespace.
     *
     * @return NamespaceAccessToken Access token to the namespace
     */
    public NamespaceAccessToken getDefaultNamespaceToken() {
        // Initialization on demand
        if ((domain==null) && ( !initialized)) {
            try {
                initialize();
            } catch (Exception e) {
                this.logger.error("Could not initialize Slide repository", e);
            }
        }

        if (domain!=null) {
            return this.domain.getNamespaceToken(this.domain.getDefaultNamespace());
        }

        return Domain.accessNamespace(null, Domain.getDefaultNamespace());
    }

    /**
     * Returns a token for the access of a namespace.
     *
     * @param namespaceName Name of the namespace on which access is requested
     * @return NamespaceAccessToken Access token to the namespace
     */
    public NamespaceAccessToken getNamespaceToken(String namespaceName) {
        // Initialization on demand
        if ((domain==null) && ( !initialized)) {
            try {
                initialize();
            } catch (Exception e) {
                this.logger.error("Could not initialize Slide repository", e);
            }
        }

        if (namespaceName==null) {
            return getDefaultNamespaceToken();
        }

        if (domain!=null) {
            return this.domain.getNamespaceToken(namespaceName);
        }

        return Domain.accessNamespace(null, namespaceName);
    }
}
