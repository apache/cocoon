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
package org.apache.cocoon.components.cprocessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: AbstractProcessingNode.java,v 1.4 2004/02/23 08:19:17 cziegeler Exp $
 */
public abstract class AbstractProcessingNode extends AbstractNode 
implements ProcessingNode, Configurable {

    private static final String PARAMETER_ELEMENT = "parameter";
    private static final String PARAMETER_NAME_ATTR = "name";
    private static final String PARAMETER_VALUE_ATTR = "value";
    
    protected Map m_parameters;
    
    public AbstractProcessingNode() {
    }
    
    public void configure(final Configuration config) throws ConfigurationException {
        super.configure(config);
        if (hasParameters()) {
            setParameters(config);
        }
    }
    
    /**
     * Parametrizable ProcessingNodes can overide this method to
     * have resolvable parameters set at configuration time.
     * 
     * @return  whether this processing node is parametrizable.
     */
    protected boolean hasParameters() {
        return false;
    }
    
    /**
     * Set &lt;xxx:parameter&gt; elements as a <code>Map</code> of </code>ListOfMapResolver</code>s,
     * that can be turned into parameters using <code>ListOfMapResolver.buildParameters()</code>.
     *
     * @return the Map of ListOfMapResolver, or <code>null</code> if there are no parameters.
     */
    private final void setParameters(Configuration config) throws ConfigurationException {
        final Configuration[] children = config.getChildren(PARAMETER_ELEMENT);
        if (children.length == 0) {
            return;
        }
        m_parameters = new HashMap();
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            String name = child.getAttribute(PARAMETER_NAME_ATTR);
            String value = child.getAttribute(PARAMETER_VALUE_ATTR);
            try {
                m_parameters.put(
                    VariableResolverFactory.getResolver(name, m_manager),
                    VariableResolverFactory.getResolver(value, m_manager));
            } catch(PatternException pe) {
                String msg = "Invalid pattern '" + value + "' at " + getConfigLocation(child);
                throw new ConfigurationException(msg, pe);
            }
        }
    }
    
    /**
     * Check if the namespace URI of the given configuraition is the same as the
     * one given by the builder.
     */
    protected final void checkNamespace(Configuration config) throws ConfigurationException {
        if (TreeProcessor.SITEMAP_NS.equals(config.getNamespace())) {
            String msg = "Invalid namespace '" + config.getNamespace() + "' at " + getConfigLocation(config);
            throw new ConfigurationException(msg);
        }
    }

}
