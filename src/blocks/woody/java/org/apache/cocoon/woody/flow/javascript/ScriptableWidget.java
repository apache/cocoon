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

package org.apache.cocoon.woody.flow.javascript;
import org.apache.cocoon.woody.formmodel.AggregateField;
import org.apache.cocoon.woody.formmodel.BooleanField;
import org.apache.cocoon.woody.formmodel.Field;
import org.apache.cocoon.woody.formmodel.MultiValueField;
import org.apache.cocoon.woody.formmodel.Output;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.cocoon.woody.formmodel.Widget;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 * @version $Id: ScriptableWidget.java,v 1.6 2004/02/11 10:43:32 antonio Exp $
 * 
 */
public class ScriptableWidget extends ScriptableObject {

    Widget delegate;

    public String getClassName() {
        return "Widget";
    }

    public ScriptableWidget() {
    }

    public ScriptableWidget(Object widget) {
        this.delegate = (Widget)unwrap(widget);
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

    private ScriptableWidget wrap(Widget w) {
        ScriptableWidget result = new ScriptableWidget(w);
        result.setPrototype(getClassPrototype(this, "Widget"));
        result.setParentScope(getParentScope());
        return result;
    }

    public boolean has(String id, Scriptable start) {
        if (delegate instanceof Repeater) {
            if (id.equals("length")) {
                return true;
            }
        } else if (delegate instanceof MultiValueField) {
            if (id.equals("length")) {
                return true;
            }
        } else if (delegate != null) {
            Widget sub = delegate.getWidget(id);
            if (sub != null) {
                return true;
            }
        }
        return super.has(id, start);
    }

    public boolean has(int index, Scriptable start) {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            return index >= 0 && index < repeater.getSize();
        }
        if (delegate instanceof MultiValueField) {
            Object[] values = (Object[])delegate.getValue();
            return index >= 0 && index < values.length;
        }
        return super.has(index, start);
    }

    public Object get(String id, Scriptable start) {
        if (delegate instanceof Repeater) {
            if (id.equals("length")) {
                Repeater repeater = (Repeater)delegate;
                return new Integer(repeater.getSize());
            }
        } else if (delegate instanceof MultiValueField) {
            if (id.equals("length")) {
                Object[] values = (Object[])delegate.getValue();
                return new Integer(values.length);
            }
        } else if (delegate != null) {
            Widget sub = delegate.getWidget(id);
            if (sub != null) {
                if (sub instanceof Field ||
                    sub instanceof BooleanField ||
                    sub instanceof AggregateField ||
                    sub instanceof Output) {
                    return sub.getValue();
                }
                return wrap(sub);
            }
        }
        return super.get(id, start);
    }

