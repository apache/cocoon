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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.cprocessor.AbstractParentProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;
import org.apache.cocoon.sitemap.PatternException;

/**
 * 
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SelectNode.java,v 1.3 2004/01/31 16:57:52 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=select-node
 */
public class SelectNode extends AbstractParentProcessingNode implements Initializable {
    
    private String m_type;
    private VariableResolver[] m_tests;
    private ProcessingNode[][] m_whenNodes;
    private ProcessingNode[] m_otherwiseNodes;
    
    private Selector m_selector;
    private SwitchSelector m_switchSelector;

    public SelectNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_type = config.getAttribute("type", null);
        
        List nodes = new ArrayList();
        List tests = new ArrayList();
        
        // <when> clauses
        Configuration[] children = config.getChildren("when");
        for (int i = 0; i < children.length; i++) {
            String test = children[i].getAttribute("test");
            try {
                tests.add(VariableResolverFactory.getResolver(test, super.m_manager));
                nodes.add(super.getChildNodes(children[i]));
            }
            // TODO: better error reporting
            catch (PatternException e) {
                throw new ConfigurationException(e.toString());
            }
        }
        m_tests = (VariableResolver[]) tests.toArray(new VariableResolver[tests.size()]);
        m_whenNodes = (ProcessingNode[][]) nodes.toArray(new ProcessingNode[tests.size()][0]);
        
        // <otherwise> clause
        m_otherwiseNodes = getChildNodes(config.getChild("otherwise"));
    }
    
    public void initialize() throws Exception {
        String key = Selector.ROLE;
        if (m_type != null) {
            key += "/" + m_type;
        }
        // TODO: better error message
        m_selector = (Selector) super.m_manager.lookup(key);
        if (m_selector instanceof SwitchSelector) {
            m_switchSelector = (SwitchSelector) m_selector;
        }
    }
    
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
        
        Map om = env.getObjectModel();
        Parameters parameters = VariableResolver.buildParameters(m_parameters, context, om);
        
        if (m_switchSelector != null) {
            Object ctx = m_switchSelector.getSelectorContext(om, parameters);
            for (int i = 0; i < m_tests.length; i++) {
                if (m_switchSelector.select(m_tests[i].resolve(context, om), ctx)) {
                    return invokeNodes(m_whenNodes[i], env, context);
                }
            }
        }
        else {
            for (int i = 0; i < m_tests.length; i++) {
                String test = m_tests[i].resolve(context, om);
                if (m_selector.select(test, om, parameters)) {
                    return invokeNodes(m_whenNodes[i], env, context);
                }
            }
        }

        if (m_otherwiseNodes != null) {
            return invokeNodes(m_otherwiseNodes, env, context);
        }

        return false;
        
    }
    
    /**
     * @return <code>true</code>
     */
    protected boolean hasParameters() {
        return true;
    }

}
