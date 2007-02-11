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
 * @version CVS $Id: MountNode.java,v 1.10 2004/02/05 07:55:23 cziegeler Exp $
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
