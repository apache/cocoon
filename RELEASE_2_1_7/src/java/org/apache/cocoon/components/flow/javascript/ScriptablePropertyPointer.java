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
 * @version CVS $Id: ScriptablePropertyPointer.java,v 1.5 2004/03/05 13:02:46 bdelacretaz Exp $
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
                if (property != Scriptable.NOT_FOUND) { 
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
