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
package org.apache.cocoon.components.cprocessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.fortress.impl.ComponentHandlerMetaData;
import org.apache.avalon.fortress.impl.DefaultContainer;
import org.apache.avalon.fortress.impl.lookup.FortressServiceManager;
import org.apache.avalon.fortress.impl.lookup.FortressServiceSelector;
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
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.ContentAggregator;
import org.apache.cocoon.sitemap.LinkGatherer;
import org.apache.cocoon.sitemap.LinkTranslator;
import org.apache.cocoon.sitemap.NotifyingGenerator;
import org.apache.cocoon.transformation.Transformer;

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
        this.addSelector(Generator.ROLE);
        this.addSelector(Transformer.ROLE);
        this.addSelector(Serializer.ROLE);
        this.addSelector(Reader.ROLE);
        this.addSelector(Matcher.ROLE);
        this.addSelector(Selector.ROLE);
        this.addSelector(Action.ROLE);
        this.addSelector(ProcessingPipeline.ROLE);
    }
    
    /**
     * Add a selector for compatibility with 2.1.x
     */   
    protected void addSelector(String role) {
        final String selectorRole = role + "Selector";
        FortressServiceSelector fss = new FortressServiceSelector(this, selectorRole);
        Map hintMap = createHintMap();
        hintMap.put( DEFAULT_ENTRY, fss );
        hintMap.put( SELECTOR_ENTRY,
                    new FortressServiceSelector( this, selectorRole ) );
        m_mapper.put( selectorRole, hintMap );        
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
