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
 * @version CVS $Id: SelectNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
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
