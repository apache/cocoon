/*
 * Created on 27.06.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.cocoon.components.flow.java.test;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.IdFunction;
import org.mozilla.javascript.IdFunctionMaster;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;


/**
 * @author stephan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LinkerContinuable extends IdFunction {
    
    /**
     * @param master
     * @param name
     * @param id
     */
    public LinkerContinuable(IdFunctionMaster master, String name, int id) {
        super(master, name, id);
        // TODO Auto-generated constructor stub
    }

    public static void init(Context cx, Scriptable scope, boolean sealed) {
	    
	}

    /* (non-Javadoc)
     * @see org.mozilla.javascript.IdFunctionMaster#execMethod(int, org.mozilla.javascript.IdFunction, org.mozilla.javascript.Context, org.mozilla.javascript.Scriptable, org.mozilla.javascript.Scriptable, java.lang.Object[])
     */
    public Object execMethod(int methodId, IdFunction function, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) throws JavaScriptException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.mozilla.javascript.IdFunctionMaster#methodArity(int)
     */
    public int methodArity(int methodId) {
        // TODO Auto-generated method stub
        return 0;
    }
}
