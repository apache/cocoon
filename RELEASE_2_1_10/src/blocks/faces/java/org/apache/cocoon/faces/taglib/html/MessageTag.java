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
import javax.faces.component.UIMessage;

/**
 * @version CVS $Id$
 */
public class MessageTag extends UIComponentTag {

    private String _for;
    private String showDetail;
    private String showSummary;
    private String errorClass;
    private String errorStyle;
    private String fatalClass;
    private String fatalStyle;
    private String infoClass;
    private String infoStyle;
    private String layout;
    private String style;
    private String styleClass;
    private String title;
    private String tooltip;
    private String warnClass;
    private String warnStyle;


    public void setFor(String _for) {
        this._for = _for;
    }

    public void setShowDetail(String showDetail) {
        this.showDetail = showDetail;
    }

    public void setShowSummary(String showSummary) {
        this.showSummary = showSummary;
    }

    public void setErrorClass(String errorClass) {
        this.errorClass = errorClass;
    }

    public void setErrorStyle(String errorStyle) {
        this.errorStyle = errorStyle;
    }

    public void setFatalClass(String fatalClass) {
        this.fatalClass = fatalClass;
    }

    public void setFatalStyle(String fatalStyle) {
        this.fatalStyle = fatalStyle;
    }

    public void setInfoClass(String infoClass) {
        this.infoClass = infoClass;
    }

    public void setInfoStyle(String infoStyle) {
        this.infoStyle = infoStyle;
    }

    public void setLayout(String layout) {
        this.layout = layout;
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

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public void setWarnClass(String warnClass) {
        this.warnClass = warnClass;
    }

    public void setWarnStyle(String warnStyle) {
        this.warnStyle = warnStyle;
    }


    public String getRendererType() {
        return "javax.faces.Message";
    }

    public String getComponentType() {
        return "javax.faces.HtmlMessage";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIMessage message = null;
        try {
            message = (UIMessage) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIMessage. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (_for != null) {
            // FIXME Should it be "for"?
            if (FacesUtils.isExpression(_for)) {
                message.setValueBinding("_for", createValueBinding(_for));
            } else {
                message.setFor(_for);
            }
        }

        if (showDetail != null) {
            if (FacesUtils.isExpression(showDetail)) {
                message.setValueBinding("showDetail", createValueBinding(showDetail));
            } else {
                message.setShowDetail(BooleanUtils.toBoolean(showDetail));
            }
        }

        if (showSummary != null) {
            if (FacesUtils.isExpression(showSummary)) {
                message.setValueBinding("showSummary", createValueBinding(showSummary));
            } else {
                message.setShowSummary(BooleanUtils.toBoolean(showSummary));
            }
        }

        setProperty(component, "errorClass", errorClass);
        setProperty(component, "errorStyle", errorStyle);
        setProperty(component, "fatalClass", fatalClass);
        setProperty(component, "fatalStyle", fatalStyle);
        setProperty(component, "infoClass", infoClass);
        setProperty(component, "infoStyle", infoStyle);
        setProperty(component, "layout", layout);
        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
        setProperty(component, "title", title);

        setBooleanProperty(component, "tooltip", tooltip);

        setProperty(component, "warnClass", warnClass);
        setProperty(component, "warnStyle", warnStyle);
    }

    public void recycle() {
        super.recycle();
        _for = null;
        showDetail = null;
        showSummary = null;
        errorClass = null;
        errorStyle = null;
        fatalClass = null;
        fatalStyle = null;
        infoClass = null;
        infoStyle = null;
        layout = null;
        style = null;
        styleClass = null;
        title = null;
        tooltip = null;
        warnClass = null;
        warnStyle = null;
    }
}
