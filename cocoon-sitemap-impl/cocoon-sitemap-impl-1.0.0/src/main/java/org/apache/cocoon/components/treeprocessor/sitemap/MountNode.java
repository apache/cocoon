/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.commons.lang.BooleanUtils;

/**
 * @version $Id$
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
    private final Boolean passThrough;


    public MountNode(VariableResolver prefix,
                     VariableResolver source,
                     TreeProcessor parentProcessor,
                     boolean checkReload,
                     boolean passThrough) {
        this.prefix = prefix;
        this.source = source;
        this.parentProcessor = parentProcessor;
        this.checkReload = checkReload;
        this.passThrough = BooleanUtils.toBooleanObject(passThrough);
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNode#invoke(Environment, InvokeContext)
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
        final Map objectModel = env.getObjectModel();

        String resolvedSource = this.source.resolve(context, objectModel);
        String resolvedPrefix = this.prefix.resolve(context, objectModel);

        if (resolvedSource.length() == 0) {
            throw new ProcessingException("Source of mount statement is empty");
        }

        // Handle directory mounts
        if (resolvedSource.charAt(resolvedSource.length() - 1) == '/') {
            resolvedSource = resolvedSource + "sitemap.xmap";
        }

        TreeProcessor processor = getProcessor(resolvedSource, resolvedPrefix);

        // Save context
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        Object oldPassThrough = env.getAttribute(COCOON_PASS_THROUGH);
        env.setAttribute(COCOON_PASS_THROUGH, this.passThrough);

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
        } catch(Exception e) {
            // Wrap with our location
            throw ProcessingException.throwLocated("Sitemap: error when calling sub-sitemap", e, getLocation());

        } finally {
            // Restore context
            env.setURI(oldPrefix, oldURI);
            if (oldPassThrough != null) {
                env.setAttribute(COCOON_PASS_THROUGH, oldPassThrough);
            } else {
                env.removeAttribute(COCOON_PASS_THROUGH);
            }
        }
    }

    private synchronized TreeProcessor getProcessor(String source, String prefix)
    throws Exception {

        TreeProcessor processor = (TreeProcessor) processors.get(source);
        if (processor == null) {
            processor = this.parentProcessor.createChildProcessor(source, this.checkReload, prefix);

            // Associate to the original source
            processors.put(source, processor);
        }

        return processor;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        Iterator i = this.processors.values().iterator();
        while (i.hasNext()) {
            ContainerUtil.dispose(i.next());
        }
        this.processors.clear();
    }
}
