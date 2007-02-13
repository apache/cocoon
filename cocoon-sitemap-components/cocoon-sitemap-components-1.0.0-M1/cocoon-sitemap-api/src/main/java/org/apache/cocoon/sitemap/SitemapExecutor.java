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
package org.apache.cocoon.sitemap;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;

/**
 * The sitemap executor executes all sitemap statements, so it actually
 * calls an action, adds a generator to the pipeline etc.
 * By separating this functionality into a single object it is easier to
 * plugin custom profiling or debugging tools.
 *
 * TODO - This is not finished yet!
 * TODO - we should add invocation of a Redirector as well
 * 
 * @since 2.2
 * @version $Id$
 */
public interface SitemapExecutor {
    
    /** The component role */
    String ROLE = SitemapExecutor.class.getName();
    
    public static class PipelineComponentDescription {
        public String type;
        public String source;
        public Parameters parameters;
        public Parameters hintParameters;
        /** Mime-type for serializers and readers */
        public String mimeType;
    }
    
    /**
     * Invoke an action and return the result.
     */
    Map invokeAction(ExecutionContext context,
                     Map              objectModel, 
                     Action           action, 
                     Redirector       redirector, 
                     SourceResolver   resolver, 
                     String           source, 
                     Parameters       parameters )
    throws Exception;
    
    /**
     * Invoke a match and return the result
     */
    Map invokeMatcher(ExecutionContext context,
                      Map              objectModel,
                      Matcher          matcher,
                      String           pattern,
                      Parameters       parameters )
    throws PatternException;
    
    /**
     * Invoke a match and return the result
     */
    Map invokePreparableMatcher(ExecutionContext context,
                      Map               objectModel,
                      PreparableMatcher matcher,
                      String            pattern,
                      Object            preparedPattern,
                      Parameters        parameters )
    throws PatternException;

    /**
     * Invoke a selector
     * @param context
     * @param objectModel
     * @param selector
     * @param expression
     * @param parameters
     * @return True if the selector did match.
     */
    boolean invokeSelector(ExecutionContext context,
            Map               objectModel,
            Selector selector, 
            String expression, 
            Parameters parameters);
    
    /**
     * Invoke a switch selector
     * @param context
     * @param objectModel
     * @param selector
     * @param expression
     * @param parameters
     * @param selectorContext The context object for the switch selector
     * @return True if the selector did match.
     */
    boolean invokeSwitchSelector(ExecutionContext context,
                                 Map             objectModel,
                                 SwitchSelector  selector, 
                                 String expression, 
                                 Parameters parameters,
                                 Object selectorContext);

    /**
     * Push map of information on the context stack.
     * @param context The execution context
     * @param objectModel The object model
     * @param key A key that can be used to identify this map (can be null)
     * @param variables The variables as key/value pairs
     * @return The variables that are used in the sitemap. The executor can
     *         modify the set of available variables by returning a different
     *         map.
     */
    Map pushVariables(ExecutionContext context, 
                      Map              objectModel,
                      String           key, 
                      Map              variables);
    
    /**
     * Pop a map of information from the context stack.
     * @param context     The execution context
     * @param objectModel The object model
     */
    void popVariables(ExecutionContext context,
                      Map              objectModel);
    
    /**
     * Enter a new sitemap
     * @param context     The execution context
     * @param objectModel The object model
     * @param source The uri of the sitemap
     */
    void enterSitemap(ExecutionContext context, 
                      Map              objectModel,
                      String           source);

    /**
     * Leaves a sitemap.
     */
    void leaveSitemap(ExecutionContext context,
                      Map              objectModel);

    /**
     * Add a generator
     * @param context
     * @param objectModel
     * @param desc The descrption of the component
     * @return The desc of the component to use
     */
    PipelineComponentDescription addGenerator(ExecutionContext context, 
                                              Map              objectModel,
                                              PipelineComponentDescription desc);

    /**
     * Add a transformer
     * @param context
     * @param objectModel
     * @param desc The descrption of the component
     * @return The desc of the component to use
     */
    PipelineComponentDescription addTransformer(ExecutionContext context, 
                                                Map              objectModel,
                                                PipelineComponentDescription desc);

    /**
     * Add a serializer
     * @param context
     * @param objectModel
     * @param desc The descrption of the component
     * @return The desc of the component to use
     */
    PipelineComponentDescription addSerializer(ExecutionContext context, 
                                               Map              objectModel,
                                               PipelineComponentDescription desc);

    /**
     * Add a reader
     * @param context
     * @param objectModel
     * @param desc The descrption of the component
     * @return The desc of the component to use
     */
    PipelineComponentDescription addReader(ExecutionContext context, 
                                           Map              objectModel,
                                           PipelineComponentDescription desc);

    /**
     * This informs the executor about a new pipeline section.
     * @param context
     * @param objectModel
     * @param desc
     * @return A (new) description for the pipeline component to use.
     */
    PipelineComponentDescription enteringPipeline(ExecutionContext context,
                                                  Map              objectModel,
                                                  PipelineComponentDescription desc);

    /**
     * Informs about a redirect.
     * @return The uri to redirect to.
     */
    String redirectTo(ExecutionContext context,
                      Map              objectModel,
                      String           uri,
                      boolean          createSession,
                      boolean          global,
                      boolean          permanent);
    
}
