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
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.PermanentRedirector;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: RedirectToURINode.java,v 1.5 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class RedirectToURINode extends AbstractProcessingNode {

    // TODO: It can implement ParameterizableProcessingNode to pass redirect parameters
    //       Those parameters will be URL-encoded and appended to the redirect URI
    
    /** The 'uri' attribute */
    private VariableResolver uri;

    private boolean createSession;

    private boolean global;

    private boolean permanent;

    public RedirectToURINode(VariableResolver uri, boolean createSession, boolean global, boolean permanent )
    throws PatternException {
        this.global = global;
        this.uri = uri;
        this.createSession = createSession;
        this.permanent = permanent;
    }

    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {
        String resolvedURI = uri.resolve(context, env.getObjectModel());

        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + resolvedURI + "' at " + this.getLocation());
        }

        final Redirector redirector = PipelinesNode.getRedirector(env);

        if( this.global ) {
            redirector.globalRedirect(this.createSession, resolvedURI);
        } else if (this.permanent && redirector instanceof PermanentRedirector) {
            ((PermanentRedirector)redirector).permanentRedirect(this.createSession, resolvedURI);
        } else {
            redirector.redirect(this.createSession, resolvedURI);
        }

        return true;
    }
}
