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

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPointer;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 *
 * @version CVS $Id: ScriptablePointer.java,v 1.4 2003/05/04 20:24:47 cziegeler Exp $
 */
public class ScriptablePointer extends DynamicPointer {

    Scriptable node;

    final static ScriptablePropertyHandler handler = 
        new ScriptablePropertyHandler();

    public ScriptablePointer(NodePointer parent,
                             QName name,
                             Scriptable object) {
        super(parent, name, object, handler);
        node = object;
    }

    public ScriptablePointer(QName name,
                             Scriptable object,
                             Locale locale) {
        super(name, object, handler, locale);
        node = object;
    }

    public PropertyPointer getPropertyPointer(){
        return new ScriptablePropertyPointer(this, handler);
    }

    public int getLength() {
        Object obj = getBaseValue();
        if (obj instanceof Scriptable) {
            Scriptable node = (Scriptable)obj;
            if (node instanceof NativeArray) {
                return (int)((NativeArray)node).jsGet_length();
            }
            if (ScriptableObject.hasProperty(node, "length")) {
                Object val = ScriptableObject.getProperty(node, "length");
                if (val instanceof Number) {
                    return ((Number)val).intValue();
                }
            }
        }
        return super.getLength();
    }

    public Object getImmediateNode() {
        Object value;
        if (index == WHOLE_COLLECTION) {
            value = node;
        } else {
            value = ScriptableObject.getProperty(node, index);
            if (value == ScriptableObject.NOT_FOUND) {
                value = node; // hack: same behavior as ValueUtils.getValue()
            } 
        }
        if (value instanceof Wrapper) {
            value = ((Wrapper)value).unwrap();
        }
        return value;
    }

    public void setValue(Object value){
        if (getParent() != null) {
            getParent().setValue(value);
        }
    }

}
