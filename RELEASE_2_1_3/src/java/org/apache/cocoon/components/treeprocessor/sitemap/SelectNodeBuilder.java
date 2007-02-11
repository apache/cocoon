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
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SelectNodeBuilder.java,v 1.1 2003/03/09 00:09:22 pier Exp $
 */

public class SelectNodeBuilder extends AbstractParentProcessingNodeBuilder {

    private static final String SELECTOR_ROLE = Selector.ROLE + "Selector";

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String type = this.treeBuilder.getTypeForStatement(config, SELECTOR_ROLE);

        // Lists of ProcessingNode[] and test resolvers for each "when"
        List whenChildren = new ArrayList();
        List whenTests = new ArrayList();

        // Nodes for otherwise (if any)
        ProcessingNode[] otherwiseNodes = null;

        Configuration[] childrenConfig = config.getChildren();
        for (int i = 0; i < childrenConfig.length; i++) {

            Configuration childConfig = childrenConfig[i];
            String name = childConfig.getName();

            if ("when".equals(name)) {

                checkNamespace(childConfig);
                whenTests.add(
                    VariableResolverFactory.getResolver(childConfig.getAttribute("test"), this.manager)
                );
                whenChildren.add(buildChildNodes(childConfig));

            } else if ("otherwise".equals(name)) {

                checkNamespace(childConfig);
                if (otherwiseNodes != null) {
                    String msg = "Duplicate " + name + " (only one is allowed) at " + childConfig.getLocation();
                    getLogger().error(msg);
                    throw new ConfigurationException(msg);
                }

                otherwiseNodes = buildChildNodes(childConfig);

            } else if (isParameter(childConfig)) {
                // ignore it. It is handled automatically in setupNode()

            } else {
                // Unknown element
                String msg = "Unknown element '" + name + "' in select at " + childConfig.getLocation();
                throw new ConfigurationException(msg);
            }
        }

        ProcessingNode[][] whenChildrenNodes = (ProcessingNode[][])whenChildren.toArray(new ProcessingNode[0][0]);
        VariableResolver[] whenResolvers = (VariableResolver[])whenTests.toArray(new VariableResolver[whenTests.size()]);

        // Get the type and class for this selector
        ComponentsSelector compSelector = (ComponentsSelector)this.manager.lookup(SELECTOR_ROLE);

        Class clazz = null;
        try {
            // Find selector class
            Selector selector = (Selector)compSelector.select(type);
            try {
                clazz = selector.getClass();
            } finally {
                compSelector.release(selector);
            }
        } finally {
            this.manager.release(compSelector);
        }

        if (SwitchSelector.class.isAssignableFrom(clazz)) {
            SwitchSelectNode node = new SwitchSelectNode(type);
            this.treeBuilder.setupNode(node, config);
            node.setCases(whenChildrenNodes, whenResolvers, otherwiseNodes);
            return node;
        } else {
            SelectNode node = new SelectNode(type);
            this.treeBuilder.setupNode(node, config);
            node.setCases(whenChildrenNodes, whenResolvers, otherwiseNodes);
            return node;
        }
    }
}
