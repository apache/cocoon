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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.flow.javascript.fom;

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;

/**
 *
 * @version CVS $Id: FOM_WebContinuation.java,v 1.6 2004/02/20 18:48:23 sylvain Exp $
 */
public class FOM_WebContinuation extends ScriptableObject {

    WebContinuation wk;


    static class UserObject {
        boolean isBookmark;
        PageLocalScopeImpl pageLocal;
    }

    static private boolean isBookmark(WebContinuation wk) {
        UserObject userObj = (UserObject)wk.getUserObject();
        if (userObj == null) {
            return false;
        }
        return userObj.isBookmark;
    }

    public FOM_WebContinuation() {
    }


    public FOM_WebContinuation(WebContinuation wk) {
        this.wk = wk;
    }

    // new FOM_WebContinuation([Continuation] continuation,
    //                         [FOM_WebContinuation] parent,
    //                         [Number] timeToLive)
    public static Object jsConstructor(Context cx, Object[] args,
                                       Function ctorObj,
                                       boolean inNewExpr)
        throws Exception {
        FOM_WebContinuation result = null;
        if (args.length < 1) {
            // error
        }
        Continuation c = (Continuation)unwrap(args[0]);
        FOM_WebContinuation parent = null;
        if (args.length > 1) {
            parent = (FOM_WebContinuation)args[1];
        }
        int timeToLive = 0;
        if (args.length > 2) {
            timeToLive =
                (int)org.mozilla.javascript.Context.toNumber(args[2]);
        }
        WebContinuation wk;
        Scriptable scope = getTopLevelScope(c);
        FOM_Cocoon cocoon = (FOM_Cocoon)getProperty(scope, "cocoon");
        ServiceManager componentManager =  cocoon.getServiceManager();
        ContinuationsManager contMgr = (ContinuationsManager)
            componentManager.lookup(ContinuationsManager.ROLE);
        wk = contMgr.createWebContinuation(c,
                                           (parent == null ? null : parent.getWebContinuation()),
                                           timeToLive,
                                           null);
        result = new FOM_WebContinuation(wk);
        result.setParentScope(getTopLevelScope(scope));
        result.setPrototype(getClassPrototype(scope, result.getClassName()));
        return result;
    }

    public String getClassName() {
        return "FOM_WebContinuation";
    }

    public String jsGet_id() {
        return wk.getId();
    }


    public Continuation jsGet_continuation() {
        return (Continuation)wk.getContinuation();
    }

    public FOM_WebContinuation jsFunction_getParent() {
        WebContinuation parent = wk.getParentContinuation();
        if (parent == null) return null;
        FOM_WebContinuation pwk = new FOM_WebContinuation(parent);
        pwk.setParentScope(getParentScope());
        pwk.setPrototype(getClassPrototype(getParentScope(),
                                           pwk.getClassName()));
        return pwk;
    }

    public NativeArray jsFunction_getChildren() throws Exception {
        List list = wk.getChildren();
        NativeArray arr =
            (NativeArray)org.mozilla.javascript.Context.getCurrentContext().newObject(getParentScope(),
                                                                                      "Array",
                                                                                      new Object[]{new Integer(list.size())});
        Iterator iter = list.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            WebContinuation child = (WebContinuation)iter.next();
            FOM_WebContinuation cwk = new FOM_WebContinuation(child);
            cwk.setParentScope(getParentScope());
            cwk.setPrototype(getClassPrototype(getParentScope(),
                                               cwk.getClassName()));
            arr.put(i, arr, cwk);
        }
        return arr;
    }

    public void jsFunction_invalidate() throws Exception {
        ContinuationsManager contMgr = null;
        FOM_Cocoon cocoon =
            (FOM_Cocoon)getProperty(getTopLevelScope(this),
                                    "cocoon");
        ServiceManager componentManager =
            cocoon.getServiceManager();
        contMgr = (ContinuationsManager)
            componentManager.lookup(ContinuationsManager.ROLE);
        contMgr.invalidateWebContinuation(wk);
    }

    public void jsFunction_display() {
        wk.display();
    }

    public WebContinuation getWebContinuation() {
        return wk;
    }

    private static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }

    PageLocalScopeImpl getPageLocal() {
        UserObject userObj = (UserObject)wk.getUserObject();
        if (userObj == null) return null;
        return userObj.pageLocal;
    }

    void setPageLocal(PageLocalScopeImpl pageLocal) {
        UserObject userObj = (UserObject)wk.getUserObject();
        if (userObj == null) {
            userObj = new UserObject();
            wk.setUserObject(userObj);
        }
        userObj.pageLocal = pageLocal;
    }

    public void jsFunction_setBookmark(boolean value) {
        UserObject userObj = (UserObject)wk.getUserObject();
        if (userObj == null) {
            userObj = new UserObject();
            wk.setUserObject(userObj);
        }
        userObj.isBookmark = value;
    }

    public boolean jsGet_bookmark() {
        return isBookmark(wk);
    }

    public boolean jsFunction_isBookmark() {
        return isBookmark(wk);
    }

    public FOM_WebContinuation jsGet_previousBookmark() {
        WebContinuation c = wk.getParentContinuation();
        if (c == null) return null;
        // If this is a continuation of sendPageAndWait()
        // and the immediate parent is a bookmark, then
        // it is the bookmark for this page, so skip it.
        if (!isBookmark(wk) && isBookmark(c)) {
            c = c.getParentContinuation();
        }
        while (c != null && !isBookmark(c)) {
            c = c.getParentContinuation();
        }
        if (c == null) return null;
        FOM_WebContinuation pwk = new FOM_WebContinuation(c);
        pwk.setParentScope(getParentScope());
        pwk.setPrototype(getClassPrototype(getParentScope(),
                                           pwk.getClassName()));
        return pwk;

    }
}
