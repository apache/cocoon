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
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: SlideRepositoryImpl.java,v 1.5 2004/01/20 11:12:31 unico Exp $
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
