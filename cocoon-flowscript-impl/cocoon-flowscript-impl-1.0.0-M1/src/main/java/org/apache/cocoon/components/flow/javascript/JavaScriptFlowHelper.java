package org.apache.cocoon.components.flow.javascript;

import org.apache.cocoon.components.flow.FlowHelper;

import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

public class JavaScriptFlowHelper extends FlowHelper {

    /**
     * Unwrap a Rhino object (getting the raw java object) and convert undefined to null
     */
    public static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }

}
