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
package org.apache.cocoon.forms.datatype.typeimpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.DatatypeBuilder;
import org.apache.cocoon.forms.datatype.ValidationRule;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.xml.AttributesImpl;
import org.outerj.expression.ExpressionContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Abstract base class for Datatype implementations. Most concreate datatypes
 * will derive from this class.
 * @version $Id: AbstractDatatype.java,v 1.4 2004/04/10 13:40:27 bruno Exp $
 */
public abstract class AbstractDatatype implements Datatype {
    private List validationRules = new ArrayList();
    private boolean arrayType = false;
    private DatatypeBuilder builder;
    private Convertor convertor;

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        Iterator validationRulesIt = validationRules.iterator();
        while (validationRulesIt.hasNext()) {
            ValidationRule validationRule = (ValidationRule)validationRulesIt.next();
            ValidationError result = validationRule.validate(value, expressionContext);
            if (result != null)
                return result;
        }
        return null;
    }

    public void addValidationRule(ValidationRule validationRule) {
        validationRules.add(validationRule);
    }

    public boolean isArrayType() {
        return arrayType;
    }

    protected void setArrayType(boolean arrayType) {
        this.arrayType = arrayType;
    }

    protected void setConvertor(Convertor convertor) {
        this.convertor = convertor;
    }

    protected void setBuilder(DatatypeBuilder builder) {
        this.builder = builder;
    }

    public Convertor getPlainConvertor() {
        return builder.getPlainConvertor();
    }

    public DatatypeBuilder getBuilder() {
        return builder;
    }

    public Convertor getConvertor() {
        return convertor;
    }

    public Object convertFromString(String value, Locale locale) {
        return getConvertor().convertFromString(value, locale, null);
    }

    public String convertToString(Object value, Locale locale) {
        return getConvertor().convertToString(value, locale, null);
    }

    private static final String DATATYPE_EL = "datatype";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl attrs = new AttributesImpl();
        attrs.addCDATAAttribute("type", getDescriptiveName());
        contentHandler.startElement(Constants.INSTANCE_NS, DATATYPE_EL, Constants.INSTANCE_PREFIX_COLON + DATATYPE_EL, attrs);
        getConvertor().generateSaxFragment(contentHandler, locale);
        contentHandler.endElement(Constants.INSTANCE_NS, DATATYPE_EL, Constants.INSTANCE_PREFIX_COLON + DATATYPE_EL);
    }
}
