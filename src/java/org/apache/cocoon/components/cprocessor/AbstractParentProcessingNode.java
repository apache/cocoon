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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: AbstractParentProcessingNode.java,v 1.4 2004/03/08 13:57:39 cziegeler Exp $
 */
public abstract class AbstractParentProcessingNode extends AbstractProcessingNode {
    
    public AbstractParentProcessingNode() {
    }
    
    /**
     * Invoke all nodes of a node array in order, until one succeeds.
     *
     * @param currentMap the <code>Map<code> of parameters produced by this node,
     *            which is added to <code>listOfMap</code>.
     */
    protected final boolean invokeNodes(
        ProcessingNode[] nodes,
        Environment env,
        InvokeContext context,
        String currentName,
        Map currentMap)
      throws Exception {

        context.pushMap(currentName,currentMap);

        try {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].invoke(env, context)) {
                    // Success
                    return true;
                }
            }
        }
        finally {
            // No success
            context.popMap();
        }

        return false;
    }

    /**
     * Invoke all nodes of a node array in order, until one succeeds.
     */
    protected final boolean invokeNodes (
        ProcessingNode[] nodes,
        Environment env,
        InvokeContext context)
      throws Exception {

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].invoke(env, context)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Create the <code>ProcessingNode</code>s for the children of a given node.
     */
    protected final ProcessingNode[] getChildNodes(Configuration config) throws ConfigurationException {
        List children = getChildNodesList(config);
        return (ProcessingNode[]) children.toArray(new ProcessingNode[children.size()]);
    }
    
    private final List getChildNodesList(Configuration config) throws ConfigurationException {

        Configuration[] children = config.getChildren();
        List result = new ArrayList(children.length);
        
        for (int i = 0; i < children.length; i++) {
            try {
                // look it up from service manager
                String id = children[i].getAttribute("id-ref",null);
                if (id != null) {
                    result.add(m_manager.lookup(ProcessingNode.ROLE + "/" + id));
                }
            } catch(ServiceException e) {
                String msg = "Error while creating node '" + children[i].getName() 
                    + "' at " + getConfigLocation(children[i]);
                throw new ConfigurationException(msg, e);
            }
        }

        return result;
    }

}
