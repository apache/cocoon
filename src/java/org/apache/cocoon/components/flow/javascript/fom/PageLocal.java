package org.apache.cocoon.components.flow.javascript.fom;
import org.mozilla.javascript.Scriptable;

public interface PageLocal extends Scriptable {

    public Object getId();

    public void setPageLocalScope(PageLocalScope scope);

}
