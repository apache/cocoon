/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @version $Id$
 */
public interface TreeBuilder {

    String ROLE = TreeBuilder.class.getName();

    WebApplicationContext getWebApplicationContext();

    ConcreteTreeProcessor getProcessor();

    void setProcessor(ConcreteTreeProcessor processor);

    /**
     * Register a <code>ProcessingNode</code> under a given name.
     * For example, <code>ResourceNodeBuilder</code> stores here the <code>ProcessingNode</code>s
     * it produces for use by sitemap pipelines. This allows to turn the tree into a graph.
     * If a node with the name is already registed, the process fails!
     * @return If the node could be registered, <code>true</code> is returned; otherwise false.
     */
    boolean registerNode(String name, ProcessingNode node);

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
     * Build a processing tree from a <code>Configuration</code> object holding the sitemap program.
     */
    ProcessingNode build(Configuration config, String location) throws Exception;

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
     * and otherwhise the default hint for the <code>ServiceSelector</code> identified by
     * the role <code>role</code>.
     *
     * @param statement the statement
     * @param role the component's role (warn: not the selector's role)
     *
     * @throws ConfigurationException if the default type could not be found.
     */
    String getTypeForStatement(Configuration statement, String role) throws ConfigurationException;

    /**
     * Add an attribute. Useful to transmit information between distant (in the tree) node builders
     */
    void setAttribute(String name, Object value);

    /**
     * Get the value of an attribute.
     */
    Object getAttribute(String name);

    /**
     * Return all event listers that are registered for the
     * {@link org.apache.cocoon.sitemap.EnterSitemapEvent}.
     * @return A list of components.
     */
    List getEnterSitemapEventListeners();

    /**
     * Return all event listers that are registered for the
     * {@link org.apache.cocoon.sitemap.LeaveSitemapEvent}.
     * @return A list of components.
     */
    List getLeaveSitemapEventListeners();
}
