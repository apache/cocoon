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
package org.apache.cocoon.components.cprocessor;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SimpleParentProcessingNode.java,v 1.3 2004/03/08 13:57:39 cziegeler Exp $
 */
public abstract class SimpleParentProcessingNode extends AbstractParentProcessingNode {    
    
    private ProcessingNode[] m_childNodes;
    
    public SimpleParentProcessingNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_childNodes = super.getChildNodes(config);
    }
        
    /**
     * Define common invoke behavior here
     */
    public boolean invoke(Environment env, InvokeContext context) throws Exception {

        // inform the pipeline (if available) that we have come across
        // a possible branch point
        if (context.pipelineIsSet() && this.hasChildren() ) {
            context.getProcessingPipeline().informBranchPoint();
        }

        // processing not yet complete, so return false
        return false;
    }
    
    protected final ProcessingNode[] getChildNodes() {
        return m_childNodes;
    }
    
    /**
     * Boolean method with returns true if this Node has children 
     * and false otherwise
     *
     * @return boolean 
     */
    protected final boolean hasChildren() {
        if (getChildNodes() != null || getChildNodes().length > 0) {
            return true;
        }
        return false;
    }

}
