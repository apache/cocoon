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
package org.apache.cocoon.faces.taglib.html;

import org.apache.cocoon.faces.taglib.UIComponentTag;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;

/**
 * @version CVS $Id$
 */
public class PanelGridTag extends UIComponentTag {

    private String bgcolor;
    private String border;
    private String cellpadding;
    private String cellspacing;
    private String columnClasses;
    private String columns;
    private String dir;
    private String footerClass;
    private String frame;
    private String headerClass;
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
    private String rowClasses;
    private String rules;
    private String style;
    private String styleClass;
    private String summary;
    private String title;
    private String width;


    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public void setBorder(String border) {
        this.border = border;
    }

    public void setCellpadding(String cellpadding) {
        this.cellpadding = cellpadding;
    }

    public void setCellspacing(String cellspacing) {
        this.cellspacing = cellspacing;
    }

    public void setColumnClasses(String columnClasses) {
        this.columnClasses = columnClasses;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setFooterClass(String footerClass) {
        this.footerClass = footerClass;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public void setHeaderClass(String headerClass) {
        this.headerClass = headerClass;
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

    public void setRowClasses(String rowClasses) {
        this.rowClasses = rowClasses;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWidth(String width) {
        this.width = width;
    }


    public String getRendererType() {
        return "javax.faces.Grid";
    }

    public String getComponentType() {
        return "javax.faces.HtmlPanelGrid";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (!(component instanceof UIPanel)) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIPanel. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        setProperty(component, "bgcolor", bgcolor);

        setIntegerProperty(component, "border", border);

        setProperty(component, "cellpadding", cellpadding);
        setProperty(component, "cellspacing", cellspacing);
        setProperty(component, "columnClasses", columnClasses);

        setIntegerProperty(component, "columns", columns);

        setProperty(component, "dir", dir);
        setProperty(component, "footerClass", footerClass);
        setProperty(component, "frame", frame);
        setProperty(component, "headerClass", headerClass);
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

        setProperty(component, "rowClasses", rowClasses);
        setProperty(component, "rules", rules);
        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "summary", summary);
        setProperty(component, "title", title);
        setProperty(component, "width", width);
    }

    public void recycle() {
        super.recycle();
        bgcolor = null;
        border = null;
        cellpadding = null;
        cellspacing = null;
        columnClasses = null;
        columns = null;
        dir = null;
        footerClass = null;
        frame = null;
        headerClass = null;
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
        rowClasses = null;
        rules = null;
        style = null;
        styleClass = null;
        summary = null;
        title = null;
        width = null;
    }
}
