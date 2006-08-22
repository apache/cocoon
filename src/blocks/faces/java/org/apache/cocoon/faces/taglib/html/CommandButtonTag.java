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
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;

/**
 * @version CVS $Id$
 */
public class CommandButtonTag extends UIComponentTag {

    private String action;
    private String actionListener;
    private String immediate;
    private String value;
    private String accesskey;
    private String alt;
    private String dir;
    private String disabled;
    private String image;
    private String lang;
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
    private String type;


    public void setAction(String action) {
        this.action = action;
    }

    public void setActionListener(String actionListener) {
        this.actionListener = actionListener;
    }

    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setLang(String lang) {
        this.lang = lang;
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

    public void setType(String type) {
        this.type = type;
    }

    public String getRendererType() {
        return "javax.faces.Button";
    }

    public String getComponentType() {
        return "javax.faces.HtmlCommandButton";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UICommand command;
        try {
            command = (UICommand) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UICommand. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (action != null) {
            MethodBinding vb;
            if (FacesUtils.isExpression(action)) {
                vb = getApplication().createMethodBinding(action, null);
            } else {
                vb = new ConstantMethodBinding(action);
            }
            command.setAction(vb);
        }

        if (actionListener != null) {
            if (FacesUtils.isExpression(actionListener)) {
                MethodBinding vb = getApplication().createMethodBinding(actionListener,
                                                                        new Class[]{ ActionEvent.class });
                command.setActionListener(vb);
            } else {
                throw new FacesException("Tag <" + getClass().getName() + "> actionListener must be an expression. " +
                                         "Got <" + actionListener + ">");
            }
        }

        if (immediate != null) {
            if (FacesUtils.isExpression(immediate)) {
                command.setValueBinding("immediate", createValueBinding(immediate));
            } else {
                command.setImmediate(BooleanUtils.toBoolean(immediate));
            }
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                command.setValueBinding("value", createValueBinding(value));
            } else {
                command.setValue(value);
            }
        }

        setProperty(component, "accesskey", accesskey);
        setProperty(component, "alt", alt);
        setProperty(component, "dir", dir);

        setBooleanProperty(component, "disabled", disabled);

        setProperty(component, "image", image);
        setProperty(component, "lang", lang);
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
        setProperty(component, "type", type);
    }

    public void recycle() {
        super.recycle();
        action = null;
        actionListener = null;
        immediate = null;
        value = null;
        accesskey = null;
        alt = null;
        dir = null;
        disabled = null;
        image = null;
        lang = null;
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
        type = null;
    }
}
