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
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.core.container.DefaultServiceSelector;
import org.apache.cocoon.generation.GeneratorFactory;
import org.apache.cocoon.serialization.SerializerFactory;
import org.apache.cocoon.transformation.TransformerFactory;

/**
 * Component selector for sitemap components.
 *
 * @version CVS $Id$
 */
public class ComponentsSelector extends DefaultServiceSelector {

    private static final int UNKNOWN     = -1;
    private static final int GENERATOR   = 0;
    private static final int TRANSFORMER = 1;
    private static final int SERIALIZER  = 2;
    private static final int READER      = 3;
    private static final int MATCHER     = 4;
    private static final int SELECTOR    = 5;
    private static final int ACTION      = 6;
    private static final int PIPELINE    = 7;

//    /** Role names, ordered as the constants above */
//    public static final String[] SELECTOR_ROLES = {
//        Generator.ROLE   + "Selector",
//        Transformer.ROLE + "Selector",
//        Serializer.ROLE  + "Selector",
//        Reader.ROLE      + "Selector",
//        Matcher.ROLE     + "Selector",
//        Selector.ROLE    + "Selector",
//        Action.ROLE      + "Selector",
//        ProcessingPipeline.ROLE + "Selector"
//    };

    /** Configuration element names, used to find the role */
    private static final String[] CONFIG_NAMES = {
        "generators",
        "transformers",
        "serializers",
        "readers",
        "matchers",
        "selectors",
        "actions",
        "pipes"
    };

    /** Names of children elements, according to role */
    private static final String[] COMPONENT_NAMES = {
        "generator",
        "transformer",
        "serializer",
        "reader",
        "matcher",
        "selector",
        "action",
        "pipe"
    };

    /** The role as an integer */
    private int roleId;

//    /** The set of known hints, used to add standard components (see ensureExists) */
//    private Set knownHints = new HashSet();

    /**
     * Return the component instance name according to the selector role
     * (e.g. "action" for "org.apache.cocoon.acting.Action").
     */
    protected String getComponentInstanceName() {
        return COMPONENT_NAMES[this.roleId];
    }

    /**
     * Get the attribute for class names. This is "src" for known roles, and
     * "class" (the default) for other roles.
     */
    protected String getClassAttributeName() {
        return "src";
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        // Who are we ?
        final String configName = config.getName();
        this.roleId = UNKNOWN; // unknown
        for (int i = 0; i < CONFIG_NAMES.length; i++) {
            if (CONFIG_NAMES[i].equals(configName)) {
                this.roleId = i;
                break;
            }
        }
        
        if (this.roleId == UNKNOWN) {
            throw new ConfigurationException("ComponentsSelector is reserved for sitemap components. Illegal use at " +
                    config.getLocation());
        }

        super.configure(config);
    }

//    /**
//     * Add a component in this selector.
//     */
//    public void addComponent(String key, Class clazz, Configuration config) throws ServiceException {
//        super.addComponent(key, clazz, config);
//
//        // Add to known hints. This is needed as we cannot call isSelectable() if initialize()
//        // has not been called, and we cannot add components once it has been called...
//        this.knownHints.add(key);
//    }

// Now defined in cocoon.roles
//    /**
//     * Ensure system-defined components exist (e.g. &lt;aggregator&gt;) and initialize
//     * the selector.
//     */
//    public void initialize() throws Exception {
//        DefaultConfiguration config = null;
//
//        // Ensure all system-defined hints exist.
//        // NOTE : checking this here means they can be user-defined in the sitemap
//        switch(this.roleId) {
//            case GENERATOR :
//                config = new DefaultConfiguration(COMPONENT_NAMES[GENERATOR], "autogenerated");
//                config.setAttribute("name", "<notifier>");
//                ensureExists("<notifier>",
//                             org.apache.cocoon.sitemap.NotifyingGenerator.class, config);
//
//                config = new DefaultConfiguration(COMPONENT_NAMES[GENERATOR], "autogenerated");
//                config.setAttribute("name", "<aggregator>");
//                ensureExists("<aggregator>",
//                             org.apache.cocoon.sitemap.ContentAggregator.class, config);
//            break;
//
//            case TRANSFORMER :
//                config = new DefaultConfiguration(COMPONENT_NAMES[TRANSFORMER], "autogenerated");
//                config.setAttribute("name", "<translator>");
//                ensureExists("<translator>",
//                             org.apache.cocoon.sitemap.LinkTranslator.class, config);
//
//                config = new DefaultConfiguration(COMPONENT_NAMES[TRANSFORMER], "autogenerated");
//                config.setAttribute("name", "<gatherer>");
//                ensureExists("<gatherer>",
//                             org.apache.cocoon.sitemap.LinkGatherer.class, config);
//            break;
//        }
//
//        super.initialize();
//
//        // Don't keep known hints (they're no more needed)
//        this.knownHints = null;
//    }
//
//    /**
//     * Ensure a component exists or add it otherwhise. We cannot simply call hasComponent()
//     * since it requires to be initialized, and we want to add components, and this must
//     * be done before initialization.
//     */
//    private void ensureExists(String key, Class clazz, Configuration config) throws ServiceException {
//        if (!this.knownHints.contains(key)) {
//            if (this.parentSelector == null || !this.parentSelector.isSelectable(key)) {
//                this.addComponent(key, clazz, config);
//            }
//        }
//    }

    /**
     * Override parent to implement support for {@link GeneratorFactory},
     * {@link TransformerFactory}, and {@link SerializerFactory}.
     */
    public Object select(Object hint) throws ServiceException {
        final Object component = super.select(hint);

        switch (this.roleId) {
            case GENERATOR:
                if (component instanceof GeneratorFactory) {
                    return ((GeneratorFactory)component).getInstance();
                }
                break;
            case TRANSFORMER:
                if (component instanceof TransformerFactory) {
                    return ((TransformerFactory)component).getInstance();
                }
                break;
            case SERIALIZER:
                if (component instanceof SerializerFactory) {
                    return ((SerializerFactory)component).getInstance();
                }
                break;
        }

        return component;
    }

    /**
     * Override parent to implement support for {@link GeneratorFactory},
     * {@link TransformerFactory}, and {@link SerializerFactory}.
     */
    public void release(Object component) {

        // If component is an Instance returned by Factory, get the Factory.
        switch (this.roleId) {
            case GENERATOR:
                if (component instanceof GeneratorFactory.Instance) {
                    // Dispose component, if needed
                    ContainerUtil.dispose(component);
                    component = ((GeneratorFactory.Instance)component).getFactory();
                }
                break;
            case TRANSFORMER:
                if (component instanceof TransformerFactory.Instance) {
                    // Dispose component, if needed
                    ContainerUtil.dispose(component);
                    component = ((TransformerFactory.Instance)component).getFactory();
                }
                break;
            case SERIALIZER:
                if (component instanceof SerializerFactory.Instance) {
                    // Dispose component, if needed
                    ContainerUtil.dispose(component);
                    component = ((SerializerFactory.Instance)component).getFactory();
                }
                break;
        }

        super.release(component);
    }
}
