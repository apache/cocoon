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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractProcessingNodeBuilder.java,v 1.2 2003/10/31 11:12:56 sylvain Exp $
 */


public abstract class AbstractProcessingNodeBuilder extends AbstractLogEnabled
  implements ProcessingNodeBuilder, Recomposable {

    protected TreeBuilder treeBuilder;
    
    protected ComponentManager manager;

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public void recompose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public void setBuilder(TreeBuilder treeBuilder) {
        this.treeBuilder = treeBuilder;
    }

    /**
     * Does this node accept parameters ? Default is true : if a builder that doesn't
     * have parameters doesn't override this method, erroneous parameters will be silently
     * ignored.
     */
    protected boolean hasParameters() {
        return true;
    }

    /**
     * Get &lt;xxx:parameter&gt; elements as a <code>Map</code> of </code>ListOfMapResolver</code>s,
     * that can be turned into parameters using <code>ListOfMapResolver.buildParameters()</code>.
     *
     * @return the Map of ListOfMapResolver, or <code>null</code> if there are no parameters.
     */
    protected Map getParameters(Configuration config) throws ConfigurationException {
        Configuration[] children = config.getChildren("parameter");

        if (children.length == 0) {
            // Parameters are only the component's location
            return Collections.singletonMap(Constants.SITEMAP_PARAMETERS_LOCATION, config.getLocation());
        }

        Map params = new HashMap(children.length+1);
        // Add the location information as a parameter
        params.put(Constants.SITEMAP_PARAMETERS_LOCATION, config.getLocation());
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            if (true) { // FIXME : check namespace
                String name = child.getAttribute("name");
                String value = child.getAttribute("value");
                try {
                    params.put(
                        VariableResolverFactory.getResolver(name, this.manager),
                        VariableResolverFactory.getResolver(value, this.manager));
                } catch(PatternException pe) {
                    String msg = "Invalid pattern '" + value + " at " + child.getLocation();
                    throw new ConfigurationException(msg, pe);
                }
            }
        }

        return params;
    }

    /**
     * Check if the namespace URI of the given configuraition is the same as the
     * one given by the builder.
     */
    protected void checkNamespace(Configuration config) throws ConfigurationException {
        if (!this.treeBuilder.getNamespace().equals(config.getNamespace()))
        {
            String msg = "Invalid namespace '" + config.getNamespace() + "' at " + config.getLocation();
            throw new ConfigurationException(msg);
        }
    }
}
