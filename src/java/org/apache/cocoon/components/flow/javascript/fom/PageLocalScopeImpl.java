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
package org.apache.cocoon.components.flow.javascript.fom;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version CVS $Id: PageLocalScopeImpl.java,v 1.2 2004/03/08 13:57:39 cziegeler Exp $
 */
public class PageLocalScopeImpl implements PageLocalScope {

    private Map locals;
    private Scriptable scope;

    public PageLocalScopeImpl(Scriptable scope) {
        locals = new HashMap();
        this.scope = scope;
    }

    private Scriptable newObject() {
        try {
            return Context.getCurrentContext().newObject(scope);
        } catch (Exception ignored) {
            // can't happen here
            ignored.printStackTrace();
            throw new Error("error: " + ignored);
        }
    }

    private PageLocalScopeImpl(PageLocalScopeImpl toBeCloned) {
        this.scope = toBeCloned.scope;
        locals = new HashMap();
        Iterator iter = toBeCloned.locals.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry)iter.next();
            Object key = e.getKey();
            Object value = e.getValue();
            // clone it
            Scriptable obj = (Scriptable)value;
            Scriptable newObj = newObject();
            Object[] ids = obj.getIds();
            for (int i = 0; i < ids.length; i++) {
                String name = ids[i].toString();
                newObj.put(name, newObj, obj.get(name, obj));
            }
            value = newObj;
            locals.put(key, value);
        }
   }

    private Scriptable resolve(PageLocal local) {
        final Object id = local.getId();
        Scriptable result = (Scriptable)locals.get(id);
        if (result == null) {
            locals.put(id, result = newObject());
        }
        return result;
    }

    public boolean has(PageLocal local, String name) {
        Scriptable obj = resolve(local);
        return obj.has(name, obj);
    }

    public boolean has(PageLocal local, int index) {
        Scriptable obj = resolve(local);
        return obj.has(index, obj);
    }

    public Object get(PageLocal local, String name) {
        Scriptable obj = resolve(local);
        return obj.get(name, obj);
    }

    public Object get(PageLocal local, int index) {
        Scriptable obj = resolve(local);
        return obj.get(index, obj);
    }

    public void put(PageLocal local, String name, Object value) {
        Scriptable obj = resolve(local);
        obj.put(name, obj, value);
    }

    public void put(PageLocal local, int index, Object value) {
        Scriptable obj = resolve(local);
        obj.put(index, obj, value);
    }

    public void delete(PageLocal local, String name) {
        Scriptable obj = resolve(local);
        obj.delete(name);
    }

    public void delete(PageLocal local, int index) {
        Scriptable obj = resolve(local);
        obj.delete(index);
    }

    public Object[] getIds(PageLocal local) {
        Scriptable obj = resolve(local);
        return obj.getIds();
    }

    public Object getDefaultValue(PageLocal local, Class hint) {
        Scriptable obj = resolve(local);
        return obj.getDefaultValue(hint);
    }

    public PageLocalScopeImpl duplicate() {
        return new PageLocalScopeImpl(this);
    }

    public PageLocal createPageLocal() {
        // not used
        return null;
    }
}
