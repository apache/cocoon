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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.Source;

import java.util.List;

/**
 * The TreeBuilder represents a particular language such as "sitemap".  It provides
 * all the necessary functionality to create new instances of a TreeProcessor.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: TreeBuilder.java,v 1.5 2003/10/30 13:02:31 cziegeler Exp $
 */

public interface TreeBuilder
{

    /**
     * Set the TreeProcessor for this TreeBuilder.
     * <p/>
     * ?? Is there a better way ??
     * ?? What exactly is this used for ??
     *
     * @param processor the TreeProcesor
     */
    void setProcessor( TreeProcessor processor );

    /**
     * Get the TreeProcessor from this TreeBuilder.
     * <p/>
     * ?? What exactly is this used for ??
     *
     * @return the TreeProcessor
     */
    TreeProcessor getProcessor();

    /**
     * Returns the language that is being built (e.g. "sitemap").
     *
     * @return the language name
     */
    String getLanguage();

    /**
     * Returns the name of the parameter element.  For example, do we refer to a parameter
     * with the name "parameter", "property", or "preference"?  It is used to assemble
     * {@link ParameterizableProcessingNode}s.
     *
     * @return parameter's name
     */
    String getParameterName();

    /**
     * Register a <code>ProcessingNode</code> under a given name.
     * For example, <code>ResourceNodeBuilder</code> stores here the <code>ProcessingNode</code>s
     * it produces for use by sitemap pipelines. This allows to turn the tree into a graph.
     *
     * @param name The name used to look up the node
     * @param node The node being registered
     */
    void registerNode( String name, ProcessingNode node );

    /**
     * Get a node we registered under a specific name earlier.
     *
     * @throws IllegalStateException ??if the name is not registered??
     */
    ProcessingNode getRegisteredNode( String name );

    /**
     * Create a ProcessingNodeBuilder for a particular configuration.
     * <p/>
     * ?? How does this fit into the overall picture ??
     * ?? Is this for a particular sitemap file ??
     *
     * @param config the configuration used to create the node builder
     * @return the generated node builder
     * @throws Exception if there was a problem generating the node builder
     */
    ProcessingNodeBuilder createNodeBuilder( Configuration config ) throws Exception;

    /**
     * Get the namespace URI that builders should use to find their nodes.
     *
     * @return the namespace URI
     */
    String getNamespace();

    /**
     * Build a processing tree from a <code>Configuration</code>.
     *
     * @param tree the Configuration to build the tree
     * @return the processing node
     * @throws Exception if there is a problem building the processing tree
     */
    ProcessingNode build( Configuration tree ) throws Exception;

    /**
     * Build a processing tree from a Source object.  This method essentially converts the
     * Source into a Configuration, and calls the {@link #build(Configuration)} method.
     *
     * @param source The Source object referencing the configuration file
     * @return the processing node
     * @throws Exception if there was a problem building the processing tree or reading the Source
     */
    ProcessingNode build( Source source ) throws Exception;

    /**
     * Get the default filename to look for to build processing trees.
     *
     * @return the default filename
     */
    String getFileName();

    /**
     * Return the list of <code>ProcessingNodes</code> part of this tree that are
     * <code>Disposable</code>. Care should be taken to properly dispose them before
     * trashing the processing tree.
     *
     * @return the list of {@link ProcessingNode}s that are disposable
     */
    List getDisposableNodes();

    /**
     * Setup a <code>ProcessingNode</code> by setting its location, calling all
     * the lifecycle interfaces it implements and giving it the parameter map if
     * it's a <code>ParameterizableNode</code>.
     * <p/>
     * As a convenience, the node is returned by this method to allow constructs
     * like <code>return treeBuilder.setupNode(new MyNode(), config)</code>.
     *
     * @param node   the ProcessingNode to setup
     * @param config the configuration element for the node
     * @return the active ProcessingNode
     * @throws Exception if there was a problem setting up the node
     */
    ProcessingNode setupNode( ProcessingNode node, Configuration config ) throws Exception;


    /**
     * Get the type for a statement : it returns the 'type' attribute if present,
     * and otherwhise the default hint for the <code>ComponentSelector</code> identified by
     * the role <code>role</code>.
     *
     * @param statement the statement
     * @param role      the role
     * @return the type name
     * @throws ConfigurationException if the default type could not be found.
     */
    String getTypeForStatement( Configuration statement, String role ) throws ConfigurationException;

    /**
     * Return the sitemap service manager.
     *
     * @return the service manager
     */
    ServiceManager getSitemapServiceManager();

    /**
     * Add an attribute. Useful to transmit information between distant (in the tree) node builders.
     *
     * @param name  the attribute name
     * @param value the attribute value
     */
    void setAttribute( String name, Object value );

    /**
     * Get the value of an attribute.
     *
     * @param name the attribute name
     * @return the attribute value
     */
    Object getAttribute( String name );
}
