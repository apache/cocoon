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
package org.apache.cocoon.template.instruction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.JXTExpression;
import org.apache.cocoon.template.expression.StringTemplateParser;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class FormatDate extends LocaleAwareInstruction {
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String DATETIME = "both";

    private JXTExpression var;
    private JXTExpression value;
    private JXTExpression type;
    private JXTExpression pattern;
    private JXTExpression timeZone;
    private JXTExpression dateStyle;
    private JXTExpression timeStyle;
    public FormatDate(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException{
        super(parsingContext, raw, attrs, stack);

        Locator locator = getLocation();

        StringTemplateParser expressionCompiler = parsingContext.getStringTemplateParser();
        this.var = expressionCompiler.compileExpr(attrs.getValue("var"), null, locator);
        this.value = expressionCompiler.compileExpr(attrs.getValue("value"), null, locator);
        this.type = expressionCompiler.compileExpr(attrs.getValue("type"), null, locator);
        this.pattern = expressionCompiler.compileExpr(attrs.getValue("pattern"), null, locator);
        this.timeZone = expressionCompiler.compileExpr(attrs.getValue("timeZone"), null, locator);
        this.dateStyle = expressionCompiler.compileExpr(attrs.getValue("dateStyle"), null, locator);
        this.timeStyle = expressionCompiler.compileExpr(attrs.getValue("timeStyle"), null, locator);
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         MacroContext macroContext, Event startEvent, Event endEvent) 
        throws SAXException {
        try {
            String result = format(expressionContext);
            if (result != null) {
                char[] chars = result.toCharArray();
                consumer.characters(chars, 0, chars.length);
            }
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(), getLocation(), new ErrorHolder(err));
        }
        return getNext();
    }

    private String format(ExpressionContext expressionContext) throws Exception {
        String var = this.var == null ? null : this.var.getStringValue(expressionContext);
        Object value = this.value == null ? null : this.value.getValue(expressionContext);

        String pattern = this.pattern == null ? null : this.pattern.getStringValue(expressionContext);
        Object timeZone = this.timeZone == null ? null : this.timeZone.getValue(expressionContext);

        String type = this.type == null ? null : this.type.getStringValue(expressionContext);
        String timeStyle = this.timeStyle == null ? null : this.timeStyle.getStringValue(expressionContext);
        String dateStyle = this.dateStyle == null ? null : this.dateStyle.getStringValue(expressionContext);

        String formatted = null;

        // Create formatter
        Locale locale = getLocale( expressionContext );
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
            expressionContext.put(var, formatted);
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
