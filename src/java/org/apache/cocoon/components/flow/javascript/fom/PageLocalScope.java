package org.apache.cocoon.components.flow.javascript.fom;

public interface PageLocalScope {

    public boolean has(PageLocal local, String name);

    public boolean has(PageLocal local, int index);

    public Object get(PageLocal local, String name);

    public Object get(PageLocal local, int index);

    public void put(PageLocal local, String name, Object value);

    public void put(PageLocal local, int index, Object value);

    public void delete(PageLocal local, String name);

    public void delete(PageLocal local, int index);

    public Object[] getIds(PageLocal local);

    public Object getDefaultValue(PageLocal local, Class hint);

    public PageLocal createPageLocal();

}
