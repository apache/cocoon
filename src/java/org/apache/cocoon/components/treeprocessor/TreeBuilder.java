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

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.excalibur.source.Source;

import java.util.List;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: TreeBuilder.java,v 1.4 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public interface TreeBuilder extends Component {

    void setProcessor(TreeProcessor processor);

    TreeProcessor getProcessor();

    /**
     * Returns the language that is being built (e.g. "sitemap").
     */
    String getLanguage();

    /**
     * Returns the name of the parameter element.
     */
    String getParameterName();

    /**
     * Register a <code>ProcessingNode</code> under a given name.
     * For example, <code>ResourceNodeBuilder</code> stores here the <code>ProcessingNode</code>s
     * it produces for use by sitemap pipelines. This allows to turn the tree into a graph.
     */
    void registerNode(String name, ProcessingNode node);

    /**
     * @throws IllegalStateException
     */
    ProcessingNode getRegisteredNode(String name);

    ProcessingNodeBuilder createNodeBuilder(Configuration config) throws Exception;

    /**
     * Get the namespace URI that builders should use to find their nodes.
     */
    String getNamespace();

    /**
     * Build a processing tree from a <code>Configuration</code>.
     */
    ProcessingNode build(Configuration tree) throws Exception;

    ProcessingNode build(Source source) throws Exception;

    String getFileName();

    /**
     * Return the list of <code>ProcessingNodes</code> part of this tree that are
     * <code>Disposable</code>. Care should be taken to properly dispose them before
     * trashing the processing tree.
     */
    List getDisposableNodes();

    /**
     * Setup a <code>ProcessingNode</code> by setting its location, calling all
     * the lifecycle interfaces it implements and giving it the parameter map if
     * it's a <code>ParameterizableNode</code>.
     * <p>
     * As a convenience, the node is returned by this method to allow constructs
     * like <code>return treeBuilder.setupNode(new MyNode(), config)</code>.
     */
    ProcessingNode setupNode(ProcessingNode node, Configuration config) throws Exception;


    /**
     * Get the type for a statement : it returns the 'type' attribute if present,
     * and otherwhise the default hint for the <code>ComponentSelector</code> identified by
     * the role <code>role</code>.
     *
     * @throws ConfigurationException if the default type could not be found.
     */
    String getTypeForStatement(Configuration statement, String role) throws ConfigurationException;

    /**
     * Return the sitemap component manager
     */
    ComponentManager getSitemapComponentManager();
    
    /**
     * Add an attribute. Useful to transmit information between distant (in the tree) node builders
     */
    void setAttribute(String name, Object value);
    
    /**
     * Get the value of an attribute.
     */
    Object getAttribute(String name);
}
