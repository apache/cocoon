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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
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
 * @version $Id$
 */
public class ProcessorComponentInfo extends org.apache.cocoon.components.treeprocessor.ProcessorComponentInfo {
    
    protected static final String PIPELINE_PREFIX = ProcessingPipeline.ROLE + "/";
    
    protected static final Set DEFAULT_ROLES = new HashSet(Arrays.asList(new String[] {
            Generator.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Transformer.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Serializer.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Reader.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            ProcessingPipeline.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Matcher.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Selector.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT,
            Action.ROLE + "/" + DefaultServiceSelector.DEFAULT_HINT
    }));
    
    public ProcessorComponentInfo(ProcessorComponentInfo parent) {
        super(parent);
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
}