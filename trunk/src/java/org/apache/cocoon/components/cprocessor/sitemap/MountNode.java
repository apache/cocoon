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
package org.apache.cocoon.components.cprocessor.sitemap;

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
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.TreeProcessor;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: MountNode.java,v 1.4 2004/01/05 10:53:35 cziegeler Exp $
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
    
    public void contextualize(Context context) throws ContextException {
        m_parentProcessor = (TreeProcessor) context.get("treeprocessor");
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        Map objectModel = env.getObjectModel();

        String resolvedSource = m_source.resolve(context, objectModel);
        String resolvedPrefix = m_prefix.resolve(context, objectModel);

        TreeProcessor processor = this.getProcessor(resolvedSource, resolvedPrefix);

        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        try {
            processor.getEnvironmentHelper().changeContext(env);

            if (context.isBuildingPipelineOnly()) {
                // Propagate pipelines
                ProcessingPipeline pp = processor.buildPipeline(env);
                if (pp != null) {
                    context.setProcessingPipeline(pp);
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
            processor.getEnvironmentHelper().resetContext(env);

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
