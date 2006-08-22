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
package org.apache.cocoon.faces.taglib.html;

import org.apache.cocoon.faces.FacesUtils;
import org.apache.cocoon.faces.taglib.UIComponentTag;
import org.apache.commons.lang.BooleanUtils;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectOne;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ValueChangeEvent;

/**
 * @version CVS $Id$
 */
public class SelectOneRadioTag extends UIComponentTag {

    private String converter;
    private String immediate;
    private String required;
    private String validator;
    private String value;
    private String valueChangeListener;
    private String accesskey;
    private String border;
    private String dir;
    private String disabled;
    private String disabledClass;
    private String enabledClass;
    private String lang;
    private String layout;
    private String onblur;
    private String onchange;
    private String onclick;
    private String ondblclick;
    private String onfocus;
    private String onkeydown;
    private String onkeypress;
    private String onkeyup;
    private String onmousedown;
    private String onmousemove;
    private String onmouseout;
    private String onmouseover;
    private String onmouseup;
    private String onselect;
    private String readonly;
    private String style;
    private String styleClass;
    private String tabindex;
    private String title;


    public void setConverter(String converter) {
        this.converter = converter;
    }

    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValueChangeListener(String valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public void setBorder(String border) {
        this.border = border;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    public void setDisabledClass(String disabledClass) {
        this.disabledClass = disabledClass;
    }

    public void setEnabledClass(String enabledClass) {
        this.enabledClass = enabledClass;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setOnblur(String onblur) {
        this.onblur = onblur;
    }

    public void setOnchange(String onchange) {
        this.onchange = onchange;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public void setOndblclick(String ondblclick) {
        this.ondblclick = ondblclick;
    }

    public void setOnfocus(String onfocus) {
        this.onfocus = onfocus;
    }

    public void setOnkeydown(String onkeydown) {
        this.onkeydown = onkeydown;
    }

    public void setOnkeypress(String onkeypress) {
        this.onkeypress = onkeypress;
    }

    public void setOnkeyup(String onkeyup) {
        this.onkeyup = onkeyup;
    }

    public void setOnmousedown(String onmousedown) {
        this.onmousedown = onmousedown;
    }

    public void setOnmousemove(String onmousemove) {
        this.onmousemove = onmousemove;
    }

    public void setOnmouseout(String onmouseout) {
        this.onmouseout = onmouseout;
    }

    public void setOnmouseover(String onmouseover) {
        this.onmouseover = onmouseover;
    }

    public void setOnmouseup(String onmouseup) {
        this.onmouseup = onmouseup;
    }

    public void setOnselect(String onselect) {
        this.onselect = onselect;
    }

    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public void setTabindex(String tabindex) {
        this.tabindex = tabindex;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getRendererType() {
        return "javax.faces.Radio";
    }

    public String getComponentType() {
        return "javax.faces.HtmlSelectOneRadio";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UISelectOne select;
        try {
            select = (UISelectOne) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UISelectOne. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (converter != null) {
            if (FacesUtils.isExpression(converter)) {
                select.setValueBinding("converter", createValueBinding(converter));
            } else {
                select.setConverter(getApplication().createConverter(converter));
            }
        }

        if (immediate != null) {
            if (FacesUtils.isExpression(immediate)) {
                select.setValueBinding("immediate", createValueBinding(immediate));
            } else {
                select.setImmediate(BooleanUtils.toBoolean(immediate));
            }
        }

        if (required != null) {
            if (FacesUtils.isExpression(required)) {
                select.setValueBinding("required", createValueBinding(required));
            } else {
                select.setRequired(BooleanUtils.toBoolean(required));
            }
        }

        if (validator != null) {
            if (FacesUtils.isExpression(validator)) {
                MethodBinding vb = getApplication().createMethodBinding(validator,
                                                                        new Class[]{ FacesContext.class, UIComponent.class, Object.class });
                select.setValidator(vb);
            } else {
                throw new FacesException("Tag <" + getClass().getName() + "> validator must be an expression. " +
                                         "Got <" + validator + ">");
            }
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                select.setValueBinding("value", createValueBinding(value));
            } else {
                select.setValue(value);
            }
        }

        if (valueChangeListener != null) {
            if (FacesUtils.isExpression(valueChangeListener)) {
                MethodBinding vb = getApplication().createMethodBinding(valueChangeListener,
                                                                        new Class[]{ ValueChangeEvent.class });
                select.setValueChangeListener(vb);
            } else {
                throw new FacesException("Tag <" + getClass().getName() + "> valueChangeListener must be an expression. " +
                                         "Got <" + valueChangeListener + ">");
            }
        }

        setProperty(component, "accesskey", accesskey);

        setIntegerProperty(component, "border", border);

        setProperty(component, "dir", dir);

        setBooleanProperty(component, "disabled", disabled);

        setProperty(component, "disabledClass", disabledClass);
        setProperty(component, "enabledClass", enabledClass);
        setProperty(component, "lang", lang);
        setProperty(component, "layout", layout);
        setProperty(component, "onblur", onblur);
        setProperty(component, "onchange", onchange);
        setProperty(component, "onclick", onclick);
        setProperty(component, "ondblclick", ondblclick);
        setProperty(component, "onfocus", onfocus);
        setProperty(component, "onkeydown", onkeydown);
        setProperty(component, "onkeypress", onkeypress);
        setProperty(component, "onkeyup", onkeyup);
        setProperty(component, "onmousedown", onmousedown);
        setProperty(component, "onmousemove", onmousemove);
        setProperty(component, "onmouseout", onmouseout);
        setProperty(component, "onmouseover", onmouseover);
        setProperty(component, "onmouseup", onmouseup);
        setProperty(component, "onselect", onselect);

        setBooleanProperty(component, "readonly", readonly);

        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "tabindex", tabindex);
        setProperty(component, "title", title);
    }

    public void recycle() {
        super.recycle();
        converter = null;
        immediate = null;
        required = null;
        validator = null;
        value = null;
        valueChangeListener = null;
        accesskey = null;
        dir = null;
        disabled = null;
        disabledClass = null;
        enabledClass = null;
        lang = null;
        layout = null;
        onblur = null;
        onchange = null;
        onclick = null;
        ondblclick = null;
        onfocus = null;
        onkeydown = null;
        onkeypress = null;
        onkeyup = null;
        onmousedown = null;
        onmousemove = null;
        onmouseout = null;
        onmouseover = null;
        onmouseup = null;
        onselect = null;
        readonly = null;
        style = null;
        styleClass = null;
        tabindex = null;
        title = null;
    }
}
