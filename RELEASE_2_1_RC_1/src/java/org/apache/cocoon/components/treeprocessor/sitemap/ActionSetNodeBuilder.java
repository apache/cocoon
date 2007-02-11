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

import org.apache.cocoon.acting.Action;

import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

import java.util.*;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActionSetNodeBuilder.java,v 1.1 2003/03/09 00:09:20 pier Exp $
 */

public class ActionSetNodeBuilder extends AbstractProcessingNodeBuilder implements ThreadSafe {

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String actionSetName = config.getAttribute("name");

        // Lists of action types, names and sources for each map:act
        List actionTypes  = new ArrayList();
        List actionNames  = new ArrayList();
        List actionSources = new ArrayList();
        List actionParameters = new ArrayList();

        Configuration[] childrenConfig = config.getChildren();
        for (int i = 0; i < childrenConfig.length; i++) {

            Configuration childConfig = childrenConfig[i];
            String name = childConfig.getName();

            if ("act".equals(name)) {

                checkNamespace(childConfig);
                String type = this.treeBuilder.getTypeForStatement(childConfig, Action.ROLE + "Selector");

                actionTypes.add(type);
                actionNames.add(childConfig.getAttribute("action", null));
                actionSources.add(VariableResolverFactory.getResolver(
                    childConfig.getAttribute("src", null), this.manager));
                actionParameters.add(this.getParameters(childConfig));

            } else {
                // Unknown element
                String msg = "Unknown element " + name + " in action-set at " + childConfig.getLocation();
                throw new ConfigurationException(msg);
            }
        }

        String[] types   = (String[])actionTypes.toArray(new String[actionTypes.size()]);
        String[] actions = (String[])actionNames.toArray(new String[actionNames.size()]);
        Map[]    parameters = (Map[])actionParameters.toArray(new Map[actionParameters.size()]);
        VariableResolver[] sources =
            (VariableResolver[])actionSources.toArray(new VariableResolver[actionSources.size()]);

        ActionSetNode node = new ActionSetNode(actionSetName, types, actions, sources, parameters);
        this.treeBuilder.setupNode(node, config);

        return node;
    }
}
