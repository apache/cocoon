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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.cprocessor.AbstractNode;
import org.apache.cocoon.components.cprocessor.sitemap.FlowNode;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.Interpreter;

/**
 * Handler for &lt;map:flow&gt; element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @since September 13, 2002
 * @version CVS $Id: FlowNodeImpl.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=FlowNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=flow-node
 */
public class FlowNodeImpl extends AbstractNode implements FlowNode {

    private String m_language;
    private Interpreter m_interpreter;
    
    public FlowNodeImpl() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_language = config.getAttribute("language", "javascript");

        try {
            m_interpreter = (Interpreter) lookup(Interpreter.ROLE + "/" + m_language);
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
    
    public final Interpreter getInterpreter() {
        return m_interpreter;
    }

}
