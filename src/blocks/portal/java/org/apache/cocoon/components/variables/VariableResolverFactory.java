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
package org.apache.cocoon.components.variables;

import org.apache.cocoon.sitemap.PatternException;

/**
 * This factory component creates a {@link VariableResolver} for an expression.
 * A {@link VariableResolver} can then be used at runtime to resolve
 * a variable with the current value.
 * A variable can contain dynamic parts that are contained in {...},
 *
 * NOTE: This interface is work in progress, so chances are that it will
 * change.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *
 * @version CVS $Id: VariableResolverFactory.java,v 1.2 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public interface VariableResolverFactory {
    
    String ROLE = VariableResolverFactory.class.getName();
    
    /**
     * Get a resolver for a given expression. Chooses the most efficient implementation
     * depending on <code>expression</code>.
     * Don't forget to release the resolver
     */
    VariableResolver lookup(String expression) 
    throws PatternException;

    void release(VariableResolver resolver);
}


