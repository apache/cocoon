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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractParentProcessingNode.java,v 1.2 2003/11/16 18:25:31 unico Exp $
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
     * Subclasses should overide this method if they want to allow certain children.
     * 
     * @return  a Collection of node names that are allowed as children of this node.
     */
    protected Collection getAllowedChildren() {
        return null;
    }
    
    /**
     * Subclasses should overide this method if they want to forbid certain children.
     * 
     * @return  a Collection of node names that are forbidden as children of this node.
     */
    protected Collection getForbiddenChildren() {
        return null;
    }
    
    protected final ProcessingNode[] getChildNodes(Configuration config) throws ConfigurationException {
        return toNodeArray(getChildNodesList(config));
    }
    
    private final ProcessingNode[] toNodeArray(List list) {
        return (ProcessingNode[])list.toArray(new ProcessingNode[list.size()]);
    }

    /**
     * Create the <code>ProcessingNode</code>s for the children of a given node.
     * Child nodes are controlled to be actually allowed in this node.
     */
    private final List getChildNodesList(Configuration config) throws ConfigurationException {

        Configuration[] children = config.getChildren();
        List result = new ArrayList();
        
        for (int i = 0; i < children.length; i++) {
            
            Configuration child = children[i];
            try {
                 // TODO: is this check still neccesary?
                 if (isChild(child)) {
                    // OK : look it up from service manager
                    String id = child.getAttribute("id-ref");
                    result.add(m_manager.lookup(ProcessingNode.ROLE + "/" + id));
                 }
            } catch(ConfigurationException ce) {
                throw ce;
            } catch(Exception e) {
                String msg = "Error while creating node '" + child.getName() + "' at " + child.getLocation();
                throw new ConfigurationException(msg, e);
            }
        }

        return result;
    }
    
    /**
     * Checks if a child element and is allowed, and if not throws a <code>ConfigurationException</code>.
     *
     * @param child the child configuration to check.
     * @return <code>true</code> if this child should be considered or <code>false</code>
     *         if it should be ignored.
     * @throws ConfigurationException if this child isn't allowed.
     */
    private final boolean isChild(Configuration child) throws ConfigurationException {

        checkNamespace(child);

        String name = child.getName();

        // Is this a parameter of a parameterizable node builder ?
        if (isParameter(child)) {
            return false;
        }

        // Is this element to be ignored ?
        // TODO: do we need this? It's not used in treeprocessor-builtins.xml
//        Collection ignoredChildren = getIgnoredChildren();
//        if (ignoredChildren != null && ignoredChildren.contains(name)) {
//            if (getLogger().isDebugEnabled()) {
//                getLogger().debug("Element '" + name + "' is ignored for building children of element '" +
//                                  child.getName() + "'");
//            }
//
//            return false;
//        }

        // Is it allowed ?
        Collection allowedChildren = getAllowedChildren();
        Collection forbiddenChildren = getForbiddenChildren();
        if ( (allowedChildren != null && !allowedChildren.contains(name)) ||
             (forbiddenChildren != null && forbiddenChildren.contains(name)) ) {
            String msg = "Element '" + name + "' is not allowed at " + getConfigLocation(child);
            throw new ConfigurationException(msg);
        }
        return true;
    }
    
    /**
     * Check if the current config element is a parameter.
     * 
     * @throws ConfigurationException  if this config element is a parameter
     * and this node does not allow parameters.
     */
    private final boolean isParameter(Configuration config) throws ConfigurationException {
        String name = config.getName();
        if (name.equals(PARAMETER_ELEMENT)) {
            if (this.hasParameters()) {
                return true;
            } else {
                String msg = "Element '" + name + "' has no parameters at " + getConfigLocation(config);
                throw new ConfigurationException(msg);
            }
        }
        return false;
    }

}
