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

package org.apache.cocoon.forms.flow.javascript.v2;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.event.ActionEvent;
import org.apache.cocoon.forms.event.FormHandler;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.formmodel.Action;
import org.apache.cocoon.forms.formmodel.AggregateField;
import org.apache.cocoon.forms.formmodel.BooleanField;
import org.apache.cocoon.forms.formmodel.ContainerWidget;
import org.apache.cocoon.forms.formmodel.DataWidget;
import org.apache.cocoon.forms.formmodel.Field;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.MultiValueField;
import org.apache.cocoon.forms.formmodel.Output;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.SelectableWidget;
import org.apache.cocoon.forms.formmodel.Submit;
import org.apache.cocoon.forms.formmodel.Upload;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
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

/**
 * @version $Id: ScriptableWidget.java,v 1.11 2004/05/08 12:51:29 bruno Exp $
 * 
 */
public class ScriptableWidget extends ScriptableObject {

    final static String WIDGETS_PROPERTY = "__widgets__";

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
            defineProperty(WIDGETS_PROPERTY, widgetMap, DONTENUM|PERMANENT);
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
            Map widgetMap = (Map)super.get(WIDGETS_PROPERTY, this);
            widgetMap.remove(w);
        }
    }

    private ScriptableWidget wrap(Widget w) {
        if (w == null) return null;
        if (delegate instanceof Form) {
            Map widgetMap = (Map)super.get(WIDGETS_PROPERTY, this);
            ScriptableWidget result = null;
            result = (ScriptableWidget)widgetMap.get(w);
            if (result == null) {
                result = new ScriptableWidget(w);
                result.formWidget = this;
                result.setPrototype(getClassPrototype(this, getClassName()));
                result.setParentScope(getParentScope());
                widgetMap.put(w, result);
            }
            return result;
        } else {
            return formWidget.wrap(w);
        }
    }

    public boolean has(String id, Scriptable start) {
        if (delegate != null && delegate instanceof ContainerWidget) {
            Widget sub = ((ContainerWidget)delegate).getChild(id);
            if (sub != null) {
                return true;
            }
        } 
        return super.has(id, start);
    }

    public boolean has(int index, Scriptable start) {
        if (super.has(index, start)) {
            return true;
        }
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            return index >= 0 && index < repeater.getSize();
        }
        if (delegate instanceof MultiValueField) {
            Object[] values = (Object[])delegate.getValue();
            return index >= 0 && index < values.length;
        }
        return false;
    }

    public Object get(String id, Scriptable start) {
        Object result = super.get(id, start);
        if (result != NOT_FOUND) {
            return result;
        }
        if (delegate != null && delegate instanceof ContainerWidget) {
            Widget sub = ((ContainerWidget)delegate).getChild(id);
            if (sub != null) {
                return wrap(sub);
            }
        }
        return NOT_FOUND;
    }

    public Object get(int index, Scriptable start) {
        Object result = super.get(index, start);
        if (result != NOT_FOUND) {
            return result;
        }
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            if (index >= 0) {
                int count = index + 1 - repeater.getSize();
                if (count > 0) {
                    ScriptableWidget[] rows = new ScriptableWidget[count];
                    for (int i = 0; i < count; i++) {
                        rows[i] = wrap(repeater.addRow());
                    }
                    for (int i = 0; i < count; i++) {
                        rows[i].notifyAddRow();
                    }
                }
                return wrap(repeater.getRow(index));
            }
        } else if (delegate instanceof MultiValueField) {
            Object[] values = (Object[])delegate.getValue();
            if (index >= 0 && index < values.length) {
                return values[index];
            }
        }
        return NOT_FOUND;
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

    private void deleteRow(Repeater repeater, int index) {
        Widget row = repeater.getRow(index);
        ScriptableWidget s = wrap(row);
        s.notifyRemoveRow();
        formWidget.deleteWrapper(row);
        repeater.removeRow(index);
    }

    private void notifyAddRow() {
        ScriptableWidget repeater = wrap(delegate.getParent());
        Object prop = getProperty(repeater, "onAddRow");
        if (prop instanceof Function) {
            try {
                Function fun = (Function)prop;
                Object[] args = new Object[1];
                Scriptable scope = getTopLevelScope(this);
                Scriptable thisObj = scope;
                Context cx = Context.getCurrentContext();
                args[0] = this;
                fun.call(cx, scope, thisObj, args);
            } catch (Exception exc) {
                throw Context.reportRuntimeError(exc.getMessage());
            }
        }
    }

    private void notifyRemoveRow() {
        ScriptableWidget repeater = wrap(delegate.getParent());
        Object prop = getProperty(repeater, "onRemoveRow");
        if (prop instanceof Function) {
            try {
                Function fun = (Function)prop;
                Object[] args = new Object[1];
                Scriptable scope = getTopLevelScope(this);
                Scriptable thisObj = scope;
                Context cx = Context.getCurrentContext();
                args[0] = this;
                fun.call(cx, scope, thisObj, args);
            } catch (Exception exc) {
                throw Context.reportRuntimeError(exc.getMessage());
            }
        }
    }

    public void delete(int index) {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            if (index >= 0 && index < repeater.getSize()) {
                deleteRow(repeater, index);
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
                for (;i < values.length; i++) {
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
                    deleteRow(repeater, repeater.getSize() - 1);
                }
            } else {
                for (int i = size; i < len; ++i) {
                    wrap(repeater.addRow()).notifyAddRow();
                }
            }
        }
    }

    public Object jsGet_length() {
        if (delegate instanceof Repeater) {
            Repeater repeater = (Repeater)delegate;
            return new Integer(repeater.getSize());
        }
        return Undefined.instance;
    }

    public void jsSet_value(Object value) throws JavaScriptException {
        if (delegate instanceof AggregateField) {
            AggregateField aggregateField = (AggregateField)delegate;
            if (value instanceof Scriptable) {
                Scriptable obj = (Scriptable)value;
                Object[] ids = obj.getIds();
                for (int i = 0; i < ids.length; i++) {
                    String id = String.valueOf(ids[i]);
                    Object val = getProperty(obj, id);
                    ScriptableWidget wid = wrap(aggregateField.getChild(id));
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
                aggregateField.combineFields();
                return;
            }
            // fall through
        }
        if (delegate instanceof DataWidget) {
            value = unwrap(value);
            if (value != null) {
                // Coerce values
                Datatype datatype = ((DataWidget)delegate).getDatatype();
                Class typeClass = datatype.getTypeClass();
                if (typeClass == String.class) {
                    value = Context.toString(value);
                } else if (typeClass == boolean.class || 
                           typeClass == Boolean.class) {
                    value = Context.toBoolean(value) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    if (value instanceof Double) {
                        // make cform accept a JS Number
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
                    deleteRow(repeater, i);
                }
                for (int i = 0; i < len; i++) {
                    Object elemValue = getProperty(arr, i);
                    ScriptableWidget wid = wrap(repeater.getRow(i));
                    wid.jsSet_value(elemValue);
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
                    ScriptableWidget wid = wrap(row.getChild(id));
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
            delegate.setValue(value);
        }
    }

    public String jsFunction_getId() {
        return delegate.getId();
    }

    public ScriptableWidget jsFunction_getSubmitWidget() {
        return wrap(delegate.getForm().getSubmitWidget());
    }

    public String jsFunction_getRequestParameterName() {
        return delegate.getRequestParameterName();
    }

//    public String jsFunction_getNamespace() {
//        return delegate.getNamespace();
//    }

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

    public ScriptableWidget jsFunction_getChild(String id) {
        Widget sub = null;
        if (delegate instanceof ContainerWidget)
            sub = ((ContainerWidget)delegate).getChild(id);
        return wrap(sub);
    }

    public ScriptableWidget jsFunction_lookupWidget(String id) {
        Widget sub = null;
        sub = delegate.lookupWidget(id);
        return wrap(sub);
    }

    public void jsFunction_setValidationError(Object message /* null to clear error */, 
                                              Object parameters) {
        if (delegate instanceof ValidationErrorAware) {
            String[] parms = null;
            if (parameters != null && parameters != Undefined.instance) {
                Scriptable obj = Context.toObject(parameters, this);
                int len = (int)
                    Context.toNumber(getProperty(obj, "length"));
                parms = new String[len];
                for (int i = 0; i < len; i++) {
                    parms[i] = Context.toString(getProperty(obj, i));
                }
            }
            ValidationError validationError = null;
            if (message != null) {
                if (parms != null && parms.length > 0) {
                    validationError = 
                        new ValidationError(Context.toString(message), parms);
                } else {
                    validationError = 
                        new ValidationError(Context.toString(message), parms != null);
                }
            }
            ((ValidationErrorAware)delegate).setValidationError(validationError);
            formWidget.notifyValidationErrorListener(this, validationError);
        }
    }

    private void notifyValidationErrorListener(ScriptableWidget widget,
                                               ValidationError error) {
        Object fun = getProperty(this, "validationErrorListener");
        if (fun instanceof Function) {
            try {
                Scriptable scope = getTopLevelScope(this);
                Scriptable thisObj = scope;
                Context cx = Context.getCurrentContext();
                Object[] args = new Object[2];
                args[0] = widget;
                args[1] = error;
                ((Function)fun).call(cx, scope, thisObj, args);
            } catch (Exception exc) {
                throw Context.reportRuntimeError(exc.getMessage());
            }
        }
    }

    public Widget jsFunction_unwrap() {
        return delegate;
    }

    public ScriptableWidget jsFunction_addRow() {
        ScriptableWidget result = null;
        if (delegate instanceof Repeater) {
            result = wrap(((Repeater)delegate).addRow());
            result.notifyAddRow();
        }
        return result;
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
                        deleteRow(repeater, i);
                    }
                }
            } else {
                int index = (int)Context.toNumber(obj);
                if (index >= 0 && index < repeater.getSize()) {
                    deleteRow(repeater, index);
                }
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

    public void jsFunction_setSelectionList(Object arg, 
                                            Object valuePathArg, 
                                            Object labelPathArg) 
        throws Exception {
        if (delegate instanceof SelectableWidget) {
            arg = unwrap(arg);
            if (valuePathArg != Undefined.instance && labelPathArg != Undefined.instance) {
                String valuePath = Context.toString(valuePathArg);
                String labelPath = Context.toString(labelPathArg);
                ((SelectableWidget)delegate).setSelectionList(arg, valuePath, labelPath);
            } else {
                if (arg instanceof SelectionList) {
                    SelectionList selectionList = (SelectionList)arg;
                    ((SelectableWidget)delegate).setSelectionList(selectionList);
                } else {
                    String str = Context.toString(arg);
                    ((SelectableWidget)delegate).setSelectionList(str);
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
