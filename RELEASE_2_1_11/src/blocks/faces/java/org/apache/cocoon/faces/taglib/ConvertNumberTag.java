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

import org.apache.cocoon.i18n.I18nUtils;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.convert.Converter;
import javax.faces.convert.NumberConverter;
import java.util.Locale;

/**
 * @version CVS $Id$
 */
public class ConvertNumberTag extends ConverterTag {
    private String currencyCode;
    private String currencySymbol;
    private String groupingUsed;
    private String integerOnly;
    private String maxFractionDigits;
    private String maxIntegerDigits;
    private String minFractionDigits;
    private String minIntegerDigits;
    private String locale;
    private String pattern;
    private String type;


    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public void setGroupingUsed(String groupingUsed) {
        this.groupingUsed = groupingUsed;
    }

    public void setIntegerOnly(String integerOnly) {
        this.integerOnly = integerOnly;
    }

    public void setMaxFractionDigits(String maxFractionDigits) {
        this.maxFractionDigits = maxFractionDigits;
    }

    public void setMaxIntegerDigits(String maxIntegerDigits) {
        this.maxIntegerDigits = maxIntegerDigits;
    }

    public void setMinFractionDigits(String minFractionDigits) {
        this.minFractionDigits = minFractionDigits;
    }

    public void setMinIntegerDigits(String minIntegerDigits) {
        this.minIntegerDigits = minIntegerDigits;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setType(String type) {
        this.type = type;
    }


    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        super.setConverterId("javax.faces.Number");
        return super.doStartTag(namespaceURI, localName, qName, atts);
    }

    protected Converter createConverter() {
        final UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        NumberConverter converter = (NumberConverter) super.createConverter();

        converter.setCurrencyCode((String) tag.evaluate(currencyCode));
        converter.setCurrencySymbol((String) tag.evaluate(currencySymbol));
        converter.setPattern((String) tag.evaluate(pattern));
        converter.setType((String) tag.evaluate(type));

        if (groupingUsed != null) {
            converter.setGroupingUsed(tag.evaluateBoolean(groupingUsed));
        }
        if (integerOnly != null) {
            converter.setIntegerOnly(tag.evaluateBoolean(integerOnly));
        }

        if (maxFractionDigits != null) {
            converter.setMaxFractionDigits(tag.evaluateInteger(maxFractionDigits));
        }
        if (maxIntegerDigits != null) {
            converter.setMaxIntegerDigits(tag.evaluateInteger(maxIntegerDigits));
        }
        if (minFractionDigits != null) {
            converter.setMinFractionDigits(tag.evaluateInteger(minFractionDigits));
        }
        if (minIntegerDigits != null) {
            converter.setMinIntegerDigits(tag.evaluateInteger(minIntegerDigits));
        }

        Locale l = null;
        if (locale != null) {
            if (FacesUtils.isExpression(locale)) {
                l = (Locale) tag.evaluate(locale);
            } else {
                l = I18nUtils.parseLocale(locale);
            }
        }
        converter.setLocale(l);

        return converter;
    }

    public void recycle() {
        super.recycle();
        currencyCode = null;
        currencySymbol = null;
        groupingUsed = null;
        integerOnly = null;
        maxFractionDigits = null;
        maxIntegerDigits = null;
        minFractionDigits = null;
        minIntegerDigits = null;
        locale = null;
        pattern = null;
        type = null;
    }
}
