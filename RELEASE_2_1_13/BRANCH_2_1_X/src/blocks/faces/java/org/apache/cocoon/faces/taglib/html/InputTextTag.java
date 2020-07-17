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

import javax.faces.component.UIComponent;

/**
 * @version CVS $Id$
 */
public class InputTextTag extends InputHiddenTag {

    private String accesskey;
    private String alt;
    private String dir;
    private String disabled;
    private String lang;
    private String maxlength;
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
    private String size;
    private String style;
    private String styleClass;
    private String tabindex;
    private String title;


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

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setMaxlength(String maxlength) {
        this.maxlength = maxlength;
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

    public void setSize(String size) {
        this.size = size;
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
        return "javax.faces.HtmlInputText";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        setProperty(component, "accesskey", accesskey);
        setProperty(component, "alt", alt);
        setProperty(component, "dir", dir);

        setBooleanProperty(component, "disabled", disabled);

        setProperty(component, "lang", lang);

        setIntegerProperty(component, "maxlength", maxlength);

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
        setIntegerProperty(component, "size", size);

        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "tabindex", tabindex);
        setProperty(component, "title", title);
    }

    public void recycle() {
        super.recycle();
        accesskey = null;
        alt = null;
        dir = null;
        disabled = null;
        lang = null;
        maxlength = null;
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
        size = null;
        style = null;
        styleClass = null;
        tabindex = null;
        title = null;
    }
}
