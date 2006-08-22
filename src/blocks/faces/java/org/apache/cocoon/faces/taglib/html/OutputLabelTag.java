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

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;

/**
 * @version CVS $Id$
 */
public class OutputLabelTag extends UIComponentTag {

    private String converter;
    private String value;
    private String accesskey;
    private String dir;
    private String _for;
    private String lang;
    private String onblur;
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
    private String style;
    private String styleClass;
    private String tabindex;
    private String title;


    public void setConverter(String converter) {
        this.converter = converter;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setFor(String _for) {
        this._for = _for;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setOnblur(String onblur) {
        this.onblur = onblur;
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
        return "javax.faces.Text";
    }

    public String getComponentType() {
        return "javax.faces.HtmlOutputText";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIOutput output;
        try {
            output = (UIOutput) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIOutput. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (converter != null) {
            if (FacesUtils.isExpression(converter)) {
                output.setValueBinding("converter", createValueBinding(converter));
            } else {
                output.setConverter(getApplication().createConverter(converter));
            }
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                output.setValueBinding("value", createValueBinding(value));
            } else {
                output.setValue(value);
            }
        }

        setProperty(component, "accesskey", accesskey);
        setProperty(component, "dir", dir);
        // FIXME: Should it be __for?
        setProperty(component, "for", _for);
        setProperty(component, "lang", lang);
        setProperty(component, "onblur", onblur);
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

        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "tabindex", tabindex);
        setProperty(component, "title", title);
    }

    public void recycle() {
        super.recycle();
        converter = null;
        value = null;
        accesskey = null;
        dir = null;
        _for = null;
        lang = null;
        onblur = null;
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
        style = null;
        styleClass = null;
        tabindex = null;
        title = null;
    }
}
