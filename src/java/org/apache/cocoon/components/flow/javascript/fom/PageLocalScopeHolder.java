package org.apache.cocoon.components.flow.javascript.fom;
import org.mozilla.javascript.*;

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
