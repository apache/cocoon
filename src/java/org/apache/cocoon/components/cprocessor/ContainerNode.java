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
 * A generic container node that just invokes its children.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ContainerNode.java,v 1.4 2004/03/08 13:57:39 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=container-node
 */
public class ContainerNode extends SimpleParentProcessingNode {

    public ContainerNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        if (!hasChildren()) {
            String msg = "There must be at least one child at " + getLocation();
            throw new ConfigurationException(msg);
        }
    }
    
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
        return invokeNodes(getChildNodes(), env, context);
    }

}
