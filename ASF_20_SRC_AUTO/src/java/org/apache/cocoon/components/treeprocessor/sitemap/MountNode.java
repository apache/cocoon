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
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: MountNode.java,v 1.11 2004/03/05 13:02:52 bdelacretaz Exp $
 */
public class MountNode extends AbstractProcessingNode implements Composable {

    /** The 'uri-prefix' attribute */
    private VariableResolver prefix;

    /** The 'src' attribute */
    private VariableResolver source;

    /** Processors for sources */
    private Map processors = new HashMap();

    /** The processor for this node */
    private TreeProcessor parentProcessor;

    /** The language for the mounted processor */
    private String language;

    /** The component manager to be used by the mounted processor */
    private ComponentManager manager;

    public MountNode(VariableResolver prefix, VariableResolver source, String language, TreeProcessor parentProcessor) {
        this.prefix = prefix;
        this.source = source;
        this.language = language;
        this.parentProcessor = parentProcessor;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {

        Map objectModel = env.getObjectModel();

        String resolvedSource = this.source.resolve(context, objectModel);
        TreeProcessor processor = getProcessor(resolvedSource);

        String resolvedPrefix = this.prefix.resolve(context, objectModel);

        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        String oldContext   = env.getContext();
        try {
            env.changeContext(resolvedPrefix, resolvedSource);

            if (context.isBuildingPipelineOnly()) {
                // Propagate pipelines
                ProcessingPipeline pp = processor.buildPipeline(env);
                if ( pp != null ) {
                    context.setProcessingPipeline( pp );
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
			env.setContext(oldPrefix, oldURI, oldContext);

            // Turning recomposing as a test, according to:
            // http://marc.theaimsgroup.com/?t=106802211400005&r=1&w=2
            // Recompose pipelines which may have been recomposed by subsitemap
            // context.recompose(this.manager);
        }
    }

    private synchronized TreeProcessor getProcessor(String source) throws Exception {

        TreeProcessor processor = (TreeProcessor)processors.get(source);

        if (processor == null) {
            // Handle directory mounts
            String actualSource;
            if (source.charAt(source.length() - 1) == '/') {
                actualSource = source + "sitemap.xmap";
            } else {
                actualSource = source;
            }
            
            SourceResolver resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
            try {
                Source src = resolver.resolveURI(actualSource);
                try {
                    processor = this.parentProcessor.createChildProcessor(this.manager, this.language, src);
                } finally {
                    resolver.release(src);
                }
            } finally {
                this.manager.release(resolver);
            }

            // Associate to the original source
            processors.put(source, processor);
        }

        return processor;
    }

    public void dispose() {
        Iterator iter = this.processors.values().iterator();
        while(iter.hasNext()) {
            ((TreeProcessor)iter.next()).dispose();
        }
    }
}
