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
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.sitemap.PatternException;

/**
 * 
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: MatchNode.java,v 1.1 2003/12/28 21:03:17 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=match-node
 */
public class MatchNode extends SimpleParentProcessingNode implements ProcessingNode, Initializable {

    /** The 'type' attribute */
    private String m_type;
    
    /** The 'pattern' attribute */
    private String m_pattern;
    
    /** The pattern resolver in case of a non-PreparableMatcher */
    private VariableResolver m_patternResolver;
    
    /** The prepared pattern in case of a PreparableMatcher */
    private Object m_preparedPattern;
    
    /** The 'name' for the variable anchor */
    private String m_name;
    
    /** The Matcher component identified by the 'type' attribute */
    private Matcher m_matcher;
    
    /** The PreparableMatcher component identified by the 'type' attribute */
    private PreparableMatcher m_preparableMatcher;

    public MatchNode() {
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        try {
            m_pattern = config.getAttribute("pattern", null);
            if (m_pattern != null && VariableResolverFactory.needsResolve(m_pattern)) {
                m_patternResolver = VariableResolverFactory.getResolver(
                    config.getAttribute("pattern", null),super.m_manager);
            }
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
        m_name = config.getAttribute("name", null);
        m_type = config.getAttribute("type", null);
    }

    public void initialize() throws Exception {
        String key = Matcher.ROLE;
        if (m_type != null) {
            key += "/" + m_type;
        }
        // TODO: better error message
        m_matcher = (Matcher) super.m_manager.lookup(key);
        
        // prepare the matcher in case of PreparableMatcher
        if (m_matcher instanceof PreparableMatcher) {
            m_preparableMatcher = (PreparableMatcher) m_matcher;
            try {
                m_preparedPattern = m_preparableMatcher.preparePattern(m_pattern);
            } catch(PatternException pe) {
                String msg = "Invalid pattern '" + m_pattern + "' for matcher at " + getLocation();
                throw new ConfigurationException(msg);
            }
        }
    }

    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
	
        // Perform any common invoke functionality
        super.invoke(env, context);
        
        Map om = env.getObjectModel();
        Parameters parameters = VariableResolver.buildParameters(
            super.m_parameters, context, om);
        
        Map result;
        if (m_preparableMatcher != null) {
            // No pattern resolving in case of a prepared matcher
            result = m_preparableMatcher.preparedMatch(m_preparedPattern,om,parameters);
        }
        else {
            String pattern;
            if (m_patternResolver != null) {
                pattern = m_patternResolver.resolve(context,om);
            }
            else {
                pattern = m_pattern;
            }
            result = m_matcher.match(pattern,om,parameters);
        }
        
        if (result != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Matcher '" + m_type + "' matched pattern '" + m_pattern +
                    "' at " + getLocation());
            }
            // Invoke children with the matcher results
            return invokeNodes(getChildNodes(), env, context, m_name, result);
        } else {
            // Matcher failed
            return false;
        }
    }

    /**
     * @return <code>true</code>
     */
    protected boolean hasParameters() {
        return true;
    }

}
