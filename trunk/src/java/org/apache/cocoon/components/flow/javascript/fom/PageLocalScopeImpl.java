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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version CVS $Id: PageLocalScopeImpl.java,v 1.1 2004/02/20 18:53:46 sylvain Exp $
 */
public class PageLocalScopeImpl implements PageLocalScope {

    private Map locals;
    private Scriptable scope;

    public PageLocalScopeImpl(Scriptable scope) {
        locals = new HashMap();
        this.scope = scope;
    }

    private Scriptable newObject() {
        try {
            return Context.getCurrentContext().newObject(scope);
        } catch (Exception ignored) {
            // can't happen here
            ignored.printStackTrace();
            throw new Error("error: " + ignored);
        }
    }

    private PageLocalScopeImpl(PageLocalScopeImpl toBeCloned) {
        this.scope = toBeCloned.scope;
        locals = new HashMap();
        Iterator iter = toBeCloned.locals.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry)iter.next();
            Object key = e.getKey();
            Object value = e.getValue();
            // clone it
            Scriptable obj = (Scriptable)value;
            Scriptable newObj = newObject();
            Object[] ids = obj.getIds();
            for (int i = 0; i < ids.length; i++) {
                String name = ids[i].toString();
                newObj.put(name, newObj, obj.get(name, obj));
            }
            value = newObj;
            locals.put(key, value);
        }
   }

    private Scriptable resolve(PageLocal local) {
        final Object id = local.getId();
        Scriptable result = (Scriptable)locals.get(id);
        if (result == null) {
            locals.put(id, result = newObject());
        }
        return result;
    }

    public boolean has(PageLocal local, String name) {
        Scriptable obj = resolve(local);
        return obj.has(name, obj);
    }

    public boolean has(PageLocal local, int index) {
        Scriptable obj = resolve(local);
        return obj.has(index, obj);
    }

    public Object get(PageLocal local, String name) {
        Scriptable obj = resolve(local);
        return obj.get(name, obj);
    }

    public Object get(PageLocal local, int index) {
        Scriptable obj = resolve(local);
        return obj.get(index, obj);
    }

    public void put(PageLocal local, String name, Object value) {
        Scriptable obj = resolve(local);
        obj.put(name, obj, value);
    }

    public void put(PageLocal local, int index, Object value) {
        Scriptable obj = resolve(local);
        obj.put(index, obj, value);
    }

    public void delete(PageLocal local, String name) {
        Scriptable obj = resolve(local);
        obj.delete(name);
    }

    public void delete(PageLocal local, int index) {
        Scriptable obj = resolve(local);
        obj.delete(index);
    }

    public Object[] getIds(PageLocal local) {
        Scriptable obj = resolve(local);
        return obj.getIds();
    }

    public Object getDefaultValue(PageLocal local, Class hint) {
        Scriptable obj = resolve(local);
        return obj.getDefaultValue(hint);
    }

    public PageLocalScopeImpl duplicate() {
        return new PageLocalScopeImpl(this);
    }

    public PageLocal createPageLocal() {
        // not used
        return null;
    }
}
