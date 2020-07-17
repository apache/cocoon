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

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItems;

/**
 * @version CVS $Id$
 */
public class SelectItemsTag extends UIComponentTag {

    protected String value;

    public void setValue(String value) {
        this.value = value;
    }

    protected String getComponentType() {
        return "javax.faces.SelectItems";
    }

    protected String getRendererType() {
        return null;
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UISelectItems selectItems;
        try {
            selectItems = (UISelectItems) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UISelectItems. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                selectItems.setValueBinding("value", createValueBinding(value));
            } else {
                selectItems.setValue(value);
            }
        }
    }

    public void recycle() {
        super.recycle();
        this.value = null;
    }
}
