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
package org.apache.cocoon.components.flow.javascript;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 *
 * @version CVS $Id: ScriptablePropertyHandler.java,v 1.7 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class ScriptablePropertyHandler implements DynamicPropertyHandler {

    public Object getProperty(Object obj, String propertyName) {
        Context cx = null;
        try {
            cx = Context.enter();
            Scriptable s = (Scriptable)obj;
            Object result = ScriptableObject.getProperty(s, propertyName);
            if (result == Scriptable.NOT_FOUND) {
                result = ScriptableObject.getProperty(s, "get" + propertyName.substring(0, 1).toUpperCase() + (propertyName.length() > 1 ? propertyName.substring(1) : ""));
                if (result != Scriptable.NOT_FOUND &&
                    result instanceof Function) {
                    try {
                        result = ((Function)result).call(cx, 
                                                         ScriptableObject.getTopLevelScope(s), s, new Object[] {});
                    } catch (JavaScriptException exc) {
                        exc.printStackTrace();
                        result = Undefined.instance;
                    }
                } 
                if (result == Undefined.instance ||
                    result == Scriptable.NOT_FOUND) {
                    result = null;
                }
            } else if (result instanceof Wrapper) {
                result = ((Wrapper)result).unwrap();
            } else if (result == Undefined.instance) {
                result = null;
            }
            return result;
        } finally {
            Context.exit();
        }
    }
    
    public String[] getPropertyNames(Object obj) {
        Context.enter();
        try {
            Object[] ids;
            if (obj instanceof ScriptableObject) {
                ids = ((ScriptableObject)obj).getAllIds();
            } else {
                ids = ((Scriptable)obj).getIds();
            }
            String[] result = new String[ids.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = (String)ids[i];
            }
            return result;
        } finally {
            Context.exit();
        }
    }
    
    public void setProperty(Object obj, String propertyName,
                            Object value) {
        Context.enter();
        try {
            if (!(value == null
                  || value instanceof String 
                  || value instanceof Number 
                  || value instanceof Boolean)) {
                value = Context.toObject(value, 
                                         (Scriptable)obj);
            }
            ScriptableObject.putProperty((Scriptable)obj,
                                         propertyName, value);
        } finally {
            Context.exit();
        }
    }
}
