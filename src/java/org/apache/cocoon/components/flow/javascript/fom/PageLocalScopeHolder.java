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
 * @version CVS $Id: PageLocalScopeHolder.java,v 1.3 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class PageLocalScopeHolder implements PageLocalScope {

    private Scriptable scope;
    private PageLocalScopeImpl delegate;

    public PageLocalScopeHolder(Scriptable scope) {
        this.scope = scope;
    }

    public boolean has(PageLocal local, String name) {
        return delegate.has(local, name);
    }

    public boolean has(PageLocal local, int index) {
        return delegate.has(local, index);
    }

    public Object get(PageLocal local, String name) {
        return delegate.get(local, name);
    }

    public Object get(PageLocal local, int index) {
        return delegate.get(local, index);
    }

    public void put(PageLocal local, String name, Object value) {
        delegate.put(local, name, value);
    }

    public void put(PageLocal local, int index, Object value) {
        delegate.put(local, index, value);
    }

    public void delete(PageLocal local, String name) {
        delegate.delete(local, name);
    }

    public void delete(PageLocal local, int index) {
        delegate.delete(local, index);
    }

    public Object[] getIds(PageLocal local) {
        return delegate.getIds(local);
    }

    public Object getDefaultValue(PageLocal local, Class hint) {
        return delegate.getDefaultValue(local, hint);
    }

    public void setDelegate(PageLocalScopeImpl delegate) {
        this.delegate = delegate;
    }

    public PageLocalScopeImpl getDelegate() {
        return delegate;
    }

    public PageLocal createPageLocal() {
        PageLocalImpl pageLocal = new PageLocalImpl();
        pageLocal.setPrototype(ScriptableObject.getClassPrototype(scope,
                                                                  pageLocal.getClassName()));
        pageLocal.setParentScope(scope);
        pageLocal.setPageLocalScope(this);
        return pageLocal;
    }
}
