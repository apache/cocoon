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
package org.apache.cocoon.template.jxtg.script.event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.cocoon.template.jxtg.environment.ValueHelper;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jxpath.JXPathContext;

public class StartFormatDate extends StartInstruction {

    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String DATETIME = "both";

    JXTExpression var;
    JXTExpression value;
    JXTExpression type;
    JXTExpression pattern;
    JXTExpression timeZone;
    JXTExpression dateStyle;
    JXTExpression timeStyle;
    JXTExpression locale;

    public StartFormatDate(StartElement raw, JXTExpression var,
            JXTExpression value, JXTExpression type, JXTExpression pattern,
            JXTExpression timeZone, JXTExpression dateStyle,
            JXTExpression timeStyle, JXTExpression locale) {
        super(raw);
        this.var = var;
        this.value = value;
        this.type = type;
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.dateStyle = dateStyle;
        this.timeStyle = timeStyle;
        this.locale = locale;
    }

    public String format(JexlContext jexl, JXPathContext jxp) throws Exception {
        String var = ValueHelper.getStringValue(this.var, jexl, jxp);
        Object value = ValueHelper.getValue(this.value, jexl, jxp);
        Object locVal = ValueHelper.getValue(this.locale, jexl, jxp);
        String pattern = ValueHelper.getStringValue(this.pattern, jexl,
                jxp);
        Object timeZone = ValueHelper
                .getValue(this.timeZone, jexl, jxp);

        String type = ValueHelper.getStringValue(this.type, jexl, jxp);
        String timeStyle = ValueHelper.getStringValue(this.timeStyle,
                jexl, jxp);
        String dateStyle = ValueHelper.getStringValue(this.dateStyle,
                jexl, jxp);

        String formatted = null;

        // Create formatter
        Locale locale;
        if (locVal != null) {
            locale = locVal instanceof Locale ? (Locale) locVal
                    : ValueHelper.parseLocale(locVal.toString(), null);
        } else {
            locale = Locale.getDefault();
        }
        DateFormat formatter = createFormatter(locale, type, dateStyle,
                timeStyle);
        // Apply pattern, if present
        if (pattern != null) {
            try {
                ((SimpleDateFormat) formatter).applyPattern(pattern);
            } catch (ClassCastException cce) {
                formatter = new SimpleDateFormat(pattern, locale);
            }
        }
        // Set time zone
        TimeZone tz = null;
        if ((timeZone instanceof String) && ((String) timeZone).equals("")) {
            timeZone = null;
        }
        if (timeZone != null) {
            if (timeZone instanceof String) {
                tz = TimeZone.getTimeZone((String) timeZone);
            } else if (timeZone instanceof TimeZone) {
                tz = (TimeZone) timeZone;
            } else {
                throw new IllegalArgumentException("Illegal timeZone value: \""
                        + timeZone + "\"");
            }
        }
        if (tz != null) {
            formatter.setTimeZone(tz);
        }
        formatted = formatter.format(value);
        if (var != null) {
            jexl.getVars().put(var, formatted);
            jxp.getVariables().declareVariable(var, formatted);
            return null;
        }
        return formatted;
    }

    private DateFormat createFormatter(Locale loc, String type,
            String dateStyle, String timeStyle) throws Exception {
        DateFormat formatter = null;
        if ((type == null) || DATE.equalsIgnoreCase(type)) {
            formatter = DateFormat.getDateInstance(getStyle(dateStyle), loc);
        } else if (TIME.equalsIgnoreCase(type)) {
            formatter = DateFormat.getTimeInstance(getStyle(timeStyle), loc);
        } else if (DATETIME.equalsIgnoreCase(type)) {
            formatter = DateFormat.getDateTimeInstance(getStyle(dateStyle),
                    getStyle(timeStyle), loc);
        } else {
            throw new IllegalArgumentException("Invalid type: \"" + type + "\"");
        }
        return formatter;
    }

    private static final String DEFAULT = "default";
    private static final String SHORT = "short";
    private static final String MEDIUM = "medium";
    private static final String LONG = "long";
    private static final String FULL = "full";

    private int getStyle(String style) {
        int ret = DateFormat.DEFAULT;
        if (style != null) {
            if (DEFAULT.equalsIgnoreCase(style)) {
                ret = DateFormat.DEFAULT;
            } else if (SHORT.equalsIgnoreCase(style)) {
                ret = DateFormat.SHORT;
            } else if (MEDIUM.equalsIgnoreCase(style)) {
                ret = DateFormat.MEDIUM;
            } else if (LONG.equalsIgnoreCase(style)) {
                ret = DateFormat.LONG;
            } else if (FULL.equalsIgnoreCase(style)) {
                ret = DateFormat.FULL;
            } else {
                throw new IllegalArgumentException(
                        "Invalid style: \""
                                + style
                                + "\": should be \"default\" or \"short\" or \"medium\" or \"long\" or \"full\"");
            }
        }
        return ret;
    }
}