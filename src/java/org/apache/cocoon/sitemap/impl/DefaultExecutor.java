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
package org.apache.cocoon.sitemap.impl;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.ExecutionContext;
import org.apache.cocoon.sitemap.SitemapExecutor;

/**
 * This is the default executor that does nothing but just executing the
 * statements.
 * TODO - This is not finished yet!
 * 
 * @since 2.2
 * @version CVS $Id: DefaultExecutor.java,v 1.2 2004/06/09 13:43:04 cziegeler Exp $
 */
public class DefaultExecutor 
    implements SitemapExecutor {
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeAction(org.apache.cocoon.acting.Action, org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeAction(final ExecutionContext context,
                            final Action           action, 
                            final Redirector       redirector, 
                            final SourceResolver   resolver, 
                            final Map              objectModel, 
                            final String           resolvedSource, 
                            final Parameters       resolvedParams )
    throws Exception {
        return action.act(redirector, resolver, objectModel, 
                resolvedSource, resolvedParams);        
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#popVariables()
     */
    public void popVariables(ExecutionContext context) {
        // nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#pushVariables(java.lang.String, java.util.Map)
     */
    public Map pushVariables(ExecutionContext context, String key, Map variables) {
        return variables;
    }
}
