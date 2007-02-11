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
package org.apache.garbage.tree;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: VariableScope.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public class VariableScope implements Variables {

    public Variables parent = null;
    public Variables variables = null;

    public VariableScope(JXPathContext context) {
        super();
        JXPathContext ctx = context.getParentContext();
        if (ctx != null) this.parent = ctx.getVariables();
        this.variables = context.getVariables();
        context.setVariables(this);
    }

    public void declareVariable(String name, Object value) {
        if ((this.parent != null) && (this.parent.isDeclaredVariable(name))) {
            this.parent.declareVariable(name, value);
        }
        this.variables.declareVariable(name, value);
    }

    public Object getVariable(String name) {
        return(this.variables.getVariable(name));
    }

    public boolean isDeclaredVariable(String name) {
        return(this.variables.isDeclaredVariable(name));
    }

    public void undeclareVariable(String name) {
        this.variables.undeclareVariable(name);
    }
}
