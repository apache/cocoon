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

import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.FacesException;

/**
 * @version CVS $Id$
 */
public class ParameterTag extends UIComponentTag {

    private String name;
    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    protected String getComponentType() {
        return "javax.faces.Parameter";
    }

    protected String getRendererType() {
        return null;
    }

    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIParameter parameter;
        try {
            parameter = (UIParameter) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIParameter. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (name != null) {
            if (FacesUtils.isExpression(name)) {
                parameter.setValueBinding("name", createValueBinding(name));
            } else {
                parameter.setName(name);
            }
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                parameter.setValueBinding("value", createValueBinding(value));
            } else {
                parameter.setValue(value);
            }
        }
    }

    public void recycle() {
        super.recycle();
        this.name = null;
        this.value = null;
    }
}
