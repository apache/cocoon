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
package org.apache.cocoon.components.profiler;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.sitemap.ExecutionContext;

/**
 * Sampe sitemap executor that prints out everything to a logger
 * 
 * @since 2.2
 * @version CVS $Id: SimpleSitemapExecutor.java,v 1.1 2004/06/09 13:43:04 cziegeler Exp $
 */
public class SimpleSitemapExecutor 
    extends AbstractLogEnabled
    implements ThreadSafe {

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeAction(org.apache.cocoon.sitemap.ExecutionContext, org.apache.cocoon.acting.Action, org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeAction(ExecutionContext context, Action action,
            Redirector redirector, SourceResolver resolver, Map objectModel,
            String resolvedSource, Parameters resolvedParams) 
    throws Exception {
        this.getLogger().info("- Invoking action '" + context.getType() + "' (" +
                           context.getLocation() + ").");
        final Map result = action.act(redirector, resolver, objectModel, resolvedSource, resolvedParams);
        if ( result != null ) {
            this.getLogger().info("- Action '" + context.getType() + "' returned a map.");
        } else {
            this.getLogger().info("- Action '" + context.getType() + "' did not return a map.");            
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#popVariables(org.apache.cocoon.sitemap.ExecutionContext)
     */
    public void popVariables(ExecutionContext context) {
        this.getLogger().info("- Variable Context ends");
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#pushVariables(org.apache.cocoon.sitemap.ExecutionContext, java.lang.String, java.util.Map)
     */
    public Map pushVariables(ExecutionContext context, String key, Map variables) {
        this.getLogger().info("- New Variable Context: " + (key != null ? "('" + key + "')" : ""));
        Iterator keys = variables.entrySet().iterator();
        while (keys.hasNext()) {
            Map.Entry entry = (Map.Entry)keys.next();
            this.getLogger().info("   " + entry.getKey() + " : " + entry.getValue());
        }
        return variables;
    }

}

