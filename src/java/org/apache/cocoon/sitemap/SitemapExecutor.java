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

/**
 * The sitemap executor executes all sitemap statements, so it actually
 * calls an action, adds a generator to the pipeline etc.
 * By separating this functionality into a single object it is easier to
 * plugin custom profiling or debugging tools.
 *
 * TODO - This is not finished yet!
 * 
 * @since 2.2
 * @version CVS $Id: SitemapExecutor.java,v 1.1 2004/06/09 11:59:23 cziegeler Exp $
 */
public interface SitemapExecutor {
    
    /** The component role */
    String ROLE = SitemapExecutor.class.getName();
    
    /**
     * Invoke an action and return the result.
     */
    Map invokeAction(ExecutionContext context,
                     Action           action, 
                     Redirector       redirector, 
                     SourceResolver   resolver, 
                     Map              objectModel, 
                     String           resolvedSource, 
                     Parameters       resolvedParams )
    throws Exception;
    
}
