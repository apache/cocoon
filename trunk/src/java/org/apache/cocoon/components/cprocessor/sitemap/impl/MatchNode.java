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
package org.apache.cocoon.components.cprocessor.sitemap.impl;

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
 * @version CVS $Id: MatchNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
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
