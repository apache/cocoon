/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.generation;
import org.mozilla.javascript.*;
import java.util.Iterator;
import org.apache.velocity.util.introspection.*;

/**
 * Velocity Introspector that supports Rhino JavaScript objects
 * as well as Java Objects
 */

public class JSIntrospector extends UberspectImpl {

    public static class JSMethod implements VelMethod {

        Scriptable scope;
        String name;

        public JSMethod(Scriptable scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        public Object invoke(Object thisArg, Object[] args)
            throws Exception {
            Context cx = Context.enter();
            try {
                Object result; 
                Scriptable thisObj;
                if (!(thisArg instanceof Scriptable)) {
                    thisObj = Context.toObject(thisArg, scope);
                } else {
                    thisObj = (Scriptable)thisArg;
                }
                result = ScriptableObject.getProperty(thisObj, name);
                Object[] newArgs = null;
                if (args != null) {
                    newArgs = new Object[args.length];
                    for (int i = 0; i < args.length; i++) {
                        newArgs[i] = args[i];
                        if (args[i] != null && 
                            !(args[i] instanceof Number) &&
                            !(args[i] instanceof Boolean) &&
                            !(args[i] instanceof String) &&
                            !(args[i] instanceof Scriptable)) {
                            newArgs[i] = Context.toObject(args[i], scope);
                        }
                    }
                }
                result = ScriptRuntime.call(cx, result, thisObj, 
                                            newArgs, scope);
                if (result == Undefined.instance ||
                    result == ScriptableObject.NOT_FOUND) {
                    result = null;
                } else while (result instanceof Wrapper) {
                    result = ((Wrapper)result).unwrap();
                }
                return result;
            } catch (JavaScriptException e) {
                throw new java.lang.reflect.InvocationTargetException(e);
            } finally {
                Context.exit();
            }
        }
        
        public boolean isCacheable() {
            return false;
        }
        
        public String getMethodName() {
            return name;
        }
        
        public Class getReturnType() {
            return Object.class;
        }
        
    }

    public static class JSPropertyGet implements VelPropertyGet {

        Scriptable scope;
        String name;
        
        public JSPropertyGet(Scriptable scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        public Object invoke(Object thisArg) throws Exception {
            Context cx = Context.enter();
            try {
                Scriptable thisObj;
                if (!(thisArg instanceof Scriptable)) {
                    thisObj = Context.toObject(thisArg, scope);
                } else {
                    thisObj = (Scriptable)thisArg;
                }
                Object result = ScriptableObject.getProperty(thisObj, name);
                if (result == Undefined.instance || 
                    result == ScriptableObject.NOT_FOUND) {
                    result = null;
                } else while (result instanceof Wrapper) {
                    result = ((Wrapper)result).unwrap();
                }
                return result;
            } finally {
                Context.exit();
            }
        }
        
        public boolean isCacheable() {
            return false;
        }

        public String getMethodName() {
            return name;
        }

    }

    public static class JSPropertySet implements VelPropertySet {

        Scriptable scope;
        String name;
        
        public JSPropertySet(Scriptable scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        public Object invoke(Object thisArg, Object rhs) throws Exception {
            Context cx = Context.enter();
            try {
                Scriptable thisObj;
                Object arg = rhs;
                if (!(thisArg instanceof Scriptable)) {
                    thisObj = Context.toObject(thisArg, scope);
                } else {
                    thisObj = (Scriptable)thisArg;
                }
                if (arg != null && 
                    !(arg instanceof Number) &&
                    !(arg instanceof Boolean) &&
                    !(arg instanceof String) &&
                    !(arg instanceof Scriptable)) {
                    arg = Context.toObject(arg, scope);
                }
                ScriptableObject.putProperty(thisObj, name, arg);
                return rhs;
            } finally {
                Context.exit();
            }
        }

        public boolean isCacheable() {
            return false;
        }

        public String getMethodName() {
            return name;        
        }
    }

    public static class NativeArrayIterator implements Iterator {

        NativeArray arr;
        int index;

        public NativeArrayIterator(NativeArray arr) {
            this.arr = arr;
            this.index = 0;
        }

        public boolean hasNext() {
            return index < (int)arr.jsGet_length();
        }

        public Object next() {
            Context cx = Context.enter();
            try {
                Object result = arr.get(index++, arr);
                if (result == Undefined.instance ||
                    result == ScriptableObject.NOT_FOUND) {
                    result = null;
                } else while (result instanceof Wrapper) {
                    result = ((Wrapper)result).unwrap();
                }
                return result;
            } finally {
                Context.exit();
            }
        }

        public void remove() {
            arr.delete(index);
        }
    }

    public static class ScriptableIterator implements Iterator {

        Scriptable scope;
        Object[] ids;
        int index;

        public ScriptableIterator(Scriptable scope) {
            this.scope = scope;
            this.ids = scope.getIds();
            this.index = 0;
        }

        public boolean hasNext() {
            return index < ids.length;
        }

        public Object next() {
            Context cx = Context.enter();
            try {
                Object result = 
                    ScriptableObject.getProperty(scope, 
                                                 ids[index++].toString());
                if (result == Undefined.instance ||
                    result == ScriptableObject.NOT_FOUND) {
                    result = null;
                } else while (result instanceof Wrapper) {
                    result = ((Wrapper)result).unwrap();
                }
                return result;
            } finally {
                Context.exit();
            }
        }

        public void remove() {
            Context cx = Context.enter();
            try {
                scope.delete(ids[index].toString());
            } finally {
                Context.exit();
            }
        }
    }

    public Iterator getIterator(Object obj, Info i)
        throws Exception {
        if (!(obj instanceof Scriptable)) {
            return super.getIterator(obj, i);
        }
        if (obj instanceof NativeArray) {
            return new NativeArrayIterator((NativeArray)obj);
        }
        return new ScriptableIterator((Scriptable)obj);
    }

    public VelMethod getMethod(Object obj, String methodName, 
                               Object[] args, Info i)
        throws Exception {
        if (!(obj instanceof Scriptable)) {
            return super.getMethod(obj, methodName, args, i);
        }
        return new JSMethod((Scriptable)obj, methodName);
    }

    public VelPropertyGet getPropertyGet(Object obj, String identifier, 
                                         Info i)
        throws Exception {
        if (!(obj instanceof Scriptable)) {
            return super.getPropertyGet(obj, identifier, i);
        }
        return new JSPropertyGet((Scriptable)obj, identifier);
    }

    public VelPropertySet getPropertySet(Object obj, String identifier, 
                                         Object arg, Info i)
        throws Exception {
        if (!(obj instanceof Scriptable)) {
            return super.getPropertySet(obj, identifier, arg, i);
        }
        return new JSPropertySet((Scriptable)obj, identifier);
    }
}
