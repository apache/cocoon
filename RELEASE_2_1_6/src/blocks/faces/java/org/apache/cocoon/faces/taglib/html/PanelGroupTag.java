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
public class PanelGroupTag extends UIComponentTag {

    private String style;
    private String styleClass;


    public void setStyle(String style) {
        this.style = style;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }


    public String getRendererType() {
        return "javax.faces.Group";
    }

    public String getComponentType() {
        return "javax.faces.HtmlPanelGroup";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        if (!(component instanceof UIPanel)) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIPanel. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        setProperty(component, "style", style);
        setProperty(component, "styleClass", styleClass);
    }

    public void recycle() {
        super.recycle();
        style = null;
        styleClass = null;
    }
}
