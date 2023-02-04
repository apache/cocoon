/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.commons.logging.Log;
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
 * @version $Id$
 */
public class FOM_WebContinuation extends ScriptableObject {

    WebContinuation wk;

    private Log logger;

    static class UserObject {
        boolean isBookmark;
        PageLocalScopeImpl pageLocal;
    }

    static private boolean isBookmark(WebContinuation wk) {
        UserObject userObj = (UserObject) wk.getUserObject();
        return userObj != null && userObj.isBookmark;
    }


    public FOM_WebContinuation() {
        this(null);
    }

    public FOM_WebContinuation(WebContinuation wk) {
        this.wk = wk;
    }

    public void setLogger(Log logger) {
        this.logger = logger;
    }
    
    // new FOM_WebContinuation([Continuation] continuation,
    //                         [FOM_WebContinuation] parent,
    //                         [Number] timeToLive)
    public static Object jsConstructor(Context cx, Object[] args,
                                       Function ctorObj,
                                       boolean inNewExpr)
    throws Exception {
        if (args.length < 1) {
            // error
        }
        Continuation c = (Continuation) unwrap(args[0]);
        FOM_WebContinuation parent = null;
        if (args.length > 1) {
            parent = (FOM_WebContinuation) args[1];
        }
        int timeToLive = 0;
        if (args.length > 2) {
            timeToLive =
                    (int) org.mozilla.javascript.Context.toNumber(args[2]);
        }

        WebContinuation wk;
        Scriptable scope = getTopLevelScope(c);
        FOM_Cocoon cocoon = (FOM_Cocoon)getProperty(scope, "cocoon");
        ContinuationsManager contMgr = (ContinuationsManager)cocoon.getApplicationContext().getBean(ContinuationsManager.ROLE);
        wk = contMgr.createWebContinuation(c,
                                           (parent == null ? null : parent.getWebContinuation()),
                                           timeToLive,
                                           cocoon.getInterpreterId(), 
                                           null);

        FOM_WebContinuation result;
        result = new FOM_WebContinuation(wk);
        result.setLogger(cocoon.getLogger());
        result.setParentScope(getTopLevelScope(scope));
        result.setPrototype(getClassPrototype(scope, result.getClassName()));
        return result;
    }

    public String getClassName() {
        return "FOM_WebContinuation";
    }

    public Object jsFunction_getAttribute(String name) {
        return org.mozilla.javascript.Context.javaToJS(
                wk.getAttribute(name),
                getParentScope());
    }

    public void jsFunction_setAttribute(String name, Object value) {
        wk.setAttribute(name, unwrap(value));
    }

    public void jsFunction_removeAttribute(String name) {
        wk.removeAttribute(name);
    }

    public Object jsFunction_getAttributeNames() {
        return org.mozilla.javascript.Context.javaToJS(
                wk.getAttributeNames(),
                getParentScope());
    }

    public String jsGet_id() {
        return wk.getId();
    }


    public Continuation jsGet_continuation() {
        return (Continuation)wk.getContinuation();
    }

    public FOM_WebContinuation jsFunction_getParent() {
        WebContinuation parent = wk.getParentContinuation();
        if (parent == null) {
            return null;
        }

        FOM_WebContinuation pwk = new FOM_WebContinuation(parent);
        pwk.setLogger(logger);
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
            cwk.setLogger(logger);
            cwk.setParentScope(getParentScope());
            cwk.setPrototype(getClassPrototype(getParentScope(),
                                               cwk.getClassName()));
            arr.put(i, arr, cwk);
        }
        return arr;
    }

    public void jsFunction_invalidate() throws Exception {
        FOM_Cocoon cocoon =
            (FOM_Cocoon)getProperty(getTopLevelScope(this), "cocoon");
        ContinuationsManager contMgr = (ContinuationsManager)cocoon.getApplicationContext().getBean(ContinuationsManager.ROLE);
        contMgr.invalidateWebContinuation(wk);
    }

    public void jsFunction_display() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(wk.toString());
        }
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
        if (c == null) {
            return null;
        }

        // If this is a continuation of sendPageAndWait()
        // and the immediate parent is a bookmark, then
        // it is the bookmark for this page, so skip it.
        if (!isBookmark(wk) && isBookmark(c)) {
            c = c.getParentContinuation();
        }
        while (c != null && !isBookmark(c)) {
            c = c.getParentContinuation();
        }
        if (c == null) {
            return null;
        }

        FOM_WebContinuation pwk = new FOM_WebContinuation(c);
        pwk.setLogger(logger);
        pwk.setParentScope(getParentScope());
        pwk.setPrototype(getClassPrototype(getParentScope(), pwk.getClassName()));
        return pwk;
    }

    /**
     * Return text representation of the WebContinuation.
     */
    public String toString() {
        return "WC" + wk.getId();
    }

}
