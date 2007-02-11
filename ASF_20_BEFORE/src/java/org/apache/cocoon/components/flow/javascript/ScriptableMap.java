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

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

import java.util.Map;

/**
 * Wrap a java.util.Map for JavaScript.
 *
 * @version CVS $Id: ScriptableMap.java,v 1.2 2003/03/16 17:49:12 vgritsenko Exp $
 */
public class ScriptableMap implements Scriptable, Wrapper {

    private Map map;
    private Scriptable prototype, parent;

    public ScriptableMap() {
    }

    public ScriptableMap(Map map) {
        this.map = map;
    }

    public String getClassName() {
        return "Map";
    }

    public boolean has(String name, Scriptable start) {
        return this.map.containsKey(name);
    }

    /**
     * no numeric properties
     */
    public boolean has(int index, Scriptable start) {
        return false;
    }

    public Object get(String name, Scriptable start) {
        if (this.map.containsKey(name))
            return this.map.get(name);

        return NOT_FOUND;
    }

    public Object get(int index, Scriptable start) {
        return NOT_FOUND;
    }

    public void put(String name, Scriptable start, Object value) {
        if (value instanceof NativeJavaObject) {
            value = ((NativeJavaObject)value).unwrap();
        }
        map.put(name, value);
    }

    public void put(int index, Scriptable start, Object value) {
    }

    public void delete(String id) {
        map.remove(id);
    }

    public void delete(int index) {
    }

    public Scriptable getPrototype() {
        return prototype;
    }

    public void setPrototype(Scriptable prototype) {
        this.prototype = prototype;
    }

    public Scriptable getParentScope() {
        return parent;
    }

    public void setParentScope(Scriptable parent) {
        this.parent = parent;
    }

    public Object[] getIds() {
        return this.map.keySet().toArray();
    }

    public Object getDefaultValue(Class typeHint) {
        return this.map.toString();
    }

    public boolean hasInstance(Scriptable value) {
        Scriptable proto = value.getPrototype();
        while (proto != null) {
            if (proto.equals(this)) 
                return true;
            proto = proto.getPrototype();
        }

        return false;
    }

    /**
     * Return the java.util.Map that is wrapped by this class.
     */
    public Object unwrap() {
        return this.map;
    }

}
