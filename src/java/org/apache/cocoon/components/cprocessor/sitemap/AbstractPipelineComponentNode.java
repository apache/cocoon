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
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;

/**
 *
 * @author <a href="mailto:Michael.Melhem@dresdner-bank.com">Michael Melhem</a>
 * @version CVS $Id: AbstractPipelineComponentNode.java,v 1.3 2004/03/08 13:57:37 cziegeler Exp $
 */
public abstract class AbstractPipelineComponentNode extends AbstractProcessingNode
implements Initializable {

    private String m_type;
    protected ComponentNode m_component;
    
    // TODO: implement pipeline hints
    protected Map m_pipelineHints;
    
    public AbstractPipelineComponentNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_type = config.getAttribute("type",null);
    }
    
    public void initialize() throws Exception {
        String key = getComponentNodeRole();
        if (m_type != null) {
            key += "/" + m_type;
        }
        // TODO: meaningful error message
        m_component = (ComponentNode) lookup(key);
    }
    
    protected abstract String getComponentNodeRole();
}
