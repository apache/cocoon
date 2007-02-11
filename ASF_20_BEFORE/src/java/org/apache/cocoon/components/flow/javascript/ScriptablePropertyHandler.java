/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 *
 * @version CVS $Id: ScriptablePropertyHandler.java,v 1.6 2004/03/01 03:50:58 antonio Exp $
 */
public class ScriptablePropertyHandler implements DynamicPropertyHandler {

    public Object getProperty(Object obj, String propertyName) {
        Context cx = null;
        try {
            cx = Context.enter();
            Scriptable s = (Scriptable)obj;
            Object result = ScriptableObject.getProperty(s, propertyName);
            if (result == Scriptable.NOT_FOUND) {
                result = ScriptableObject.getProperty(s, "get" + propertyName.substring(0, 1).toUpperCase() + (propertyName.length() > 1 ? propertyName.substring(1) : ""));
                if (result != Scriptable.NOT_FOUND &&
                    result instanceof Function) {
                    try {
                        result = ((Function)result).call(cx, 
                                                         ScriptableObject.getTopLevelScope(s), s, new Object[] {});
                    } catch (JavaScriptException exc) {
                        exc.printStackTrace();
                        result = Undefined.instance;
                    }
                } 
                if (result == Undefined.instance ||
                    result == Scriptable.NOT_FOUND) {
                    result = null;
                }
            } else if (result instanceof Wrapper) {
                result = ((Wrapper)result).unwrap();
            } else if (result == Undefined.instance) {
                result = null;
            }
            return result;
        } finally {
            Context.exit();
        }
    }
    
    public String[] getPropertyNames(Object obj) {
        Context.enter();
        try {
            Object[] ids;
            if (obj instanceof ScriptableObject) {
                ids = ((ScriptableObject)obj).getAllIds();
            } else {
                ids = ((Scriptable)obj).getIds();
            }
            String[] result = new String[ids.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = (String)ids[i];
            }
            return result;
        } finally {
            Context.exit();
        }
    }
    
    public void setProperty(Object obj, String propertyName,
                            Object value) {
        Context.enter();
        try {
            if (!(value == null
                  || value instanceof String 
                  || value instanceof Number 
                  || value instanceof Boolean)) {
                value = Context.toObject(value, 
                                         (Scriptable)obj);
            }
            ScriptableObject.putProperty((Scriptable)obj,
                                         propertyName, value);
        } finally {
            Context.exit();
        }
    }
}
