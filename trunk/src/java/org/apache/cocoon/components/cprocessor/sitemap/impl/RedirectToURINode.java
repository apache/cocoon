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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.PermanentRedirector;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: RedirectToURINode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=redirect-node
 */
public class RedirectToURINode extends AbstractProcessingNode implements ProcessingNode {

    // The 'uri' attribute
    private VariableResolver m_uri;
    private boolean m_createSession;
    private boolean m_global;
    private boolean m_permanent;

    public RedirectToURINode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        try {
            m_uri = VariableResolverFactory.getResolver(config.getAttribute("uri"), m_manager);
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
        m_createSession = config.getAttributeAsBoolean("session", false);
        m_global = config.getAttributeAsBoolean("global", false);
        m_permanent = config.getAttributeAsBoolean("permanent", false);
    }

    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
        String resolvedURI = m_uri.resolve(context, env.getObjectModel());

        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + resolvedURI + "' at " + getLocation());
        }

        final Redirector redirector = context.getRedirector();

        if (m_global) {
            redirector.globalRedirect(m_createSession, resolvedURI);
        } 
        else if (m_permanent && redirector instanceof PermanentRedirector) {
            ((PermanentRedirector) redirector).permanentRedirect(m_createSession,resolvedURI);
        } 
        else {
            redirector.redirect(m_createSession,resolvedURI);
        }

        return true;
    }

}
