/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

package org.apache.cocoon.components.slide.impl;

import java.util.Hashtable;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
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
 * @version CVS $Id: SlideRepositoryImpl.java,v 1.7 2004/04/13 15:16:37 unico Exp $
 */
public class SlideRepositoryImpl extends AbstractLogEnabled
implements SlideRepository, Contextualizable, Serviceable, Configurable, 
Initializable, Disposable, ThreadSafe  {

    private ServiceManager manager;

    /**
     * The SlideRepository will handle the domain lifecycle only,
     * if it is not already initialzed.
     */
    private EmbeddedDomain domain = null;

    private String file;
    private String contextpath;
    private String workdir;


    public SlideRepositoryImpl() {
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public void contextualize(org.apache.avalon.framework.context.Context context)
      throws ContextException {
        Context ctx = ((Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT));
        this.contextpath = ctx.getRealPath("/");
        this.workdir = context.get(Constants.CONTEXT_WORK_DIR).toString();
    }

    public void configure(Configuration configuration)
      throws ConfigurationException {

        this.file = configuration.getAttribute("file", "WEB-INF/slide.xconf");
    }

    public void initialize() throws Exception {

        if (Domain.isInitialized()) {
            return;
        }

        getLogger().info("Initializing domain.");

        this.domain = new EmbeddedDomain();
        // FIXME Could not remove deprecated method, because some important
        // messages were thrown over the domain logger
        domain.setLogger(new SlideLoggerAdapter(getLogger()));

        SourceResolver resolver = null;
        SAXParser parser = null;
        Source source = null;
        Configuration configuration = null;
        try {
            resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);

            parser = (SAXParser) this.manager.lookup(SAXParser.ROLE);
            SAXConfigurationHandler confighandler = new SAXConfigurationHandler();

            source = resolver.resolveURI(this.file);
            parser.parse(new InputSource(source.getInputStream()),confighandler);
            configuration = confighandler.getConfiguration();

        } finally {
            if (source != null) {
                resolver.release(source);
            }
            if (parser != null) {
                this.manager.release(parser);
            }
            if (resolver != null) {
                this.manager.release(resolver);
            }
        }
        
        Configuration[] parameters = configuration.getChildren("parameter");
        Hashtable table = new Hashtable();
        for (int i = 0; i < parameters.length; i++) {
            String name = parameters[i].getAttribute("name");
            table.put(name, parameters[i].getValue(""));
        }
        table.put("contextpath", this.contextpath);
        table.put("workdir", this.workdir);
        this.domain.setParameters(table);
        
        domain.setDefaultNamespace(configuration.getAttribute("default","cocoon"));
        Configuration[] namespace = configuration.getChildren("namespace");

        for (int i = 0; i< namespace.length; i++) {
            String name = namespace[i].getAttribute("name");
            Configuration definition = namespace[i].getChild("definition");
            Configuration config = namespace[i].getChild("configuration");
            Configuration data = namespace[i].getChild("data");
            
            getLogger().info("Initializing namespace: " + name);
            
            domain.addNamespace(name,
                                new SlideLoggerAdapter(getLogger().getChildLogger(name)),
                                new SlideConfigurationAdapter(definition),
                                new SlideConfigurationAdapter(config),
                                new SlideConfigurationAdapter(data));

        }
        
        domain.start();
    }

    public void dispose() {
        try {
            domain.stop();
        } catch (Exception e) {
            getLogger().error("Could not stop domain", e);
        }
    }

    /**
     * Returns a token for the access of the default namespace.
     *
     * @return NamespaceAccessToken Access token to the namespace
     */
    public NamespaceAccessToken getDefaultNamespaceToken() {

        if (domain != null) {
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
        
        if (namespaceName == null) {
            return getDefaultNamespaceToken();
        }

        if (domain != null) {
            return this.domain.getNamespaceToken(namespaceName);
        }

        return Domain.accessNamespace(null, namespaceName);
    }
}
