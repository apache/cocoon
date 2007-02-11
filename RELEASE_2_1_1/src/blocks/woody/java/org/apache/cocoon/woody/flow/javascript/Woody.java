package org.apache.cocoon.woody.flow.javascript;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.components.flow.javascript.fom.FOM_WebContinuation;
import org.apache.cocoon.environment.Request;

/**
 * Woody-flowscript integration helper class.
 */
public class Woody extends ScriptableObject {
    FOM_Cocoon cocoon;

    public String getClassName() {
        return "Woody";
    }

    private FOM_Cocoon getCocoon() {
        if (cocoon == null) {
            cocoon = (FOM_Cocoon)getProperty(getTopLevelScope(this), "cocoon");
        }
        return cocoon;
    }

    public FOM_WebContinuation jsFunction_makeWebContinuation(Object k,
                                                              Object lastContinuation,
                                                              int ttl)
        throws Exception {
        Continuation kont = (Continuation)unwrap(k);
        FOM_WebContinuation fom_wk =
            (FOM_WebContinuation)unwrap(lastContinuation);
        FOM_Cocoon cocoon = getCocoon();
        return cocoon.makeWebContinuation(kont, fom_wk, ttl);
    }

    public void jsFunction_forwardTo(String uri,
                                     Object bizData,
                                     Object continuation)
        throws Exception {
        FOM_Cocoon cocoon = getCocoon();
        FOM_WebContinuation fom_wk =
            (FOM_WebContinuation)unwrap(continuation);
        cocoon.forwardTo(uri,
                         unwrap(bizData),
                         fom_wk);

    }

    public Request jsGet_request() {
        FOM_Cocoon cocoon = getCocoon();
        return cocoon.getRequest();
    }

    private Object unwrap(Object obj) {
        if (obj == Undefined.instance) {
            return null;
        }
        if (obj instanceof Wrapper) {
            return ((Wrapper)obj).unwrap();
        }
        return obj;
    }
}
