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
package org.apache.cocoon.components.flow.java.test;

import org.mozilla.javascript.Context;
//import org.mozilla.javascript.IdFunction;
//import org.mozilla.javascript.IdFunctionMaster;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;


/**
 * @author stephan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LinkerContinuable { //extends IdFunction {
    
    /**
     * @param master
     * @param name
     * @param id
    public LinkerContinuable(IdFunctionMaster master, String name, int id) {
        super(master, name, id);
        // TODO Auto-generated constructor stub
    }
     */

    public static void init(Context cx, Scriptable scope, boolean sealed) {
	    
	}

    /* (non-Javadoc)
     * @see org.mozilla.javascript.IdFunctionMaster#execMethod(int, org.mozilla.javascript.IdFunction, org.mozilla.javascript.Context, org.mozilla.javascript.Scriptable, org.mozilla.javascript.Scriptable, java.lang.Object[])
    public Object execMethod(int methodId, IdFunction function, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) throws JavaScriptException {
        // TODO Auto-generated method stub
        return null;
    }
     */

    /* (non-Javadoc)
     * @see org.mozilla.javascript.IdFunctionMaster#methodArity(int)
     */
    public int methodArity(int methodId) {
        // TODO Auto-generated method stub
        return 0;
    }
}
