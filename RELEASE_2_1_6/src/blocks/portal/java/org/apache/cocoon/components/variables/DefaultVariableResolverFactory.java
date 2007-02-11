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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.sitemap.PatternException;

/**
 * This factory component creates a {@link VariableResolver} for an expression.
 * A {@link VariableResolver} can then be used at runtime to resolve
 * a variable with the current value.
 * A variable can contain dynamic parts that are contained in {...},
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *
 * @version CVS $Id: DefaultVariableResolverFactory.java,v 1.3 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class DefaultVariableResolverFactory 
    extends AbstractLogEnabled
    implements ThreadSafe, VariableResolverFactory, Component, Serviceable, Contextualizable {
    
    protected ServiceManager manager;
    protected Context        context;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get a resolver for a given expression. Chooses the most efficient implementation
     * depending on <code>expression</code>.
     * Don't forget to release the resolver
     */
    public VariableResolver lookup(String expression) 
    throws PatternException {
        if ( this.needsResolve( expression ) ) {
            return new PreparedVariableResolver( expression, this.manager, this.context);
        } else {
            return new NOPVariableResolver( expression );
        }
    }

    public void release(VariableResolver resolver) {
        if ( resolver != null ) {
            ((Disposable)resolver).dispose();
        }
    }
    
    /**
     * Does an expression need resolving (i.e. contain {...} patterns) ?
     */
    protected boolean needsResolve(String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        }

        // Is the first char a '{' ?
        if (expression.charAt(0) == '{') {
            return true;
        }

        if (expression.length() < 2) {
            return false;
        }

        // Is there any unescaped '{' ?
        int pos = 1;
        while ( (pos = expression.indexOf('{', pos)) != -1) {
            // Found a '{' : is it escaped ?
            if (expression.charAt(pos - 1) != '\\') {
                // No : need to resolve
                return true;
            }
            pos++;
        }
        // Nothing found...
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}


