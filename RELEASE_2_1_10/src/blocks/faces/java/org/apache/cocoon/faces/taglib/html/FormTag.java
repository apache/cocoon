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

import org.apache.cocoon.faces.taglib.UIComponentTag;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;

/**
 * @version CVS $Id$
 */
public class FormTag extends UIComponentTag {

    private String accept;
    private String acceptcharset;
    private String dir;
    private String enctype;
    private String lang;
    private String onclick;
    private String ondblclick;
    private String onkeydown;
    private String onkeypress;
    private String onkeyup;
    private String onmousedown;
    private String onmousemove;
    private String onmouseout;
    private String onmouseover;
    private String onmouseup;
    private String onreset;
    private String onsubmit;
    private String style;
    private String styleClass;
    private String target;
    private String title;


    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setAcceptcharset(String acceptcharset) {
        this.acceptcharset = acceptcharset;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setEnctype(String enctype) {
        this.enctype = enctype;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }

    public void setOndblclick(String ondblclick) {
        this.ondblclick = ondblclick;
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

    public void setOnreset(String onreset) {
        this.onreset = onreset;
    }

    public void setOnsubmit(String onsubmit) {
        this.onsubmit = onsubmit;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRendererType() {
        return "javax.faces.Form";
    }

    public String getComponentType() {
        return "javax.faces.HtmlForm";
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (!(component instanceof UIForm)) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIForm. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        setProperty(component, "accept", accept);
        setProperty(component, "acceptcharset", acceptcharset);
        setProperty(component, "dir", dir);
        setProperty(component, "enctype", enctype);
        setProperty(component, "lang", lang);
        setProperty(component, "onclick", onclick);
        setProperty(component, "ondblclick", ondblclick);
        setProperty(component, "onkeydown", onkeydown);
        setProperty(component, "onkeypress", onkeypress);
        setProperty(component, "onkeyup", onkeyup);
        setProperty(component, "onmousedown", onmousedown);
        setProperty(component, "onmousemove", onmousemove);
        setProperty(component, "onmouseout", onmouseout);
        setProperty(component, "onmouseover", onmouseover);
        setProperty(component, "onmouseup", onmouseup);
        setProperty(component, "onreset", onreset);
        setProperty(component, "onsubmit", onsubmit);
        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "target", target);
        setProperty(component, "title", title);
    }

    public void recycle() {
        super.recycle();
        accept = null;
        acceptcharset = null;
        dir = null;
        enctype = null;
        lang = null;
        onclick = null;
        ondblclick = null;
        onkeydown = null;
        onkeypress = null;
        onkeyup = null;
        onmousedown = null;
        onmousemove = null;
        onmouseout = null;
        onmouseover = null;
        onmouseup = null;
        onreset = null;
        onsubmit = null;
        style = null;
        styleClass = null;
        target = null;
        title = null;
    }
}
