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
 * @version CVS $Id: AbstractParentProcessingNodeBuilder.java,v 1.3 2004/03/05 13:02:51 bdelacretaz Exp $
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
            if (getLogger().isDebugEnabled()) {
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
