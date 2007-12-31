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
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ValueChangeEvent;

/**
 * @version CVS $Id$
 */
public class InputHiddenTag extends UIComponentTag {

    private String converter;
    private String immediate;
    private String required;
    private String validator;
    private String value;
    private String valueChangeListener;


    public void setConverter(String converter) {
        this.converter = converter;
    }

    public void setImmediate(String immediate) {
        this.immediate = immediate;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValueChangeListener(String valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }


    public String getRendererType() {
        return "javax.faces.Hidden";
    }

    public String getComponentType() {
        return "javax.faces.HtmlInputHidden";
    }


    protected void setProperties(UIComponent component) {
        super.setProperties(component);

        UIInput input = null;
        try {
            input = (UIInput) component;
        } catch (ClassCastException cce) {
            throw new FacesException("Tag <" + getClass().getName() + "> expected UIInput. " +
                                     "Got <" + component.getClass().getName() + ">");
        }

        if (converter != null) {
            if (FacesUtils.isExpression(converter)) {
                input.setValueBinding("converter", createValueBinding(converter));
            } else {
                input.setConverter(getApplication().createConverter(converter));
            }
        }

        if (immediate != null) {
            if (FacesUtils.isExpression(immediate)) {
                input.setValueBinding("immediate", createValueBinding(immediate));
            } else {
                input.setImmediate(BooleanUtils.toBoolean(immediate));
            }
        }

        if (required != null) {
            if (FacesUtils.isExpression(required)) {
                input.setValueBinding("required", createValueBinding(required));
            } else {
                input.setRequired(BooleanUtils.toBoolean(required));
            }
        }

        if (validator != null) {
            if (FacesUtils.isExpression(validator)) {
                MethodBinding vb = getApplication().createMethodBinding(validator,
                                                                        new Class[]{ FacesContext.class, UIComponent.class, Object.class });
                input.setValidator(vb);
            } else {
                throw new FacesException("Tag <" + getClass().getName() + "> validator must be an expression. " +
                                         "Got <" + validator + ">");
            }
        }

        if (value != null) {
            if (FacesUtils.isExpression(value)) {
                input.setValueBinding("value", createValueBinding(value));
            } else {
                input.setValue(value);
            }
        }

        if (valueChangeListener != null) {
            if (FacesUtils.isExpression(valueChangeListener)) {
                MethodBinding vb = getApplication().createMethodBinding(valueChangeListener,
                                                                        new Class[]{ ValueChangeEvent.class });
                input.setValueChangeListener(vb);
            } else {
                throw new FacesException("Tag <" + getClass().getName() + "> valueChangeListener must be an expression. " +
                                         "Got <" + valueChangeListener + ">");
            }
        }
    }

    public void recycle() {
        super.recycle();
        converter = null;
        immediate = null;
        required = null;
        validator = null;
        value = null;
        valueChangeListener = null;
    }
}
