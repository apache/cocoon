/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.el.impl.jexl;

import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.jexl.util.introspection.Info;
import org.apache.commons.jexl.util.introspection.UberspectImpl;
import org.apache.commons.jexl.util.introspection.VelMethod;
import org.apache.commons.jexl.util.introspection.VelPropertyGet;
import org.apache.commons.jexl.util.introspection.VelPropertySet;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;


/**
 * Jexl Introspector that supports Rhino JavaScript objects
 * as well as Java Objects.
 *
 * @version $Id$
 */
public class JSIntrospector extends UberspectImpl {

    static class JSMethod implements VelMethod {

        Scriptable scope;
        String name;

        public JSMethod(Scriptable scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        public Object invoke(Object thisArg, Object[] args) throws Exception {
            Context cx = Context.enter();
            try {
                Object result;
                Scriptable thisObj = !(thisArg instanceof Scriptable) ?
                        Context.toObject(thisArg, scope) : (Scriptable)thisArg;
                result = ScriptableObject.getProperty(thisObj, name);
                Object[] newArgs = null;
                if (args != null) {
                    newArgs = new Object[args.length];
                    int len = args.length;
                    for (int i = 0; i < len; i++) {
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
                result = ScriptRuntime.call(cx, result, thisObj, newArgs, scope);

                return unwrap(result);
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

    static class JSPropertyGet implements VelPropertyGet {

        Scriptable scope;
        String name;

        public JSPropertyGet(Scriptable scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        public Object invoke(Object thisArg) throws Exception {
            Context cx = Context.enter();
            try {
                Scriptable thisObj = !(thisArg instanceof Scriptable) ?
                        Context.toObject(thisArg, scope) : (Scriptable)thisArg;
                Object result = ScriptableObject.getProperty(thisObj, name);
                if (result == Scriptable.NOT_FOUND) {
                    result = ScriptableObject.getProperty(thisObj, "get" + StringUtils.capitalize(name));
                    if (result != Scriptable.NOT_FOUND && result instanceof Function) {
                        try {
                            result = ((Function)result).call(
                                    cx, ScriptableObject.getTopLevelScope(thisObj), thisObj, new Object[] {});
                        } catch (JavaScriptException exc) {
                            exc.printStackTrace();
                            result = null;
                        }
                    }
                }

                return unwrap(result);
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

    static class JSPropertySet implements VelPropertySet {

        Scriptable scope;
        String name;

        public JSPropertySet(Scriptable scope, String name) {
            this.scope = scope;
            this.name = name;
        }

        public Object invoke(Object thisArg, Object rhs) throws Exception {
            Context.enter();
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
            return index < (int) arr.getLength();
        }

        public Object next() {
            Context.enter();
            try {
                Object result = arr.get(index++, arr);

                return unwrap(result);
            } finally {
                Context.exit();
            }
        }

        public void remove() {
            arr.delete(index);
        }
    }

    static class ScriptableIterator implements Iterator {

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
            Context.enter();
            try {
                Object result = ScriptableObject.getProperty(scope, ids[index++].toString());

                return unwrap(result);
            } finally {
                Context.exit();
            }
        }

        public void remove() {
            Context.enter();
            try {
                scope.delete(ids[index].toString());
            } finally {
                Context.exit();
            }
        }
    }

    public Iterator getIterator(Object obj, Info i) throws Exception {
        if (!(obj instanceof Scriptable)) {
            // support Enumeration
            /*
               Booth Enumeration and Iterator are supported in
               Uberspect. The only difference is that they emit a
               rather long warning message to commons logging, telling
               that Enumerations and Iterator not are resettable and
               cannot be reused.
            */
            if (obj instanceof Enumeration) {
                final Enumeration e = (Enumeration)obj;
                return new Iterator() {

                        public boolean hasNext() {
                            return e.hasMoreElements();
                        }

                        public Object next() {
                            return e.nextElement();
                        }

                        public void remove() {
                            // no action
                        }

                    };
            }
            if (obj instanceof Iterator) {
                // support Iterator
                return (Iterator)obj;
            }
            return super.getIterator(obj, i);
        }
        if (obj instanceof NativeArray) {
            return new NativeArrayIterator((NativeArray)obj);
        }
        return new ScriptableIterator((Scriptable)obj);
    }

    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i) throws Exception {
        return !(obj instanceof Scriptable) ?
                super.getMethod(obj, methodName, args, i) : new JSMethod((Scriptable)obj, methodName);
    }

    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i) throws Exception {
        return !(obj instanceof Scriptable) ?
                super.getPropertyGet(obj, identifier, i) : new JSPropertyGet((Scriptable)obj, identifier);
    }

    public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info i) throws Exception {
        return !(obj instanceof Scriptable) ?
                super.getPropertySet(obj, identifier, arg, i) : new JSPropertySet((Scriptable)obj, identifier);
    }

    private static Object unwrap(Object result) {
        if (result == Undefined.instance || result == Scriptable.NOT_FOUND) {
            return null;
        }

        if (!(result instanceof NativeJavaClass)) {
            Object value;
            while (result instanceof Wrapper) {
                value = ((Wrapper) result).unwrap();
                if (value == result) {
                    break;
                }

                result = value;
            }
        }

        return result;
    }
}
