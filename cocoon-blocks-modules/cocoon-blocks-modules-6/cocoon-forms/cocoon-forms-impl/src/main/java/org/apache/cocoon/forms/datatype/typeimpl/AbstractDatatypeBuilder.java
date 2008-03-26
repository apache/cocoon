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
package org.apache.cocoon.forms.datatype.typeimpl;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.DatatypeBuilder;
import org.apache.cocoon.forms.datatype.DatatypeManager;
import org.apache.cocoon.forms.datatype.ValidationRule;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.ConvertorBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Abstract base class for datatype builders, most concrete datatype builders
 * will derive from this class.
 * @version $Id$
 */
public abstract class AbstractDatatypeBuilder implements DatatypeBuilder {
    private Map convertorBuilders;
    private String defaultConvertorName;
    private String plainConvertorName;

    public void buildConvertor(Element datatypeEl, AbstractDatatype datatype) throws Exception {
        Element convertorEl = DomHelper.getChildElement(datatypeEl, FormsConstants.DEFINITION_NS, "convertor", false);
        Convertor convertor = buildConvertor(convertorEl);
        datatype.setConvertor(convertor);
    }

    public Convertor buildConvertor(Element convertorEl) throws Exception {
        String type = null;
        // convertor configuration is allowed to be null, so check that it is not null
        if (convertorEl != null)
            type = convertorEl.getAttribute("type");
        if (type == null || type.length() == 0)
            type = defaultConvertorName;
        ConvertorBuilder convertorBuilder = (ConvertorBuilder)convertorBuilders.get(type);
        if (convertorBuilder == null) {
            throw new IllegalArgumentException("Undefined ConvertorBuild: " + type);
        }
        return convertorBuilder.build(convertorEl);
    }

    public Convertor getPlainConvertor() {
        return (Convertor)convertorBuilders.get(plainConvertorName);
    }

    protected void buildValidationRules(Element datatypeElement, AbstractDatatype datatype, DatatypeManager datatypeManager) throws Exception {
        Element validationElement = DomHelper.getChildElement(datatypeElement, FormsConstants.DEFINITION_NS, "validation");
        if (validationElement != null) {
            Element[] validationElements = DomHelper.getChildElements(validationElement, FormsConstants.DEFINITION_NS);
            for (int i = 0; i < validationElements.length; i++) {
                ValidationRule rule = datatypeManager.createValidationRule(validationElements[i]);
                if (rule.supportsType(datatype.getTypeClass(), datatype.isArrayType())) {
                    datatype.addValidationRule(rule);
                } else {
                    throw new Exception("Validation rule \"" + validationElements[i].getLocalName() + "\" cannot be used with type <" + (datatype.isArrayType() ? "array of " : "") + datatype.getTypeClass().getName() + ">, error at " + DomHelper.getLocation(validationElements[i]));
                }
            }
        }
    }

    public void setConvertorBuilders( Map convertorBuilders )
    {
        this.convertorBuilders = convertorBuilders;
    }

    public void setDefaultConvertorName( String defaultConvertorName )
    {
        this.defaultConvertorName = defaultConvertorName;
    }

    public void setPlainConvertorName( String plainConvertorName )
    {
        this.plainConvertorName = plainConvertorName;
    }
}
