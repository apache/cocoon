/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.components.flow.javascript;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

/**
 *
 * @version CVS $Id: JSWebContinuation.java,v 1.4 2004/01/21 14:31:25 vgritsenko Exp $
 */
public class JSWebContinuation extends ScriptableObject {
    protected JSCocoon cocoon;
    protected WebContinuation wk;
    protected ContinuationsManager continuationsMgr;

    public JSWebContinuation() {
    }

    public String getClassName() {
        return "WebContinuation";
    }

    public JSCocoon getJSCocoon() {
        return cocoon;
    }

    public WebContinuation getWebContinuation() {
        return wk;
    }

    /**
     * @param args Arguments: JSCocoon cocoon, Object continuation, JSWebContinuation parent, Number timeToLive
     */
    public static Scriptable jsConstructor(Context cx, Object[] args,
                                           Function ctorObj,
                                           boolean inNewExpr)
            throws Exception {
        JSCocoon cocoon = (JSCocoon) args[0];
        ComponentManager manager = cocoon.getComponentManager();

        ContinuationsManager contMgr
                = (ContinuationsManager) manager.lookup(ContinuationsManager.ROLE);

        Object kont = args[1];
        JSWebContinuation pjswk = (JSWebContinuation) args[2];
        WebContinuation pwk = (pjswk == null ? null : pjswk.wk);

        int ttl;

        if (args[3] == Undefined.instance) {
            ttl = 0;
        } else {
            Number timeToLive = (Number) args[3];
            ttl = (timeToLive == null ? 0 : timeToLive.intValue());
        }

        JSWebContinuation jswk = new JSWebContinuation();
        WebContinuation wk
                = contMgr.createWebContinuation(kont, pwk, ttl, null);
        wk.setUserObject(jswk);

        jswk.cocoon = cocoon;
        jswk.wk = wk;
        jswk.continuationsMgr = contMgr;

        return jswk;
    }

    public String jsGet_id() {
        return wk.getId();
    }

    public Object jsGet_continuation() {
        return wk.getContinuation();
    }

    public void jsFunction_invalidate() {
        continuationsMgr.invalidateWebContinuation(wk);
    }

    public void jsFunction_display() {
        wk.display();
    }
}
