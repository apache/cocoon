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

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

import java.util.Map;

/**
 * Wrap a java.util.Map for JavaScript.
 *
 * @version CVS $Id: ScriptableMap.java,v 1.3 2004/03/08 13:57:39 cziegeler Exp $
 */
public class ScriptableMap implements Scriptable, Wrapper {

    private Map map;
    private Scriptable prototype, parent;

    public ScriptableMap() {
    }

    public ScriptableMap(Map map) {
        this.map = map;
    }

    public String getClassName() {
        return "Map";
    }

    public boolean has(String name, Scriptable start) {
        return this.map.containsKey(name);
    }

    /**
     * no numeric properties
     */
    public boolean has(int index, Scriptable start) {
        return false;
    }

    public Object get(String name, Scriptable start) {
        if (this.map.containsKey(name))
            return this.map.get(name);

        return NOT_FOUND;
    }

    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
    }

    public void put(String name, Scriptable start, Object value) {
        if (value instanceof NativeJavaObject) {
            value = ((NativeJavaObject)value).unwrap();
        }
        map.put(name, value);
    }

    public void put(int index, Scriptable start, Object value) {
    }

    public void delete(String id) {
        map.remove(id);
    }

    public void delete(int index) {
    }

    public Scriptable getPrototype() {
        return prototype;
    }

    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    public Scriptable getParentScope() {
        return parent;
    }

    public void setParentScope(Scriptable parent) {
        this.parent = parent;
    }

    public Object[] getIds() {
        return this.map.keySet().toArray();
    }

    public Object getDefaultValue(Class typeHint) {
        return this.map.toString();
    }

    public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this)) 
                return true;
            proto = proto.getPrototype();
        }

        return false;
    }

    /**
     * Return the java.util.Map that is wrapped by this class.
     */
    public Object unwrap() {
        return this.map;
    }

}
