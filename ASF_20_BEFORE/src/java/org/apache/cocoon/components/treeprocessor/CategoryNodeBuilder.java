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
package org.apache.cocoon.components.treeprocessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Builds a generic container node.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: CategoryNodeBuilder.java,v 1.2 2004/03/01 03:50:57 antonio Exp $
 */

public class CategoryNodeBuilder extends AbstractParentProcessingNodeBuilder
  implements Configurable, ThreadSafe {

    // Prefix used for registering as a TreeBuilder attribute
    private static String PREFIX = CategoryNodeBuilder.class.getName() + "/";

    protected String name;

    /**
     * The category name is the value of the "category-name" child, or if not
     * present, the name of the configuration element.
     */
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        this.name = config.getChild("category-name").getValue(config.getAttribute("name"));
    }

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {

        CategoryNode node = new CategoryNode();
        this.treeBuilder.setupNode(node, config);

        // Get all children and associate them to their name
        Map category = new HashMap();

        List children = buildChildNodesList(config);
        Iterator iter = children.iterator();
        while(iter.hasNext()) {
            NamedProcessingNode child = (NamedProcessingNode)iter.next();
            category.put(child.getName(), child);
        }

        node.setCategory(this.name, category);

        // Register node to allow lookup by other nodes
        this.treeBuilder.registerNode(PREFIX + this.name, node);

        return node;
    }

    public static CategoryNode getCategoryNode(TreeBuilder builder, String categoryName) {
        return (CategoryNode)builder.getRegisteredNode(PREFIX + categoryName);
    }

    public static ProcessingNode getNamedNode(TreeBuilder builder, String categoryName, String nodeName)
      throws Exception {
        CategoryNode category = getCategoryNode(builder, categoryName);

        return category.getNodeByName(nodeName);
    }
}
