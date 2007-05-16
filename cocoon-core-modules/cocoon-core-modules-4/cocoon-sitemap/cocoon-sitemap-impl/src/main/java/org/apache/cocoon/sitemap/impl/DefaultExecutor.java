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
package org.apache.cocoon.sitemap.impl;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
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

/**
 * This is the default executor that does nothing but just executing the
 * statements.
 * TODO - This is not finished yet!
 * 
 * @since 2.2
 * @version $Id$
 */
public class DefaultExecutor 
    implements SitemapExecutor {
    
    /**
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
        return action.act(redirector, resolver, objectModel, 
                resolvedSource, resolvedParams);        
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.Matcher, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeMatcher(ExecutionContext context, 
                             Map objectModel,
                             Matcher matcher, 
                             String pattern, 
                             Parameters resolvedParams)
    throws PatternException {
        return matcher.match(pattern, objectModel, resolvedParams);
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
        return matcher.preparedMatch(preparedPattern, objectModel, resolvedParams);
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeSelector(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.selection.Selector, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public boolean invokeSelector(ExecutionContext context, Map objectModel,
            Selector selector, String expression, Parameters parameters) {
        return selector.select(expression, objectModel, parameters);
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeSwitchSelector(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.selection.SwitchSelector, java.lang.String, org.apache.avalon.framework.parameters.Parameters, Object)
     */
    public boolean invokeSwitchSelector(ExecutionContext context,
            Map objectModel, SwitchSelector selector, String expression,
            Parameters parameters, Object selectorContext) {
        return selector.select(expression, selectorContext);
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#popVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void popVariables(ExecutionContext context,
                             Map              objectModel) {
        // nothing to do
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#pushVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String, java.util.Map)
     */
    public Map pushVariables(ExecutionContext context, 
                             Map              objectModel,
                             String key, Map variables) {
        return variables;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#enterSitemap(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String)
     */
    public void enterSitemap(ExecutionContext context, Map objectModel, String source) {
        // nothing to do
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addGenerator(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addGenerator(ExecutionContext context,
            Map objectModel, PipelineComponentDescription desc) {
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addReader(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addReader(ExecutionContext context,
            Map objectModel, PipelineComponentDescription desc) {
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addSerializer(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addSerializer(ExecutionContext context,
            Map objectModel, PipelineComponentDescription desc) {
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addTransformer(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addTransformer(
            ExecutionContext context, Map objectModel,
            PipelineComponentDescription desc) {
        return desc;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#leaveSitemap(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void leaveSitemap(ExecutionContext context, Map objectModel) {
        // nothing to do
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#redirectTo(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String, boolean, boolean, boolean)
     */
    public String redirectTo(ExecutionContext context, Map objectModel, String uri, boolean createSession, boolean global, boolean permanent) {
        return uri;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#enteringPipeline(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription enteringPipeline(ExecutionContext context, Map objectModel, PipelineComponentDescription desc) {
        return desc;
    }

}
