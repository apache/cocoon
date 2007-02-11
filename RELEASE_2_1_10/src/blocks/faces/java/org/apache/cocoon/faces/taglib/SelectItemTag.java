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
package org.apache.cocoon.faces.taglib;

import org.apache.cocoon.faces.FacesUtils;
import org.apache.commons.lang.BooleanUtils;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;

/**
 * @version CVS $Id$
 */
public class SelectItemTag extends UIComponentTag {

    protected String value;
    protected String itemValue;
    protected String itemLabel;
    protected String itemDescription;
    protected String itemDisabled;

    public void setValue(String value) {
        this.value = value;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setItemDisabled(String itemDisabled) {
        this.itemDisabled = itemDisabled;
    }

    protected String getComponentType() {
        return "javax.faces.SelectItem";
    }

    protected String getRendererType() {
        return null;
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UISelectItem selectItem;
        try {
            selectItem = (UISelectItem) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UISelectItem. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                selectItem.setValueBinding("value", createValueBinding(value));
            } else {
                selectItem.setValue(value);
            }
        }

        if (itemValue != null) {
            if (FacesUtils.isExpression(itemValue)) {
                selectItem.setValueBinding("itemValue", createValueBinding(itemValue));
            } else {
                selectItem.setItemValue(itemValue);
            }
        }

        if (itemLabel != null) {
            if (FacesUtils.isExpression(itemLabel)) {
                selectItem.setValueBinding("itemLabel", createValueBinding(itemLabel));
            } else {
                selectItem.setItemLabel(itemLabel);
            }
        }

        if (itemDescription != null) {
            if (FacesUtils.isExpression(itemDescription)) {
                selectItem.setValueBinding("itemDescription", createValueBinding(itemDescription));
            } else {
                selectItem.setItemDescription(itemDescription);
            }
        }

        if (itemDisabled != null) {
            if (FacesUtils.isExpression(itemDisabled)) {
                selectItem.setValueBinding("itemDisabled", createValueBinding(itemDisabled));
            } else {
                selectItem.setItemDisabled(BooleanUtils.toBoolean(itemDisabled));
            }
        }
    }

    public void recycle() {
        super.recycle();
        this.value = null;
        this.itemValue = null;
        this.itemLabel = null;
        this.itemDescription = null;
        this.itemDisabled = null;
    }
}
