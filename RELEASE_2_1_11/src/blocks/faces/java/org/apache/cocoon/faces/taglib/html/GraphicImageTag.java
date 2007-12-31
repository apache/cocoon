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
import javax.faces.component.UIGraphic;

/**
 * @version CVS $Id$
 */
public class GraphicImageTag extends UIComponentTag {

    private String url;
    private String value;
    private String alt;
    private String dir;
    private String height;
    private String ismap;
    private String lang;
    private String longdesc;
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
    private String style;
    private String styleClass;
    private String title;
    private String usemap;
    private String width;


    public void setUrl(String url) {
        this.url = url;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public void setIsmap(String ismap) {
        this.ismap = ismap;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setLongdesc(String longdesc) {
        this.longdesc = longdesc;
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

    public void setStyle(String style) {
        this.style = style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUsemap(String usemap) {
        this.usemap = usemap;
    }

    public void setWidth(String width) {
        this.width = width;
    }


    public String getRendererType() {
        return "javax.faces.Image";
    }

    public String getComponentType() {
        return "javax.faces.HtmlGraphicImage";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIGraphic graphic = null;
        try {
            graphic = (UIGraphic) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIGraphic. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (url != null) {
            if (FacesUtils.isExpression(url)) {
                graphic.setValueBinding("url", createValueBinding(url));
            } else {
                graphic.setUrl(url);
            }
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                graphic.setValueBinding("value", createValueBinding(value));
            } else {
                graphic.setValue(value);
            }
        }

        setProperty(component, "alt", alt);
        setProperty(component, "dir", dir);
        setProperty(component, "height", height);

        setBooleanProperty(component, "ismap", ismap);

        setProperty(component, "lang", lang);
        setProperty(component, "longdesc", longdesc);
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

        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "title", title);
        setProperty(component, "usemap", usemap);
        setProperty(component, "width", width);
    }

    public void recycle() {
        super.recycle();
        url = null;
        value = null;
        alt = null;
        dir = null;
        height = null;
        ismap = null;
        lang = null;
        longdesc = null;
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
        style = null;
        styleClass = null;
        title = null;
        usemap = null;
        width = null;
    }
}
