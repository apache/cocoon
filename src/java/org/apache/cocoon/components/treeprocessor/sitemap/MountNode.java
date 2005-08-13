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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.commons.lang.BooleanUtils;

/**
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class MountNode extends AbstractProcessingNode
                       implements Disposable {

    /** The key to get the pass_through value from the Environment*/
    public final static String COCOON_PASS_THROUGH = "COCOON_PASS_THROUGH";


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

    /** The value of the 'pass-through' attribute */
    private final boolean passThrough;

    public MountNode(VariableResolver prefix,
                     VariableResolver source,
                     TreeProcessor parentProcessor,
                     boolean checkReload,
                     boolean passThrough) {
        this.prefix = prefix;
        this.source = source;
        this.parentProcessor = parentProcessor;
        this.checkReload = checkReload;
        this.passThrough = passThrough;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNode#invoke(org.apache.cocoon.environment.Environment, org.apache.cocoon.components.treeprocessor.InvokeContext)
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
        final Map objectModel = env.getObjectModel();

        String resolvedSource = this.source.resolve(context, objectModel);
        String resolvedPrefix = this.prefix.resolve(context, objectModel);

        if (resolvedSource.length() == 0) {
            throw new ProcessingException("Source of mount statement is empty");
        }

        resolvedSource = this.executor.enterSitemap(this, objectModel, resolvedSource);
        TreeProcessor processor = getProcessor(resolvedSource, resolvedPrefix);

        // Save context
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        Object oldPassThrough = env.getAttribute(COCOON_PASS_THROUGH);
        env.setAttribute(COCOON_PASS_THROUGH, BooleanUtils.toBooleanObject(passThrough));

        try {
            processor.getEnvironmentHelper().changeContext(env);

            if (context.isBuildingPipelineOnly()) {
                // Propagate pipelines
                Processor.InternalPipelineDescription pp = processor.buildPipeline(env);
                if (pp != null) {
                    context.setInternalPipelineDescription(pp);
                    return true;
                }
                return false;
            }
            // Processor will create its own pipelines
            return processor.process(env);
        } finally {
            // Restore context
            env.setURI(oldPrefix, oldURI);
            if (oldPassThrough != null) {
                env.setAttribute(COCOON_PASS_THROUGH, oldPassThrough);
            } else {
                env.removeAttribute(COCOON_PASS_THROUGH);
            }

            this.executor.leaveSitemap(this, objectModel);
            // Turning recomposing as a test, according to:
            // http://marc.theaimsgroup.com/?t=106802211400005&r=1&w=2
            // Recompose pipelines which may have been recomposed by subsitemap
            // context.recompose(this.manager);
        }
    }

    private synchronized TreeProcessor getProcessor(String source, String prefix)
    throws Exception {

        TreeProcessor processor = (TreeProcessor) processors.get(source);
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

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        Iterator i = this.processors.values().iterator();
        while (i.hasNext()) {
            ((TreeProcessor) i.next()).dispose();
        }
        this.processors.clear();
    }
}
