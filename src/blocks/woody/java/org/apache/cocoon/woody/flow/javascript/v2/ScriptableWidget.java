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

package org.apache.cocoon.woody.flow.javascript.v2;
import org.apache.cocoon.woody.formmodel.Action;
import org.apache.cocoon.woody.formmodel.AggregateField;
import org.apache.cocoon.woody.formmodel.BooleanField;
import org.apache.cocoon.woody.formmodel.Field;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.woody.formmodel.ContainerWidget;
import org.apache.cocoon.woody.formmodel.MultiValueField;
import org.apache.cocoon.woody.formmodel.Output;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.cocoon.woody.formmodel.Submit;
import org.apache.cocoon.woody.formmodel.Upload;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.datatype.SelectionList;
import org.apache.cocoon.woody.event.FormHandler;
import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

public class ScriptableWidget extends ScriptableObject {

    Widget delegate;
    ScriptableWidget formWidget;

    class ScriptableFormHandler implements FormHandler {
        public void handleEvent(WidgetEvent widgetEvent) {
            Widget src = widgetEvent.getSourceWidget();
            ScriptableWidget w = wrap(src);
            w.handleEvent(widgetEvent);
        }
    }

    public String getClassName() {
        return "Widget";
    }

    public ScriptableWidget() {
    }

    public ScriptableWidget(Object widget) {
        this.delegate = (Widget)unwrap(widget);
        if (delegate instanceof Form) {
            Form form = (Form)delegate;
            form.setFormHandler(new ScriptableFormHandler());
            formWidget = this;
            Map widgetMap = new HashMap();
            widgetMap.put(delegate, this);
            put("__widgets__", this, widgetMap);
        }
    }

    static private Object unwrap(Object obj) {
        if (obj == Undefined.instance) {
            return null;
        }
        if (obj instanceof Wrapper) {
            return ((Wrapper)obj).unwrap();
        }
        return obj;
    }

    private void deleteWrapper(Widget w) {
        if (delegate instanceof Form) {
            Map widgetMap = (Map)super.get("__widgets__", this);
            widgetMap.remove(w);
        }
    }

    private ScriptableWidget wrap(Widget w) {
        if (w == null) return null;
        if (delegate instanceof Form) {
            Map widgetMap = (Map)super.get("__widgets__", this);
            ScriptableWidget result = null;
            result = (ScriptableWidget)widgetMap.get(w);
            if (result == null) {
                result = new ScriptableWidget(w);
                result.formWidget = this;
                result.setPrototype(getClassPrototype(this, "Widget"));
                result.setParentScope(getParentScope());
                widgetMap.put(w, result);
            }
            return result;
        } else {
            return formWidget.wrap(w);
        }
    }

