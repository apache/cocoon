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
package org.apache.cocoon.forms.flow.javascript;

import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;
import org.apache.cocoon.components.flow.javascript.fom.FOM_Cocoon;
import org.apache.cocoon.components.flow.javascript.fom.FOM_WebContinuation;
import org.apache.cocoon.environment.Request;

/**
 * Woody-flowscript integration helper class.
 * @version $Id: Forms.java,v 1.1 2004/03/09 10:34:13 reinhard Exp $
 */
public class Forms extends ScriptableObject {
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
