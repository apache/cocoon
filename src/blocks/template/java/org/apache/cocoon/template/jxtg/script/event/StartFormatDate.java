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
import java.util.Stack;
import java.util.TimeZone;

import org.apache.cocoon.template.jxtg.environment.ValueHelper;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

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

    public StartFormatDate(StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException{
        
        super(raw);

        Locator locator = getLocation();

        this.var = JXTExpression.compileExpr(attrs.getValue("var"), null, locator);
        this.value = JXTExpression.compileExpr(attrs.getValue("value"), null, locator);
        this.type = JXTExpression.compileExpr(attrs.getValue("type"), null, locator);
        this.pattern = JXTExpression.compileExpr(attrs.getValue("pattern"), null, locator);
        this.timeZone = JXTExpression.compileExpr(attrs.getValue("timeZone"), null, locator);
        this.dateStyle = JXTExpression.compileExpr(attrs.getValue("dateStyle"), null, locator);
        this.timeStyle = JXTExpression.compileExpr(attrs.getValue("timeStyle"), null, locator);
        this.locale = JXTExpression.compileExpr(attrs.getValue("locale"), null, locator);
    }

    public String format(JexlContext jexl, JXPathContext jxp) throws Exception {
        String var = this.var.getStringValue(jexl, jxp);
        Object value = this.value.getValue(jexl, jxp);
        Object locVal = this.locale.getValue(jexl, jxp);
        String pattern = this.pattern.getStringValue(jexl,
                jxp);
        Object timeZone = this.timeZone.getValue(jexl, jxp);

        String type = this.type.getStringValue(jexl, jxp);
        String timeStyle = this.timeStyle.getStringValue(jexl, jxp);
        String dateStyle = this.dateStyle.getStringValue(jexl, jxp);

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
