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
package org.apache.cocoon.components.treeprocessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.core.container.DefaultServiceSelector;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.transformation.Transformer;

/**
 * Holds informations defined in &lt;map:components&gt; such as default hint, labels and mime-types
 * that are needed when building a processor and to manage inheritance when building child processors.
 * <p>
 * In previous versions of the sitemap engine, these informations where store in specialized
 * extensions of ComponentSelector (<code>SitemapComponentSelector</code> and
 * <code>OutputComponentSelector</code>), which led to a strong dependency on the chosen component
 * container implementation. This is now a regular component that also "listens" to modifications
 * of the service manager when it is built.
 * 
 * @version CVS $Id$
 */
public class ProcessorComponentInfo {
    
    public static final String ROLE = ProcessorComponentInfo.class.getName();
    
    private static final String GENERATOR_PREFIX = Generator.ROLE + "/";
    private static final String TRANSFORMER_PREFIX = Transformer.ROLE + "/";
    private static final String SERIALIZER_PREFIX = Serializer.ROLE + "/";
    private static final String READER_PREFIX = Reader.ROLE + "/";
    private static final String PIPELINE_PREFIX = ProcessingPipeline.ROLE + "/";
    
    private static final Set DEFAULT_ROLES = new HashSet(Arrays.asList(new String[] {
            Generator.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Transformer.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Serializer.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Reader.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            ProcessingPipeline.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Matcher.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Selector.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Action.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT
    }));
    
    /** Component info for the parent processor */
    ProcessorComponentInfo parent;
    
    /** The service manager for this processor */
    private ServiceManager manager;
    
    /** Lock that prevents further modification */
    private boolean locked = false;
    
    /**
     * Component-related data (see methods below for key names). We use a single Map
     * to reduce memory usage, as each kind of data has a limited number of entries.
     */
    private Map data;
    
    public ProcessorComponentInfo(ProcessorComponentInfo parent) {
        this.parent = parent;
    }
    
    /**
     * Grabs on the fly the sitemap-related information on generators, transformers,
     * serializers and readers when they're declared in the <code>ServiceManager</code>.
     * <p>
     * This method is triggered when a component is added on a <code>CocoonServiceManager</code>.
     * 
     * @param role the component's role
     * @param clazz the component's class
     * @param config the component's configuration
     */
    public void componentAdded(String role, String clazz, Configuration config) {
        if (role.startsWith(GENERATOR_PREFIX)) {
            setupLabelAndPipelineHint(role, config);

        } else if (role.startsWith(TRANSFORMER_PREFIX)) {
            setupLabelAndPipelineHint(role, config);

        } else if (role.startsWith(SERIALIZER_PREFIX)) {
            setupLabelAndPipelineHint(role, config);
            setupMimeType(role, config);

        } else if (role.startsWith(READER_PREFIX)) {
            setupMimeType(role, config);
        }
    }
    
    /**
     * Prepares the configuration for pooled sitemap components:
     * Per default pooled components are proxied - we override this
     * for generators, transformers, serializers, readers and pipes
     * @param role the component's role
     * @param clazz the component's class
     * @param config the component's configuration
     */
    public void prepareConfig(String role, String clazz, Configuration config) {
        if (role.startsWith(GENERATOR_PREFIX)
            || role.startsWith(TRANSFORMER_PREFIX)
            || role.startsWith(SERIALIZER_PREFIX)
            || role.startsWith(READER_PREFIX)
            || role.startsWith(PIPELINE_PREFIX)) {
            
            ((DefaultConfiguration)config).setAttribute("model", ComponentInfo.TYPE_NON_THREAD_SAFE_POOLED);
        }
    }

    public void roleAliased(String existingRole, String newRole) {
        if (DEFAULT_ROLES.contains(newRole)) {
            // A default role for a sitemap component has been added
            int pos = existingRole.indexOf('/');
            String role = existingRole.substring(0, pos);
            String hint = existingRole.substring(pos+1);
            
            this.setDefaultType(role, hint);
        }
    }
    
    private void setupLabelAndPipelineHint(String role, Configuration config) {

        // Labels
        String label = config.getAttribute("label", null);
        if (label != null) {
            StringTokenizer st = new StringTokenizer(label, " ,", false);
            String[] labels = new String[st.countTokens()];
            for (int tokenIdx = 0; tokenIdx < labels.length; tokenIdx++) {
                labels[tokenIdx] = st.nextToken();
            }
            setLabels(role, labels);
        } else {
            // Set no labels, overriding those defined in the parent sitemap, if any
            setLabels(role, null);
        }

        // Pipeline hints
        String pipelineHint = config.getAttribute("hint", null);
        setPipelineHint(role, pipelineHint);
    }

    private void setupMimeType(String role, Configuration config) {
        setMimeType(role, config.getAttribute("mime-type", null));
    }

    /** Store some data, creating the storage map if needed */
    private void setData(String key, Object value) {
        if (locked) throw new IllegalStateException("ProcessorComponentInfo is locked");
        if (this.data == null) this.data = new HashMap();
        this.data.put(key, value);
    }
    
    /** Get some data, asking the parent if needed */
    private Object getData(String key) {
        // Need to check containsKey as the default hint may be unspecified (i.e. no "default" attribute)
        if (this.data != null && this.data.containsKey(key)) {
            return this.data.get(key);
        } else if (this.parent != null) {
            // Ask parent
            return this.parent.getData(key);
        } else {
            return null;
        }
    }
    
    /**
     * Lock this component info object at the end of processor building to prevent
     * any further changes.
     */
    public void lock() {
        this.locked = true;
    }
    
    private void setDefaultType(String role, String hint) {
        setData("defaultType/" + role, hint);
    }
    
    public String getDefaultType(String role) {
        return (String)getData("defaultType/" + role);
    }
    
    private void setPipelineHint(String role, String hint) {
        setData("pipelineHint/" + role, hint);
    }
    
    public String getPipelineHint(String role, String type) {
        return (String)getData("pipelineHint/" + role + "/" + type);
    }
    
    private void setMimeType(String role, String mimeType) {
        setData("mimeType/" + role, mimeType);
    }
    
    public String getMimeType(String role, String type) {
        return (String)getData("mimeType/" + role + "/" + type);
    }
    
    private void setLabels(String role, String[] labels) {
        setData("labels/" + role, labels);
    }
    
    public String[] getLabels(String role, String type) {
        return (String[])getData("labels/" + role + "/" + type);
    }
    
    public boolean hasLabel(String role, String type, String label) {
        String[] labels = this.getLabels(role, type);
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equals(label))
                    return true;
            }
        }
        return false;
    }
}