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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.ExtendedComponentSelector;
import org.apache.cocoon.components.ComponentLocator;
import org.apache.cocoon.components.pipeline.OutputComponentSelector;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapComponentSelector;
import org.apache.cocoon.transformation.Transformer;

/**
 * Component selector for sitemap components.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: ComponentsSelector.java,v 1.9 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public class ComponentsSelector extends ExtendedComponentSelector
                                implements OutputComponentSelector, SitemapComponentSelector {

    public static final int UNKNOWN     = -1;
    public static final int GENERATOR   = 0;
    public static final int TRANSFORMER = 1;
    public static final int SERIALIZER  = 2;
    public static final int READER      = 3;
    public static final int MATCHER     = 4;
    public static final int SELECTOR    = 5;
    public static final int ACTION      = 6;
    public static final int PIPELINE    = 7;

    public static final String[] SELECTOR_ROLES = {
        Generator.ROLE   + "Selector",
        Transformer.ROLE + "Selector",
        Serializer.ROLE  + "Selector",
        Reader.ROLE      + "Selector",
        Matcher.ROLE     + "Selector",
        Selector.ROLE    + "Selector",
        Action.ROLE      + "Selector",
        ProcessingPipeline.ROLE + "Selector"
    };

    public static final String[] COMPONENT_NAMES = {
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

    /** The mime-type for hints */
    private Map hintMimeTypes;

    /** The labels for hints */
    private Map hintLabels;

    /** The pipeline-hint Map */
    private Map pipelineHints;

    /** The set of known hints, used to add standard components (see ensureExists) */
    private Set knownHints = new HashSet();

    /** The parent selector, if it's of the current class */
    private SitemapComponentSelector parentSitemapSelector;
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.ParentAware#setParentInformation(org.apache.avalon.framework.component.ComponentManager, java.lang.String)
     */
    public void setParentLocator(ComponentLocator locator)
    throws ComponentException {
        super.setParentLocator(locator);

        if (super.parentSelector instanceof SitemapComponentSelector) {
            this.parentSitemapSelector = (SitemapComponentSelector)super.parentSelector;
        }
    }

    /**
     * Return the component instance name according to the selector role
     * (e.g. "action" for "org.apache.cocoon.acting.Action").
     */
    protected String getComponentInstanceName() {
        return (this.roleId == UNKNOWN) ? null : COMPONENT_NAMES[this.roleId];
    }

    /**
     * Get the attribute for class names. This is "src" for known roles, and
     * "class" (the default) for other roles.
     */
    protected String getClassAttributeName() {
        return (this.roleId == UNKNOWN) ? "class" : "src";
    }


    public void configure(Configuration config) throws ConfigurationException {
        
        // Who are we ?
        String role = getRoleName(config);
        this.roleId = UNKNOWN; // unknown
        for (int i = 0; i < SELECTOR_ROLES.length; i++) {
            if (SELECTOR_ROLES[i].equals(role)) {
                this.roleId = i;
                break;
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Setting up sitemap component selector for " +
                              role + " (role id = " + this.roleId + ")");
        }

        // Only matchers and serializers can have a MIME type
        if (this.roleId == SERIALIZER || this.roleId == READER) {
            this.hintMimeTypes = new HashMap();
        }

        this.hintLabels = new HashMap();
        this.pipelineHints = new HashMap();

        super.configure(config);
    }

    /**
     * Add a component in this selector. If needed, also register it's MIME type.
     */
    public void addComponent(Object hint, Class clazz, Configuration config) throws ComponentException {

        super.addComponent(hint, clazz, config);

        // Add to known hints
        this.knownHints.add(hint);

        if (this.roleId == SERIALIZER || this.roleId == READER) {
            // Get mime-type
            String mimeType = config.getAttribute("mime-type", null);
            if (mimeType != null) {
                this.hintMimeTypes.put(hint, mimeType);
            }
        }

        String label = config.getAttribute("label", null);
        if (label != null) {
            // Empty '' attribute will result in empty array,
            // overriding all labels on the component declared in the parent.
            StringTokenizer st = new StringTokenizer(label, " ,", false);
            String[] labels = new String[st.countTokens()];
            for (int i = 0; i < labels.length; i++) {
                labels[i] = st.nextToken();
            }
            this.hintLabels.put(hint, labels);
        }

        String pipelineHint = config.getAttribute("hint", null);
        this.pipelineHints.put(hint, pipelineHint);
    }

    /**
     * Ensure system-defined components exist (e.g. &lt;aggregator&gt;) and initialize
     * the selector.
     */
    public void initialize() /*throws Exception*/ {

        // FIXME : need to catch exceptions since ECS doesn't propagate the throws clause of Initializable
        try {

            DefaultConfiguration config = null;

            // Ensure all system-defined hints exist.
            // NOTE : checking this here means they can be user-defined in the sitemap
            switch(this.roleId) {
                case GENERATOR :

                    config = new DefaultConfiguration(COMPONENT_NAMES[GENERATOR], "autogenerated");
                    config.setAttribute("name", "<notifier>");
                    ensureExists("<notifier>",
                        org.apache.cocoon.sitemap.NotifyingGenerator.class, config);

                    config = new DefaultConfiguration(COMPONENT_NAMES[GENERATOR], "autogenerated");
                    config.setAttribute("name", "<aggregator>");
                    ensureExists("<aggregator>",
                        org.apache.cocoon.sitemap.ContentAggregator.class, config);
                break;

                case TRANSFORMER :
                    config = new DefaultConfiguration(COMPONENT_NAMES[TRANSFORMER], "autogenerated");
                    config.setAttribute("name", "<translator>");
                    ensureExists("<translator>",
                        org.apache.cocoon.sitemap.LinkTranslator.class, config);

                    config = new DefaultConfiguration(COMPONENT_NAMES[TRANSFORMER], "autogenerated");
                    config.setAttribute("name", "<gatherer>");
                    ensureExists("<gatherer>",
                        org.apache.cocoon.sitemap.LinkGatherer.class, config);
                break;
            }

            super.initialize();

            // Don't keep known hints (they're no more needed)
            this.knownHints = null;

        } catch(Exception e) {
            throw new CascadingRuntimeException("Cannot setup default components", e);
        }

    }

    /**
     * Ensure a component exists or add it otherwhise. We cannot simply call hasComponent()
     * since it requires to be initialized, and we want to add components, and this must
     * be done before initialization.
     */
    private void ensureExists(Object hint, Class clazz, Configuration config) throws ComponentException {

        if (! this.knownHints.contains(hint)) {
            this.addComponent(hint, clazz, config);
        }
    }

    /**
     * Get the MIME type for a given hint.
     */
    public String getMimeTypeForHint(Object hint) {

        if (this.hintMimeTypes == null) {
            // Not a component that has mime types
            return null;

        } else {
            if (this.hasDeclaredComponent(hint)) {
                return (String)this.hintMimeTypes.get(hint);
                
            } else if (this.parentSitemapSelector != null) {
                return this.parentSitemapSelector.getMimeTypeForHint(hint);
                
            } else {
                return null;
            }
        }
    }

    public boolean hasLabel(Object hint, String label) {
        String[] labels = this.getLabels(hint);
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(label))
                    return true;
            }
        }
        return false;
    }

    public String[] getLabels(Object hint) {
        // If this hint is declared locally, use its labels (if any), otherwise inherit
        // those of the parent.
        if (this.hasDeclaredComponent(hint)) {
            return (String[])this.hintLabels.get(hint);
            
        } else if (this.parentSitemapSelector != null) {
            return parentSitemapSelector.getLabels(hint);
            
        } else {
            return null;
        }
    }

    public String getPipelineHint(Object hint) {
        // If this hint is declared locally, use its hints (if any), otherwise inherit
        // those of the parent.
        if (this.hasDeclaredComponent(hint)) {
            return (String)this.pipelineHints.get(hint);
        } else if (this.parentSitemapSelector != null) {
            return this.parentSitemapSelector.getPipelineHint(hint);
        } else {
            return null;
        }
    }

    public void dispose() {
        super.dispose();
        this.parentSitemapSelector = null;
    }
}
