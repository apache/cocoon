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
package org.apache.cocoon.sitemap;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.selection.Selector;

/**
 * The sitemap executor executes all sitemap statements, so it actually
 * calls an action, adds a generator to the pipeline etc.
 * By separating this functionality into a single object it is easier to
 * plugin custom profiling or debugging tools.
 *
 * TODO - This is not finished yet!
 * 
 * @since 2.2
 * @version CVS $Id: SitemapExecutor.java,v 1.4 2004/06/17 13:52:35 cziegeler Exp $
 */
public interface SitemapExecutor {
    
    /** The component role */
    String ROLE = SitemapExecutor.class.getName();
    
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
     * @return
     */
    boolean invokeSelector(ExecutionContext context,
            Map               objectModel,
            Selector selector, 
            String expression, 
            Parameters parameters);
    
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
}
