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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActionSetNodeBuilder.java,v 1.4 2003/08/07 17:13:39 joerg Exp $
 */

public class ActionSetNodeBuilder extends AbstractParentProcessingNodeBuilder implements ThreadSafe {
    
    /** The TreeBuilder attribute indicating that an ActionSet is being built */
    public static final String IN_ACTION_SET = ActionSetNodeBuilder.class.getName() + "/inActionSet";

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String actionSetName = config.getAttribute("name");

        Configuration[] childrenConfig = config.getChildren();
        // Inform other builders that we're in an action-set
        this.treeBuilder.setAttribute(IN_ACTION_SET, Boolean.TRUE);
        
        // Get the child actions
        ProcessingNode[] nodes = this.buildChildNodes(config);
        
        // And get their names
        String[] actions = new String[nodes.length];
        for (int i = 0; i < childrenConfig.length; i++) {
            Configuration childConfig = childrenConfig[i];
            String name = childConfig.getName();

            if ("act".equals(name)) {
                actions[i] = childConfig.getAttribute("action", null);
            } else {
                // Unknown element
                String msg = "Unknown element " + name + " in action-set at " + childConfig.getLocation();
                throw new ConfigurationException(msg);
            }            
        }

        ActionSetNode node = new ActionSetNode(actionSetName, nodes, actions);
        this.treeBuilder.setupNode(node, config);

        // Inform other builders that we're no more in an action-set
        this.treeBuilder.setAttribute(IN_ACTION_SET, null);

        return node;
    }
}