    public boolean has(String id, Scriptable start) {
        if (delegate != null) {
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
        if (delegate != null && !(delegate instanceof Repeater)) {
            Widget sub = delegate.getWidget(id);
            if (sub != null) {
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

    public Object[] getAllIds() {
        Object[] result = super.getAllIds();
        return addWidgetIds(result);
    }

    public Object[] getIds() {
        Object[] result = super.getIds();
        return addWidgetIds(result);
    }

    private Object[] addWidgetIds(Object[] result) {
        if (delegate instanceof ContainerWidget) {
            Iterator iter = ((ContainerWidget)delegate).getChildren();
            List list = new LinkedList();
            for (int i = 0; i < result.length; i++) {
                list.add(result[i]);
            }
            while (iter.hasNext()) {
                Widget widget = (Widget)iter.next();
                list.add(widget.getId());
            }
            result = list.toArray();
        }
        return result;
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

    public Object jsGet_value() {
        return delegate.getValue();
    }

    public Object jsFunction_getValue() {
        return jsGet_value();
    }

    public void jsFunction_setValue(Object value) throws JavaScriptException {
        jsSet_value(value);
    }

    public void jsSet_length(int len) {
        if (delegate instanceof Repeater) {
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
    }

    public Integer jsGet_length() {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            return new Integer(repeater.getSize());
        }
        return null;
    }

    public void jsSet_value(Object value) throws JavaScriptException {
        if (delegate instanceof Field || delegate instanceof Output) {
            // fix me: Unify Field and Output with a "DataTypeWidget" interface
            value = unwrap(value);
            if (value != null) {
                Datatype datatype;
                if (delegate instanceof Field) {
                    datatype = ((Field)delegate).getFieldDefinition().getDatatype();
                } else {
                    datatype = ((Output)delegate).getOutputDefinition().getDatatype();
                }
                Class typeClass = datatype.getTypeClass();
                if (typeClass == String.class) {
                    value = Context.toString(value);
                } else if (typeClass == boolean.class || 
                           typeClass == Boolean.class) {
                    value = Context.toBoolean(value) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    if (value instanceof Double) {
                        // make woody accept a JS Number
                        if (typeClass == long.class || typeClass == Long.class) {
                            value = new Long(((Number)value).longValue());
                        } else if (typeClass == int.class || 
                                   typeClass == Integer.class) {
                            value = new Integer(((Number)value).intValue());
                        } else if (typeClass == float.class || 
                                   typeClass == Float.class) {
                            value = new Float(((Number)value).floatValue());
                        } else if (typeClass == short.class || 
                                   typeClass == Short.class) {
                            value = new Short(((Number)value).shortValue());
                        } else if (typeClass == BigDecimal.class) {
                            value = new BigDecimal(((Number)value).doubleValue());
                        }
                    } 
                }
            }
            delegate.setValue(value);
        } else if (delegate instanceof BooleanField) {
            BooleanField field = (BooleanField)delegate;
            field.setValue(new Boolean(Context.toBoolean(value)));
        } else if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            if (value instanceof NativeArray) {
                NativeArray arr = (NativeArray)value;
                Object length = getProperty(arr, "length");
                int len = ((Number)length).intValue();
                for (int i = repeater.getSize(); i >= len; --i) {
                    repeater.removeRow(i);
                }
                for (int i = 0; i < len; i++) {
                    Object elemValue = getProperty(arr, i);
                    ScriptableWidget wid = wrap(repeater.getRow(i));
                    wid.jsSet_value(elemValue);
                }
            }
        } else if (delegate instanceof AggregateField) {
            AggregateField aggregateField = (AggregateField)delegate;
            if (value instanceof Scriptable) {
                Scriptable obj = (Scriptable)value;
                Object[] ids = obj.getIds();
                for (int i = 0; i < ids.length; i++) {
                    String id = String.valueOf(ids[i]);
                    Object val = getProperty(obj, id);
                    ScriptableWidget wid = wrap(aggregateField.getWidget(id));
                    if (wid == null) {
                        throw new JavaScriptException("No field \"" + id + "\" in widget \"" + aggregateField.getId() + "\"");
                    }
                    if (wid.delegate instanceof Field || 
                        wid.delegate instanceof BooleanField ||
                        wid.delegate instanceof Output) {
                        if (val instanceof Scriptable) {
                            Scriptable s = (Scriptable)val;
                            if (s.has("value", s)) {
                                wid.jsSet_value(s.get("value", s));
                            }
                        }
                    } else {
                        wid.jsSet_value(val);
                    }
                }
            }
        } else if (delegate instanceof Repeater.RepeaterRow) {
            Repeater.RepeaterRow row = (Repeater.RepeaterRow)delegate;
            if (value instanceof Scriptable) {
                Scriptable obj = (Scriptable)value;
                Object[] ids = obj.getIds();
                for (int i = 0; i < ids.length; i++) {
                    String id = String.valueOf(ids[i]);
                    Object val = getProperty(obj, id);
                    ScriptableWidget wid = wrap(row.getWidget(id));
                    if (wid == null) {
                        throw new JavaScriptException("No field \"" + id + "\" in row " + i + " of repeater \"" + row.getParent().getId() + "\"");
                    }
                    if (wid.delegate instanceof Field || 
                        wid.delegate instanceof BooleanField ||
                        wid.delegate instanceof Output) {
                        if (val instanceof Scriptable) {
                            Scriptable s = (Scriptable)val;
                            if (s.has("value", s)) {
                                wid.jsSet_value(s.get("value", s));
                            }
                        }
                    } else {
                        wid.jsSet_value(val);
                    }
                }
            } else {
                throw new JavaScriptException("Expected an object instead of: " + Context.toString(value));
            }
        } else if (delegate instanceof MultiValueField) {
            MultiValueField field = (MultiValueField)delegate;
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
            System.out.println("setting value of " + delegate.getClass());
            delegate.setValue(value);
        }
    }

    public String jsFunction_getId() {
        return delegate.getId();
    }

    public ScriptableWidget jsFunction_getSubmitWidget() {
        return wrap(delegate.getForm().getSubmitWidget());
    }

    public String jsFunction_getFullyQualifiedId() {
        return delegate.getFullyQualifiedId();
    }

    public String jsFunction_getNamespace() {
        return delegate.getNamespace();
    }

    public Scriptable jsFunction_getParent() {
        if (delegate != null) {
            return wrap(delegate.getParent());
        }
        return Undefined.instance;
    }

    public boolean jsFunction_isRequired() {
        return delegate.isRequired();
    }
    
    public ScriptableWidget jsFunction_getForm() {
        return formWidget;
    }
    
    public boolean jsFunction_equals(Object other) {
        if (other instanceof ScriptableWidget) {
            ScriptableWidget otherWidget = (ScriptableWidget)other;
            return delegate.equals(otherWidget.delegate);
        }
        return false;
    }

    public ScriptableWidget jsFunction_getWidget(String id) {
        Widget sub = delegate.getWidget(id);
        return wrap(sub);
    }

    public void jsFunction_setValidationError(String message,
                                              boolean i18n,
                                              Object parameters) {
        if (delegate instanceof Field || 
            delegate instanceof Upload) {
            String[] parms = null;
            if (parameters != null && parameters != Undefined.instance) {
                Scriptable obj = Context.toObject(parameters, this);
                int len = (int)
                    Context.toNumber(getProperty(obj, "length"));
                if (len > 0) {
                    parms = new String[len];
                    for (int i = 0; i < len; i++) {
                        parms[i] = Context.toString(getProperty(obj, i));
                    }
                }
            }
            ValidationError validationError = null;
            if (message != null) {
                if (parms != null) {
                    validationError = 
                        new ValidationError(message, parms);
                } else {
                    validationError = 
                        new ValidationError(message, i18n);
                }
            }
            // fix me: unify Upload and Field into one interface:
            // Validatable or whatever
            if (delegate instanceof Upload) {
                ((Upload)delegate).setValidationError(validationError);
            } else {
                ((Field)delegate).setValidationError(validationError);
            }
        }
    }

    public Widget jsFunction_unwrap() {
        return delegate;
    }

    public ScriptableWidget jsFunction_addRow() {
        if (delegate instanceof Repeater) {
            return wrap(((Repeater)delegate).addRow());
        }
        return null;
    }

    public ScriptableObject jsFunction_getRow(int index) {
        if (delegate instanceof Repeater) {
            return wrap(((Repeater)delegate).getRow(index));
        }
        return null;
    }

    public void jsFunction_removeRow(Object obj) throws JavaScriptException {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            if (obj instanceof Function) {
                Function fun = (Function)obj;
                int len = repeater.getSize();
                boolean[] index = new boolean[len];
                Object[] args = new Object[1];
                Scriptable scope = getTopLevelScope(this);
                Scriptable thisObj = scope;
                Context cx = Context.getCurrentContext();
                for (int i = 0; i < len; i++) {
                    ScriptableWidget row = wrap(repeater.getRow(i));
                    args[0] = row;
                    Object result = fun.call(cx, scope, thisObj, args);
                    index[i] = Context.toBoolean(result);
                }    
                for (int i = len-1; i >= 0; --i) {
                    if (index[i]) {
                        Widget row = repeater.getRow(i);
                        formWidget.deleteWrapper(row);
                        repeater.removeRow(i);
                    }
                }
            } else if (obj instanceof Number) {
                int index = (int)Context.toNumber(obj);
                if (index > 0 && index < repeater.getSize()) {
                    Widget row = repeater.getRow(index);
                    formWidget.deleteWrapper(row);
                    repeater.removeRow(index);
                }
            } else {
                //...
            }
        }
    }
    
    private void handleEvent(WidgetEvent e) {
        if (e instanceof ActionEvent) {
            Object obj = super.get("onClick", this);
            if (obj instanceof Function) {
                try {
                    Function fun = (Function)obj;
                    Object[] args = new Object[1];
                    Scriptable scope = getTopLevelScope(this);
                    Scriptable thisObj = scope;
                    Context cx = Context.getCurrentContext();
                    args[0] = ((ActionEvent)e).getActionCommand();
                    fun.call(cx, scope, thisObj, args);
                } catch (Exception exc) {
                    throw Context.reportRuntimeError(exc.getMessage());
                }
            }
        } else if (e instanceof ValueChangedEvent) {
            ValueChangedEvent vce = (ValueChangedEvent)e;
            Object obj = super.get("onChange", this);
            if (obj instanceof Function) {
                try {
                    Function fun = (Function)obj;
                    Object[] args = new Object[2];
                    Scriptable scope = getTopLevelScope(this);
                    Scriptable thisObj = scope;
                    Context cx = Context.getCurrentContext();
                    args[0] = vce.getOldValue();
                    args[1] = vce.getNewValue();
                    fun.call(cx, scope, thisObj, args);
                } catch (Exception exc) {
                    throw Context.reportRuntimeError(exc.getMessage());
                }
            }
        }
    }

    public void jsFunction_setSelectionList(Object arg) throws Exception {
        if (delegate instanceof Field ||
            delegate instanceof MultiValueField) {
            arg = unwrap(arg);
            if (arg instanceof SelectionList) {
                SelectionList selectionList = (SelectionList)arg;
                if (delegate instanceof Field) {
                    ((Field)delegate).setSelectionList(selectionList);
                } else {
                    ((MultiValueField)delegate).setSelectionList(selectionList);
                }
            } else {
                String str = Context.toString(arg);
                if (delegate instanceof Field) {
                    ((Field)delegate).setSelectionList(str);
                } else {
                    ((MultiValueField)delegate).setSelectionList(str);
                }
            }
        }
    }

    static final Object[] WIDGET_CLASS_MAP = {
        Form.class, "Form",
        Field.class, "Field",
        Action.class, "Action",
        Repeater.class, "Repeater",
        Repeater.RepeaterRow.class, "RepeaterRow",
        AggregateField.class, "AggregateField",
        BooleanField.class, "BooleanField",
        MultiValueField.class, "MultiValueField",
        Output.class, "Output",
        Submit.class, "Submit",
        Upload.class, "Upload"
    };

    public String jsFunction_getWidgetClass() {
        for (int i = 0; i < WIDGET_CLASS_MAP.length; i += 2) {
            Class c = (Class)WIDGET_CLASS_MAP[i];
            if (c.isAssignableFrom(delegate.getClass())) {
                return (String)WIDGET_CLASS_MAP[i + 1];
            }
        }
        return "<unknown>";
    }

    public String jsFunction_toString() {
        return "[object Widget (" + jsFunction_getWidgetClass() + ")]";
    }

}
