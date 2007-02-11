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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertyPointer;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 *
 * @version CVS $Id: ScriptablePropertyPointer.java,v 1.3 2003/05/01 09:32:34 coliver Exp $
 */
public class ScriptablePropertyPointer extends DynamicPropertyPointer {

    DynamicPropertyHandler handler;

    public ScriptablePropertyPointer(NodePointer parent,
                                     DynamicPropertyHandler handler) {
        super(parent, handler);
        this.handler = handler;
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
            value = getBaseValue();
        }
        else {
            value = getBaseValue();
            if (value instanceof Scriptable) {
                Object property = 
                    ScriptableObject.getProperty((Scriptable)value, index);
                if (property != ScriptableObject.NOT_FOUND) { 
                    value = property; // hack?
                } 
            } else {
                return super.getImmediateNode();
            }
        }
        if (value instanceof Wrapper) {
            value = ((Wrapper)value).unwrap();
        }
        return value;
    }

    public Object getValue() {
        Object val = getNode();
        if (val instanceof NativeArray) {
            NativeArray arr = (NativeArray)val;
            List list = new LinkedList();
            int len = (int)arr.jsGet_length();
            for (int i = 0; i < len; i++) {
                Object obj = arr.get(i, arr);
                if (obj == Undefined.instance) {
                    obj = null;
                }
                list.add(obj);
            }
            return list;
        }
        return super.getValue();
    }

    public void setValue(Object value){
        if (index == WHOLE_COLLECTION){
            handler.setProperty(getBean(), getPropertyName(), value);
        } else {
            Object val = handler.getProperty(getBean(), getPropertyName());
            if (val instanceof Scriptable) {
                ScriptableObject.putProperty((Scriptable)val, index, value);
            } else {
                super.setValue(value);
            }
        }
    }

    public void remove(){
        if (index == WHOLE_COLLECTION){
            handler.setProperty(getBean(), "length", new Integer(0));
        } else {
            Object val = handler.getProperty(getBean(), getPropertyName());
            if (val instanceof Scriptable) {
                try {
                    ScriptableObject.deleteProperty((Scriptable)val, index);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                super.remove();
            }
        }
    }

    public boolean isCollection() {
        Object obj = getBaseValue();
        if (obj instanceof NativeArray) {
            return true;
        }
        return super.isCollection();
    }

    public String asPath(){
        Object obj = getBaseValue();
        if (!(obj instanceof Scriptable)) {
            return super.asPath();
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(getParent().asPath());
        if (buffer.length() == 0){
            buffer.append("/.");
        }
        else if (buffer.charAt(buffer.length() - 1) == '/'){
            buffer.append('.');
        }
        buffer.append("[@name = '");
        buffer.append(escape(getPropertyName()));
        buffer.append("']");
        if (index != WHOLE_COLLECTION && (obj instanceof NativeArray)) {
            buffer.append('[').append(index+1).append(']');
        }
        return buffer.toString();
    }

    private String escape(String string){
        int index = string.indexOf('\'');
        while (index != -1){
            string = string.substring(0, index) + "&apos;" + string.substring(index + 1);
            index = string.indexOf('\'');
        }
        index = string.indexOf('\"');
        while (index != -1){
            string = string.substring(0, index) + "&quot;" + string.substring(index + 1);
            index = string.indexOf('\"');
        }
        return string;
    }

}
