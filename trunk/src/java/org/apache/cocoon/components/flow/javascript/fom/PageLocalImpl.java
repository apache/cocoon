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

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * @version CVS $Id: PageLocalImpl.java,v 1.2 2004/03/08 13:57:39 cziegeler Exp $
 */
public class PageLocalImpl extends ScriptableObject implements PageLocal {

    private PageLocalScope scope; // null if this is the prototype
    private String id;

    public PageLocalImpl() {
        this.id = String.valueOf(System.identityHashCode(this));
    }

    public void setPageLocalScope(PageLocalScope scope) {
        this.scope = scope;
    }

    public Object getId() {
        return id;
    }

    public String getClassName() {
        return "PageLocal";
    }

    public boolean has(String name, Scriptable start) {
        if (scope == null) {
            return super.has(name, start);
        }
        return scope.has(this, name);
    }

    public boolean has(int index, Scriptable start) {
        if (scope == null) {
            return super.has(index, start);
        }
        return scope.has(this, index);
    }

    public void put(String name, Scriptable start, Object value) {
        if (scope == null) {
             super.put(name, start, value);
        } else {
            scope.put(this, name, value);
        }
    }

    public void put(int index, Scriptable start, Object value) {
        if (scope == null) {
             super.put(index, start, value);
        } else {
            scope.put(this, index, value);
        }
    }

    public Object get(String name, Scriptable start) {
        if (scope == null) {
            return super.get(name, start);
        }
        return scope.get(this, name);
    }

    public Object get(int index, Scriptable start) {
        if (scope == null) {
            return super.get(index, start);
        }
        return scope.get(this, index);
    }

    public void delete(int index) {
        if (scope == null) {
            super.delete(index);
        } else {
            scope.delete(this, index);
        }
    }

    public void delete(String name) {
        if (scope == null) {
            super.delete(name);
        } else {
            scope.delete(this, name);
        }
    }

    public Object[] getIds() {
        if (scope == null) {
            return super.getIds();
        }
        return scope.getIds(this);
    }

    public Object getDefaultValue(Class hint) {
        if (scope == null) {
            return super.getDefaultValue(hint);
        }
        return scope.getDefaultValue(this, hint);
    }

}
