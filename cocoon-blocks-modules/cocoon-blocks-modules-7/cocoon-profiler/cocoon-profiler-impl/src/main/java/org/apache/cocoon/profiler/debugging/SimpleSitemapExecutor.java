/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.profiler.debugging;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;
import org.apache.cocoon.sitemap.ExecutionContext;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapExecutor;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Sample sitemap executor that prints out everything to a logger
 * 
 * @since 2.2
 * @version $Id$
 */
public class SimpleSitemapExecutor extends AbstractLogEnabled
                                   implements ThreadSafe, SitemapExecutor {

    /**
     * @see SitemapExecutor#invokeAction(ExecutionContext, Map, Action, org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.lang.String, Parameters)
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
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.Matcher, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeMatcher(ExecutionContext context, 
                             Map              objectModel,
                             Matcher          matcher,
                             String           pattern,
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
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokePreparableMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.PreparableMatcher, java.lang.String, java.lang.Object, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokePreparableMatcher(ExecutionContext  context,
                                       Map               objectModel,
                                       PreparableMatcher matcher,
                                       String            pattern,
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

    /**
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
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeSwitchSelector(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.selection.SwitchSelector, java.lang.String, org.apache.avalon.framework.parameters.Parameters, java.lang.Object)
     */
    public boolean invokeSwitchSelector(ExecutionContext context,
            Map objectModel, SwitchSelector selector, String expression,
            Parameters parameters, Object selectorContext) {
        this.getLogger().info("- Invoking selector '" + context.getType() + "' (" +
                context.getLocation() + ").");
        final boolean result = selector.select(expression, selectorContext);
        if ( result ) {
            this.getLogger().info("- Selector '" + context.getType() + "' succeeded.");
        } else {
            this.getLogger().info("- Selector '" + context.getType() + "' failed.");            
        }
        return result;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#popVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void popVariables(ExecutionContext context,
                             Map              objectModel) {
        this.getLogger().info("- Variable Context ends");
    }
    
    /**
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

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#enterSitemap(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String)
     */
    public void enterSitemap(ExecutionContext context, 
                               Map objectModel,
                               String source) {
        this.getLogger().info("- Entering sitemap " + source);
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addGenerator(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addGenerator(ExecutionContext context,
            Map objectModel, PipelineComponentDescription desc) {
        this.getLogger().info("- Adding generator '" + desc.type + "' (" +
                context.getLocation() + ").");
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addReader(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addReader(ExecutionContext context,
            Map objectModel, PipelineComponentDescription desc) {
        this.getLogger().info("- Adding reader '" + desc.type + "' (" +
                context.getLocation() + ").");
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addSerializer(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addSerializer(ExecutionContext context,
            Map objectModel, PipelineComponentDescription desc) {
        this.getLogger().info("- Adding serializer '" + desc.type + "' (" +
                context.getLocation() + ").");
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addTransformer(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addTransformer(
            ExecutionContext context, Map objectModel,
            PipelineComponentDescription desc) {
        this.getLogger().info("- Adding transformer '" + desc.type + "' (" +
                context.getLocation() + ").");
        return desc;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#leaveSitemap(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void leaveSitemap(ExecutionContext context, Map objectModel) {
        this.getLogger().info("- Leaving sitemap");
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#redirectTo(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String, boolean, boolean, boolean)
     */
    public String redirectTo(ExecutionContext context, Map objectModel, String uri, boolean createSession, boolean global, boolean permanent) {
        this.getLogger().info("- Redirecting to " + uri);
        return uri;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#enteringPipeline(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription enteringPipeline(ExecutionContext context, Map objectModel, PipelineComponentDescription desc) {
        this.getLogger().info("- Entering new pipeline section.");
        return desc;
    }

}
