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

*/
package org.apache.cocoon.components.cprocessor.sitemap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;

/**
 * Handler for &lt;map:flow&gt; element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @since September 13, 2002
 * @version CVS $Id: FlowNode.java,v 1.1 2003/12/28 21:03:17 unico Exp $
 */
public class FlowNode extends AbstractProcessingNode implements Contextualizable {

    private String m_language;
    private Context m_context;
    private Interpreter m_interpreter;
    
    public FlowNode() {
    }
    
    public void contextualize(org.apache.avalon.framework.context.Context context) 
    throws ContextException {
        m_context = (Context) context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_language = config.getAttribute("language", "JavaScript");

        try {
            m_interpreter = (Interpreter) super.m_manager.lookup(Interpreter.ROLE + "/" + m_language);
        }
        catch (ServiceException e) {
            String msg = "Couldn't obtain a flow interpreter for " + m_language + ": " + e;
            throw new ConfigurationException(msg);
        }
        
        Configuration[] children = config.getChildren("script");
        for (int i = 0; i < children.length; i++) {
            String src = children[i].getAttribute("src");
            if (m_interpreter instanceof AbstractInterpreter) {
                ((AbstractInterpreter) m_interpreter).register(src);
            }
        }
    }
    
    /**
     * This method should never be called by the TreeProcessor, since a
     * <code>&lt;map:flow&gt;</code> element should not be in an
     * "executable" sitemap node.
     *
     * @param env an <code>Environment</code> value
     * @param context an <code>InvokeContext</code> value
     * @return a <code>boolean</code> value
     * @exception Exception if an error occurs
     */
    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        return true;
    }
    
    public Interpreter getInterpreter() {
        return m_interpreter;
    }

}
