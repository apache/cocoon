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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base class for parent <code>ProcessingNodeBuilders</code>, providing services for parsing
 * children nodes.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractParentProcessingNodeBuilder.java,v 1.1 2003/03/09 00:09:15 pier Exp $
 */

public abstract class AbstractParentProcessingNodeBuilder extends AbstractProcessingNodeBuilder implements Configurable {

    protected Collection allowedChildren;

    protected Collection forbiddenChildren;

    protected Collection ignoredChildren;

    /**
     * Configure the sets of allowed, forbidden and ignored children nodes.
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.allowedChildren   = getStringCollection(config.getChild("allowed-children"));
        this.forbiddenChildren = getStringCollection(config.getChild("forbidden-children"));
        this.ignoredChildren   = getStringCollection(config.getChild("ignored-children"));
    }

    /**
     * Checks if a child element and is allowed, and if not throws a <code>ConfigurationException</code>.
     *
     * @param child the child configuration to check.
     * @return <code>true</code> if this child should be considered or <code>false</code>
     *         if it should be ignored.
     * @throws ConfigurationException if this child isn't allowed.
     */
    protected boolean isChild(Configuration child) throws ConfigurationException {

        checkNamespace(child);

        String name = child.getName();

        // Is this a parameter of a parameterizable node builder ?
        if (isParameter(child)) {
            return false;
        }

        // Is this element to be ignored ?
        if (ignoredChildren != null && ignoredChildren.contains(name)) {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("Element '" + name + "' is ignored for building children of element '" +
                    child.getName() + "'");
            }

            return false;
        }

        // Is it allowed ?
        if ( (allowedChildren != null && !allowedChildren.contains(name)) ||
             (forbiddenChildren != null && forbiddenChildren.contains(name)) ) {
            String msg = "Element '" + name + "' is not allowed at " + child.getLocation();
            throw new ConfigurationException(msg);
        }

        return true;
    }

    protected boolean isParameter(Configuration config) throws ConfigurationException {
        String name = config.getName();
        if (name.equals(this.treeBuilder.getParameterName())) {
            if (this.hasParameters()) {
                return true;
            } else {
                String msg = "Element '" + name + "' has no parameters at " + config.getLocation();
                throw new ConfigurationException(msg);
            }
        }
        return false;
    }

    /**
     * Create the <code>ProcessingNode</code>s for the children of a given node.
     * Child nodes are controlled to be actually allowed in this node.
     */
    protected List buildChildNodesList(Configuration config) throws Exception {

        Configuration[] children = config.getChildren();
        List result = new ArrayList();

        for (int i = 0; i < children.length; i++) {

            Configuration child = children[i];
            try {
                 if (isChild(child)) {
                    // OK : get a builder.
                    ProcessingNodeBuilder childBuilder = this.treeBuilder.createNodeBuilder(child);
                    result.add(childBuilder.buildNode(child));
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

    protected ProcessingNode[] buildChildNodes(Configuration config) throws Exception {
        return toNodeArray(buildChildNodesList(config));
    }

    /**
     * Convenience function that converts a <code>List</code> of <code>ProcessingNode</code>s
     * to an array.
     */
    public static ProcessingNode[] toNodeArray(List list) {
        return (ProcessingNode[])list.toArray(new ProcessingNode[list.size()]);
    }

    /**
     * Splits the value of a Configuration in a Collection of Strings. Splitting
     * occurs at space characters (incl. line breaks) and comma.
     *
     * @return a collection of Strings, or null if <code>config</code> has no value.
     */
    private Collection getStringCollection(Configuration config) {
        String s = config.getValue(null);

        return (s == null) ? null : Arrays.asList(StringUtils.split(s, ", \t\n\r"));
    }
}
