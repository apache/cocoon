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
package org.apache.cocoon.components.cprocessor.sitemap.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.TreeProcessor;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: MountNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=mount-node
 */
public class MountNode extends AbstractProcessingNode 
implements ProcessingNode, Contextualizable, Disposable {

    /** The 'uri-prefix' attribute */
    private VariableResolver m_prefix;

    /** The 'src' attribute */
    private VariableResolver m_source;
    
    /** The value of the 'check-reload' attribute */
    private boolean m_checkReload;
    
    /** Processors for sources */
    private Map m_processors = new HashMap();

    /** The processor for this node */
    private TreeProcessor m_parentProcessor;

    public MountNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        try {
            m_prefix = VariableResolverFactory.getResolver(
                config.getAttribute("uri-prefix"),super.m_manager);
            m_source = VariableResolverFactory.getResolver(
                config.getAttribute("src"),super.m_manager);
            m_checkReload = config.getAttributeAsBoolean("check-reload",true);
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        m_parentProcessor = (TreeProcessor) context.get(TreeProcessor.CONTEXT_TREE_PROCESSOR);
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        Map objectModel = env.getObjectModel();

        String resolvedSource = m_source.resolve(context, objectModel);
        String resolvedPrefix = m_prefix.resolve(context, objectModel);

        TreeProcessor processor = this.getProcessor(resolvedSource, resolvedPrefix);

        final String oldPrefix = env.getURIPrefix();
        final String oldURI    = env.getURI();
        try {
            processor.getEnvironmentHelper().changeContext(env);

            if (context.isBuildingPipelineOnly()) {
                // Propagate pipelines
                Processor.InternalPipelineDescription pp = processor.buildPipeline(env);
                if (pp != null) {
                    context.setInternalPipelineDescription(pp);
                    return true;
                } else {
                    return false;
                }
            } else {
                // Processor will create its own pipelines
                return processor.process(env);
            }
        } finally {
            // Restore context
            env.setURI(oldPrefix, oldURI);

            // Turning recomposing as a test, according to:
            // http://marc.theaimsgroup.com/?t=106802211400005&r=1&w=2
            // Recompose pipelines which may have been recomposed by subsitemap
            // context.recompose(this.manager);
        }
    }

    /**
     * Get the processor for the sub sitemap
     * FIXME Better synchronization strategy
     */
    private synchronized TreeProcessor getProcessor(String source, String prefix) 
    throws Exception {
        // FIXME - source is only relative, so we might get into name clashes, or?
        TreeProcessor processor = (TreeProcessor) m_processors.get(source);
        if (processor == null) {
            // Handle directory mounts
            String actualSource;
            if (source.charAt(source.length() - 1) == '/') {
                actualSource = source + "sitemap.xmap";
            } else {
                actualSource = source;
            }
            
            processor = this.m_parentProcessor.createChildProcessor(actualSource, m_checkReload, prefix);

            // Associate to the original source
            m_processors.put(source, processor);
        }

        return processor;
    }

    public void dispose() {
        Iterator iter = this.m_processors.values().iterator();
        while(iter.hasNext()) {
            ContainerUtil.dispose(iter.next());
        }
    }

}
