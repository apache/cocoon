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
import javax.faces.convert.DateTimeConverter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @version CVS $Id$
 */
public class ConvertDateTimeTag extends ConverterTag {
    private String dateStyle;
    private String locale;
    private String pattern;
    private String timeStyle;
    private String timeZone;
    private String type;

    public void setDateStyle(String dateStyle) {
        this.dateStyle = dateStyle;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setTimeStyle(String timeStyle) {
        this.timeStyle = timeStyle;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void setType(String type) {
        this.type = type;
    }


    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        setConverterId("javax.faces.DateTime");
        return super.doStartTag(namespaceURI, localName, qName, atts);
    }

    protected Converter createConverter() {
        final UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        DateTimeConverter converter = (DateTimeConverter) super.createConverter();

        converter.setPattern((String) tag.evaluate(pattern));

        String ds = dateStyle == null? "default": (String) tag.evaluate(dateStyle);
        String ts = timeStyle == null? "default": (String) tag.evaluate(timeStyle);
        converter.setDateStyle(ds);
        converter.setTimeStyle(ts);

        String t = null;
        if (type != null) {
            t = (String) tag.evaluate(type);
        } else if (timeStyle != null) {
            t = dateStyle != null? "both" : "time";
        } else {
            t = "date";
        }
        converter.setType(t);

        Locale l = null;
        if (locale != null) {
            if (FacesUtils.isExpression(locale)) {
                l = (Locale) tag.evaluate(locale);
            } else {
                l = I18nUtils.parseLocale(locale);
            }
        }
        converter.setLocale(l);

        TimeZone tz = null;
        if (timeZone != null) {
            if (FacesUtils.isExpression(timeZone)) {
                tz = (TimeZone) tag.evaluate(timeZone);
            }  else {
                tz = TimeZone.getTimeZone(timeZone);
            }
        }
        converter.setTimeZone(tz);

        return converter;
    }

    public void recycle() {
        super.recycle();
        this.dateStyle = null;
        this.locale = null;
        this.pattern = null;
        this.timeStyle = null;
        this.timeZone = null;
        this.type = null;
    }
}
