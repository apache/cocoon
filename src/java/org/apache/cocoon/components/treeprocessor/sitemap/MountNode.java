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

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: MountNode.java,v 1.13 2004/05/25 07:28:25 cziegeler Exp $
 */
public class MountNode extends AbstractProcessingNode {

    /** The 'uri-prefix' attribute */
    private final VariableResolver prefix;

    /** The 'src' attribute */
    private final VariableResolver source;

    /** Processors for sources */
    private Map processors = new HashMap();

    /** The processor for this node */
    private final TreeProcessor parentProcessor;

    /** The value of the 'check-reload' attribute */
    private final boolean checkReload;

    public MountNode(VariableResolver prefix, 
                     VariableResolver source, 
                     TreeProcessor parentProcessor,
                     boolean checkReload) {
        this.prefix = prefix;
        this.source = source;
        this.parentProcessor = parentProcessor;
        this.checkReload = checkReload;
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {

        Map objectModel = env.getObjectModel();

        String resolvedSource = this.source.resolve(context, objectModel);
        String resolvedPrefix = this.prefix.resolve(context, objectModel);

        if (resolvedSource.length()==0) {
            throw new ProcessingException("Source of mount statement is empty"); 
        }
        TreeProcessor processor = getProcessor(resolvedSource, resolvedPrefix);

        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
				
        try {
            processor.getEnvironmentHelper().changeContext(env);

            if (context.isBuildingPipelineOnly()) {
                // Propagate pipelines
                Processor.InternalPipelineDescription pp = processor.buildPipeline(env);
                if ( pp != null ) {
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

    private synchronized TreeProcessor getProcessor(String source, String prefix) throws Exception {

        TreeProcessor processor = (TreeProcessor)processors.get(source);

        if (processor == null) {
            // Handle directory mounts
            String actualSource;
            if (source.charAt(source.length() - 1) == '/') {
                actualSource = source + "sitemap.xmap";
            } else {
                actualSource = source;
            }
            
            processor = this.parentProcessor.createChildProcessor(actualSource, this.checkReload, prefix);

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
