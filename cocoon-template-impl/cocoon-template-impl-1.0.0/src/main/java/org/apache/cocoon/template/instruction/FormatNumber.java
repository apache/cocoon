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

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Stack;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class FormatNumber extends LocaleAwareInstruction {
    private Subst value;
    private Subst type;
    private Subst pattern;
    private Subst currencyCode;
    private Subst currencySymbol;
    private Subst isGroupingUsed;
    private Subst maxIntegerDigits;
    private Subst minIntegerDigits;
    private Subst maxFractionDigits;
    private Subst minFractionDigits;

    private Subst var;

    private static Class currencyClass;
    private static final String NUMBER = "number";
    private static final String CURRENCY = "currency";
    private static final String PERCENT = "percent";

    static {
        try {
            currencyClass = Class.forName("java.util.Currency");
            // container's runtime is J2SE 1.4 or greater
        } catch (Exception cnfe) {
            // EMPTY
        }
    }

    public FormatNumber(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException {
        super(parsingContext, raw, attrs, stack);

        Locator locator = getLocation();

        StringTemplateParser expressionCompiler = parsingContext.getStringTemplateParser();
        this.value = expressionCompiler.compileExpr(attrs.getValue("value"), null, locator);
        this.type = expressionCompiler.compileExpr(attrs.getValue("type"), null, locator);
        this.pattern = expressionCompiler.compileExpr(attrs.getValue("pattern"), null, locator);
        this.currencyCode =
            expressionCompiler.compileExpr(attrs.getValue("currencyCode"), null, locator);
        this.currencySymbol =
            expressionCompiler.compileExpr(attrs.getValue("currencySymbol"), null, locator);
        this.isGroupingUsed =
            expressionCompiler.compileBoolean(attrs.getValue("isGroupingUsed"), null, locator);
        this.maxIntegerDigits =
            expressionCompiler.compileInt(attrs.getValue("maxIntegerDigits"), null, locator);
        this.minIntegerDigits =
            expressionCompiler.compileInt(attrs.getValue("minIntegerDigits"), null, locator);
        this.maxFractionDigits =
            expressionCompiler.compileInt(attrs.getValue("maxFractionDigits"), null, locator);
        this.minFractionDigits =
            expressionCompiler.compileInt(attrs.getValue("minFractionDigits"), null, locator);
        this.var = expressionCompiler.compileExpr(attrs.getValue("var"), null, locator);
    }

    public Event execute(final XMLConsumer consumer,
                         ObjectModel objectModel, ExecutionContext executionContext,
                         MacroContext macroContext, NamespacesTable namespaces, Event startEvent, Event endEvent) 
        throws SAXException {
        try {
            String result = format(objectModel);
            if (result != null) {
                char[] chars = result.toCharArray();
                consumer.characters(chars, 0, chars.length);
            }
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(), getLocation(),
                                        new ErrorHolder(err));
        }
        return getNext();
    }

    private String format(ObjectModel objectModel) throws Exception {
        // Determine formatting locale
        String var = this.var == null ? null : this.var.getStringValue(objectModel);
        Number input = this.value.getNumberValue(objectModel);
        String type = this.type == null ? null : this.type.getStringValue(objectModel);
        String pattern = this.pattern == null ? null : this.pattern.getStringValue(objectModel);
        String currencyCode = this.currencyCode == null ? null : this.currencyCode.getStringValue(objectModel);
        String currencySymbol = this.currencySymbol == null ? null : this.currencySymbol.getStringValue(objectModel);
        Boolean isGroupingUsed = this.isGroupingUsed == null ? null : this.isGroupingUsed.getBooleanValue(objectModel);
        Number maxIntegerDigits = this.maxIntegerDigits == null ? null : this.maxIntegerDigits.getNumberValue(objectModel);
        Number minIntegerDigits = this.minIntegerDigits == null ? null : this.minIntegerDigits.getNumberValue(objectModel);
        Number maxFractionDigits = this.maxFractionDigits == null ? null : this.maxFractionDigits.getNumberValue(objectModel);
        Number minFractionDigits = this.minFractionDigits == null ? null : this.minFractionDigits.getNumberValue(objectModel);
        
        Locale loc = getLocale(objectModel);
        String formatted;
        if (loc != null) {
            // Create formatter
            NumberFormat formatter = null;
            if (StringUtils.isNotEmpty(pattern)) {
                // if 'pattern' is specified, 'type' is ignored
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
                formatter = new DecimalFormat(pattern, symbols);
            } else {
                formatter = createFormatter(loc, type);
            }
            if (StringUtils.isNotEmpty(pattern)
                    || CURRENCY.equalsIgnoreCase(type)) {
                setCurrency(formatter, currencyCode, currencySymbol);
            }
            configureFormatter(formatter, isGroupingUsed, maxIntegerDigits,
                    minIntegerDigits, maxFractionDigits, minFractionDigits);
            formatted = formatter.format(input);
        } else {
            // no formatting locale available, use toString()
            formatted = input.toString();
        }
        if (var != null) {
            objectModel.put(var, formatted);
            return null;
        }
        return formatted;
    }

    private NumberFormat createFormatter(Locale loc, String type)
            throws Exception {
        NumberFormat formatter = null;
        if ((type == null) || NUMBER.equalsIgnoreCase(type)) {
            formatter = NumberFormat.getNumberInstance(loc);
        } else if (CURRENCY.equalsIgnoreCase(type)) {
            formatter = NumberFormat.getCurrencyInstance(loc);
        } else if (PERCENT.equalsIgnoreCase(type)) {
            formatter = NumberFormat.getPercentInstance(loc);
        } else {
            throw new IllegalArgumentException("Invalid type: \"" + type
                    + "\": should be \"number\" or \"currency\" or \"percent\"");
        }
        return formatter;
    }

    /*
     * Applies the 'groupingUsed', 'maxIntegerDigits', 'minIntegerDigits',
     * 'maxFractionDigits', and 'minFractionDigits' attributes to the given
     * formatter.
     */
    private void configureFormatter(NumberFormat formatter,
            Boolean isGroupingUsed, Number maxIntegerDigits,
            Number minIntegerDigits, Number maxFractionDigits,
            Number minFractionDigits) {
        if (isGroupingUsed != null)
            formatter.setGroupingUsed(isGroupingUsed.booleanValue());
        if (maxIntegerDigits != null)
            formatter.setMaximumIntegerDigits(maxIntegerDigits.intValue());
        if (minIntegerDigits != null)
            formatter.setMinimumIntegerDigits(minIntegerDigits.intValue());
        if (maxFractionDigits != null)
            formatter.setMaximumFractionDigits(maxFractionDigits.intValue());
        if (minFractionDigits != null)
            formatter.setMinimumFractionDigits(minFractionDigits.intValue());
    }

    /*
     * Override the formatting locale's default currency symbol with the
     * specified currency code (specified via the "currencyCode" attribute) or
     * currency symbol (specified via the "currencySymbol" attribute).
     * 
     * If both "currencyCode" and "currencySymbol" are present, "currencyCode"
     * takes precedence over "currencySymbol" if the java.util.Currency class is
     * defined in the container's runtime (that is, if the container's runtime
     * is J2SE 1.4 or greater), and "currencySymbol" takes precendence over
     * "currencyCode" otherwise.
     * 
     * If only "currencyCode" is given, it is used as a currency symbol if
     * java.util.Currency is not defined.
     * 
     * Example:
     * 
     * JDK "currencyCode" "currencySymbol" Currency symbol being displayed
     * -----------------------------------------------------------------------
     * all --- --- Locale's default currency symbol
     * 
     * <1.4 EUR --- EUR >=1.4 EUR --- Locale's currency symbol for Euro
     * 
     * all --- \u20AC \u20AC
     * 
     * <1.4 EUR \u20AC \u20AC >=1.4 EUR \u20AC Locale's currency symbol for Euro
     */
    private void setCurrency(NumberFormat formatter, String currencyCode,
            String currencySymbol) throws Exception {
        String code = null;
        String symbol = null;

        if (currencyCode == null) {
            if (currencySymbol == null) {
                return;
            }
            symbol = currencySymbol;
        } else if (currencySymbol != null) {
            if (currencyClass != null) {
                code = currencyCode;
            } else {
                symbol = currencySymbol;
            }
        } else if (currencyClass != null) {
            code = currencyCode;
        } else {
            symbol = currencyCode;
        }
        if (code != null) {
            Object[] methodArgs = new Object[1];

            /*
             * java.util.Currency.getInstance()
             */
            Method m = currencyClass.getMethod("getInstance",
                    new Class[] { String.class });

            methodArgs[0] = code;
            Object currency = m.invoke(null, methodArgs);

            /*
             * java.text.NumberFormat.setCurrency()
             */
            Class[] paramTypes = new Class[1];
            paramTypes[0] = currencyClass;
            Class numberFormatClass = Class.forName("java.text.NumberFormat");
            m = numberFormatClass.getMethod("setCurrency", paramTypes);
            methodArgs[0] = currency;
            m.invoke(formatter, methodArgs);
        } else {
            /*
             * Let potential ClassCastException propagate up (will almost never
             * happen)
             */
            DecimalFormat df = (DecimalFormat) formatter;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setCurrencySymbol(symbol);
            df.setDecimalFormatSymbols(dfs);
        }
    }
}
