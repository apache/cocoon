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
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.sitemap.ExecutionContext;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapExecutor;

/**
 * Sampe sitemap executor that prints out everything to a logger
 * 
 * @since 2.2
 * @version CVS $Id: SimpleSitemapExecutor.java,v 1.3 2004/06/17 13:52:35 cziegeler Exp $
 */
public class SimpleSitemapExecutor 
    extends AbstractLogEnabled
    implements ThreadSafe, SitemapExecutor {

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeAction(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.acting.Action, org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeAction(final ExecutionContext context,
                            final Map              objectModel, 
                            final Action           action, 
                            final Redirector       redirector, 
                            final SourceResolver   resolver, 
                            final String           resolvedSource, 
                            final Parameters       resolvedParams )
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
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.Matcher, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeMatcher(ExecutionContext context, 
                             Map objectModel,
                             Matcher matcher, 
                             String pattern, 
                             Parameters resolvedParams)
    throws PatternException {
        this.getLogger().info("- Invoking matcher '" + context.getType() + "' (" +
                context.getLocation() + ").");
        final Map result = matcher.match(pattern, objectModel, resolvedParams);
        if ( result != null ) {
            this.getLogger().info("- Matcher '" + context.getType() + "' returned a map.");
        } else {
            this.getLogger().info("- Matcher '" + context.getType() + "' did not return a map.");            
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokePreparableMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.PreparableMatcher, java.lang.Object, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokePreparableMatcher(ExecutionContext  context,
                                       Map               objectModel,
                                       PreparableMatcher matcher,
                                       Object            preparedPattern,
                                       Parameters        resolvedParams )
    throws PatternException {
        this.getLogger().info("- Invoking matcher '" + context.getType() + "' (" +
                context.getLocation() + ").");
        final Map result = matcher.preparedMatch(preparedPattern, objectModel, resolvedParams);
        if ( result != null ) {
            this.getLogger().info("- Matcher '" + context.getType() + "' returned a map.");
        } else {
            this.getLogger().info("- Matcher '" + context.getType() + "' did not return a map.");            
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeSelector(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.selection.Selector, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public boolean invokeSelector(ExecutionContext context, Map objectModel,
            Selector selector, String expression, Parameters parameters) {
        this.getLogger().info("- Invoking selector '" + context.getType() + "' (" +
                context.getLocation() + ").");
        final boolean result = selector.select(expression, objectModel, parameters);
        if ( result ) {
            this.getLogger().info("- Selector '" + context.getType() + "' succeeded.");
        } else {
            this.getLogger().info("- Selector '" + context.getType() + "' failed.");            
        }
        return result;
    }
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#popVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void popVariables(ExecutionContext context,
                             Map              objectModel) {
        this.getLogger().info("- Variable Context ends");
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapExecutor#pushVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String, java.util.Map)
     */
    public Map pushVariables(ExecutionContext context, 
                             Map              objectModel,
                             String key, Map variables) {
        this.getLogger().info("- New Variable Context: " + (key != null ? "('" + key + "')" : ""));
        Iterator keys = variables.entrySet().iterator();
        while (keys.hasNext()) {
            Map.Entry entry = (Map.Entry)keys.next();
            this.getLogger().info("   " + entry.getKey() + " : " + entry.getValue());
        }
        return variables;
    }
    
}

