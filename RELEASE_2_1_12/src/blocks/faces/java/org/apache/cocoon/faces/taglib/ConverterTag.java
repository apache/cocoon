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

import org.apache.cocoon.taglib.TagSupport;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

/**
 * Base class for converter tags.
 * Can be extended to implement custom converters.
 *
 * @version CVS $Id$
 */
public class ConverterTag extends TagSupport {

    private String converterId;

    public String getConverterId() {
        return this.converterId;
    }

    public void setConverterId(String converterId) {
        this.converterId = converterId;
    }

    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {

        UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        if (tag == null) {
            throw new SAXException("Tag " + getClass().getName() + " have to be nested in a UIComponentTag");
        }

        if (!tag.getCreated()) {
            return 0;
        }

        Converter converter = createConverter();

        ValueHolder vh = (ValueHolder) tag.getComponentInstance();
        vh.setConverter(converter);
        Object localValue = vh.getLocalValue();
        if (localValue instanceof String) {
            try {
                localValue = converter.getAsObject(tag.getFacesContext(), (UIComponent) vh, (String) localValue);
                vh.setValue(localValue);
            } catch (ConverterException ce) {
            }
        }

        return SKIP_BODY;
    }

    /**
     * Override to create custom validator
     */
    protected Converter createConverter() {
        final UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        String converterIdVal = (String) tag.evaluate(converterId);
        return tag.getApplication().createConverter(converterIdVal);
    }

    public void recycle() {
        super.recycle();
        this.converterId = null;
    }
}
