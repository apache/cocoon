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
package org.apache.cocoon.components.cprocessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.fortress.impl.ComponentHandlerMetaData;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.lookup.FortressServiceManager;
import org.apache.avalon.fortress.util.CompositeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.cprocessor.sitemap.impl.GeneratorNode;
import org.apache.cocoon.components.cprocessor.sitemap.impl.ReaderNode;
import org.apache.cocoon.components.cprocessor.sitemap.impl.SerializerNode;
import org.apache.cocoon.components.cprocessor.sitemap.impl.TransformerNode;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.sitemap.ContentAggregator;
import org.apache.cocoon.sitemap.LinkGatherer;
import org.apache.cocoon.sitemap.LinkTranslator;
import org.apache.cocoon.sitemap.NotifyingGenerator;

/**
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 */
public class SitemapContainer extends DefaultContainer {
    
    private static final Map TYPES2ROLES = new HashMap(8);
    
    static {
        TYPES2ROLES.put("generators",GeneratorNode.ROLE);
        TYPES2ROLES.put("transformers",TransformerNode.ROLE);
        TYPES2ROLES.put("serializers",SerializerNode.ROLE);
        TYPES2ROLES.put("readers",ReaderNode.ROLE);
        TYPES2ROLES.put("matchers",Matcher.ROLE);
        TYPES2ROLES.put("selectors",Selector.ROLE);
        TYPES2ROLES.put("actions",Action.ROLE);
        TYPES2ROLES.put("pipes",ProcessingPipeline.ROLE);
    }
    
    private final Map m_defaultHints = new HashMap(8);
    
    public void configure(Configuration configuration) throws ConfigurationException {
        super.configure(configuration);
        Configuration meta = configuration.getChild("meta");
        
        // default component mappings
        Configuration[] defaults = meta.getChildren("default");
        for (int i = 0; i < defaults.length; i++) {
            String type = defaults[i].getAttribute("type");
            String hint = defaults[i].getAttribute("hint");
            String role = (String) TYPES2ROLES.get(type);
            m_defaultHints.put(role,hint);
        }
    }
    
    /**
     * FIXME: Only the root sitemap container needs to provide these.
     */
    public void initialize() throws CompositeException, Exception {
        
        addNativeComponent("<notifier>",NotifyingGenerator.class);
        addNativeComponent("<aggregator>",ContentAggregator.class);
        addNativeComponent("<translator>",LinkTranslator.class);
        addNativeComponent("<gatherer>",LinkGatherer.class);
        
        super.initialize();
    }
    
    public ProcessingNode getRootNode() throws ServiceException {
        return (ProcessingNode) super.getServiceManager().lookup(ProcessingNode.ROLE);
    }
    
    /**
     * Overide from super class to provide custom implementation.
     */
    protected ServiceManager provideServiceManager(ServiceManager parent)
        throws ServiceException {
        
        return new SitemapServiceManager(this,parent,m_defaultHints);
    }
    
    private void addNativeComponent(String hint, Class klass) throws ConfigurationException {
        final DefaultConfiguration config = 
            new DefaultConfiguration("component","autogenerated");
        config.setAttribute("name", hint);
        final ComponentHandlerMetaData metaData =
            new ComponentHandlerMetaData(hint,klass.getName(),config,
                ComponentHandlerMetaData.ACTIVATION_INLINE);
        try {
            addComponent(metaData);
        }
        catch (Exception e) {
            throw new ConfigurationException("Could not add component", e);
        }
    }
    
    /**
     * Custom service manager implementation that adds component default
     * management.
     */
    private static final class SitemapServiceManager extends FortressServiceManager {
        
        private final Map m_defaultHints;
        
        private SitemapServiceManager(SitemapContainer container, 
                                      ServiceManager   parent, 
                                      Map              defaultHints) {
            super(container,parent);
            m_defaultHints = defaultHints;
        }
        
        public boolean hasService(String role) {
            final int index = role.indexOf('/');
            if (index == -1) {
                final String defaultHint = (String) m_defaultHints.get(role);
                if (defaultHint != null) {
                    role += "/" + defaultHint;
                }
            }
            return super.hasService(role);
        }

        public Object lookup(String role) throws ServiceException {
            final int index = role.indexOf('/');
            if (index == -1) {
                final String defaultHint = (String) m_defaultHints.get(role);
                if (defaultHint != null) {
                    role += "/" + defaultHint;
                }
            }
            return super.lookup(role);
        }

    }

}
