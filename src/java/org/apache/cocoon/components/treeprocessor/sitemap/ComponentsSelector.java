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
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;

import org.apache.cocoon.components.pipeline.OutputComponentSelector;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.ExtendedComponentSelector;

import org.apache.cocoon.acting.Action;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapComponentSelector;
import org.apache.cocoon.transformation.Transformer;

import java.util.*;

/**
 * Component selector for sitemap components.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ComponentsSelector.java,v 1.1 2003/03/09 00:09:21 pier Exp $
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

    public void setParentSelector(ComponentSelector selector) {
        super.setParentSelector(selector);

        if (selector instanceof SitemapComponentSelector) {
            this.parentSitemapSelector = (SitemapComponentSelector)selector;
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

        // How are we ?
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
            return null;

        } else {
            String mimeType = (String)this.hintMimeTypes.get(hint);

            if (mimeType != null) {
                return mimeType;

            } else if (this.parentSitemapSelector != null) {
                return this.parentSitemapSelector.getMimeTypeForHint(hint);

            } else {
                return null;
            }
        }
    }

    public boolean hasLabel(Object hint, String label) {
        String[] labels = (String[])this.hintLabels.get(hint);
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(label))
                    return true;
            }
        } else if (parentSitemapSelector != null) {
            return parentSitemapSelector.hasLabel(hint, label);
        }
        return false;
    }

    public String[] getLabels(Object hint) {
        String[] labels = (String[])this.hintLabels.get(hint);
        // Labels can be inherited or completely overrided
        if (labels == null && parentSitemapSelector != null) {
            return parentSitemapSelector.getLabels(hint);
        }
        return labels;
    }

    public String getPipelineHint(Object hint) {
        String pipelineHint = (String)this.pipelineHints.get(hint);
        // Pipeline-hints can be inherited or completely overrided
        if (pipelineHint == null && parentSitemapSelector != null) {
            return parentSitemapSelector.getPipelineHint(hint);
        }
        return pipelineHint;
    }

}