    public Object get(int index, Scriptable start) {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            if (index >= 0) {
                while (index >= repeater.getSize()) {
                    repeater.addRow();
                }
                return wrap(repeater.getRow(index));
            }
        }
        if (delegate instanceof MultiValueField) {
            Object[] values = (Object[])delegate.getValue();
            if (index >= 0 && index < values.length) {
                return values[index];
            } else {
	      return NOT_FOUND;
	    }
        }
        return super.get(index, start);
    }

    public void delete(String id) {
        super.delete(id);
    }

    public void delete(int index) {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            if (index >= 0 && index < repeater.getSize()) {
                repeater.removeRow(index);
                return;
            }
        } else if (delegate instanceof MultiValueField) {
            MultiValueField field = (MultiValueField)delegate;
            Object[] values = (Object[])field.getValue();
            if (values != null && values.length > index) {
                Object[] newValues = new Object[values.length-1];
                int i;
                for (i = 0; i < index; i++) {
                    newValues[i] = values[i];
                }
                i++;
                for (;i < values.length;i++) {
                    newValues[i-1] = values[i];
                }
                field.setValues(newValues);
            }
            return;
        }
        super.delete(index);
    }

    public void put(String id, Scriptable start, Object value) {
        if (delegate instanceof Repeater) {
            if (id.equals("length")) {
                int len = (int)Context.toNumber(value);
                Repeater repeater = (Repeater)delegate;
                int size = repeater.getSize();
                if (size > len) {
                    while (repeater.getSize() > len) {
                        repeater.removeRow(repeater.getSize() -1);
                    }
                } else {
                    for (int i = size; i < len; ++i) {
                        repeater.addRow();
                    }
                }
            }
        } else if (delegate != null) {
            Widget sub = delegate.getWidget(id);
            if (sub instanceof Field) {
                Field field = (Field)sub;
                value = unwrap(value);
                if (value instanceof Double) {
                    // make woody accept a JS Number
                    Class typeClass =
                        field.getFieldDefinition().getDatatype().getTypeClass();
                    if (typeClass == long.class || typeClass == Long.class) {
                        value = new Long(((Number)value).longValue());
                    } else if (typeClass == int.class || typeClass == Integer.class) {
                        value = new Integer(((Number)value).intValue());
                    } else if (typeClass == float.class || typeClass == Float.class) {
                        value = new Float(((Number)value).floatValue());
                    } else if (typeClass == short.class || typeClass == Short.class) {
                        value = new Short(((Number)value).shortValue());
                    }
                }
                field.setValue(value);
                return;
            } else if (sub instanceof BooleanField) {
                BooleanField field = (BooleanField)sub;
                value = unwrap(value);
                field.setValue(value);
            } else if (sub instanceof Output) {
                Output field = (Output)sub;
                value = unwrap(value);
                field.setValue(value);
            } else if (sub instanceof Repeater) {
                Repeater repeater = (Repeater)sub;
                if (value instanceof NativeArray) {
                    NativeArray arr = (NativeArray)value;
                    Object length = getProperty(arr, "length");
                    int len = ((Number)length).intValue();
                    for (int i = repeater.getSize(); i >= len; --i) {
                        repeater.removeRow(i);
                    }
                    for (int i = 0; i < len; i++) {
                        Object elemValue = getProperty(arr, i);
                        if (elemValue instanceof Scriptable) {
                            Scriptable s = (Scriptable)elemValue;
                            Object[] ids = s.getIds();
                            ScriptableWidget wid = wrap(repeater.getRow(i));
                            for (int j = 0; j < ids.length; j++) {
                                String idStr = ids[j].toString();
                                wid.put(idStr, wid, getProperty(s, idStr));
                            }
                        }
                    }
                    return;
                }
            } else if (sub instanceof MultiValueField) {
                MultiValueField field = (MultiValueField)sub;
                Object[] values = null;
                if (value instanceof NativeArray) {
                    NativeArray arr = (NativeArray)value;
                    Object length = getProperty(arr, "length");
                    int len = ((Number)length).intValue();
                    values = new Object[len];
                    for (int i = 0; i < len; i++) {
                        Object elemValue = getProperty(arr, i);
                        values[i] = unwrap(elemValue);
                    }
                } else if (value instanceof Object[]) {
                    values = (Object[])value;
                }
                field.setValues(values);
            } else {
                if (value instanceof Scriptable) {
                    Scriptable s = (Scriptable)value;
                    Object[] ids = s.getIds();
                    ScriptableWidget wid = wrap(sub);
                    for (int j = 0; j < ids.length; j++) {
                        String idStr = ids[j].toString();
                        wid.put(idStr, wid, getProperty(s, idStr));
                    }
                    return;
                }
            }
        }
        super.put(id, start, value);
    }

    public String jsGet_id() {
        return delegate.getId();
    }

    public Scriptable jsGet_parent() {
        if (delegate != null) {
            return wrap(delegate.getParent());
        }
        return Undefined.instance;
    }

    public boolean jsFunction_equals(Object other) {
        if (other instanceof ScriptableWidget) {
            ScriptableWidget otherWidget = (ScriptableWidget)other;
            return delegate.equals(otherWidget.delegate);
        }
        return false;
    }

    public void jsFunction_remove(int index) {
        delete(index);
    }

}
