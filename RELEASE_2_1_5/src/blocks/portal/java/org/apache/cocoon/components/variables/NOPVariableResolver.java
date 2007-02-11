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
import org.apache.cocoon.sitemap.PatternException;

/**
 * No-op implementation of {@link VariableResolver} for constant expressions
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: NOPVariableResolver.java,v 1.2 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class NOPVariableResolver 
    implements VariableResolver, Disposable {

    protected String expression;

    public NOPVariableResolver(String expression) {
        this.expression = this.unescape(expression);
    }

    /**
     * Unescape an expression that doesn't need to be resolved, but may contain
     * escaped '{' characters.
     *
     * @param expression the expression to unescape.
     * @return the unescaped result, or <code>expression</code> if unescaping isn't necessary.
     */
    protected String unescape(String expression) {
        // Does it need escaping ?
        if (expression == null || expression.indexOf("\\{") == -1) {
            return expression;
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch != '\\' || i >= (expression.length() - 1) || expression.charAt(i+1) != '{') {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.variables.VariableResolver#resolve()
     */
    public String resolve() throws PatternException {
        return this.expression;
    }

    public void dispose() {
    }
}
