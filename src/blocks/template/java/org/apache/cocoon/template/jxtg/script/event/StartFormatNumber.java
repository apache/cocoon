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

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Stack;

import org.apache.cocoon.template.jxtg.environment.ValueHelper;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class StartFormatNumber extends StartInstruction {

    JXTExpression value;
    JXTExpression type;
    JXTExpression pattern;
    JXTExpression currencyCode;
    JXTExpression currencySymbol;
    JXTExpression isGroupingUsed;
    JXTExpression maxIntegerDigits;
    JXTExpression minIntegerDigits;
    JXTExpression maxFractionDigits;
    JXTExpression minFractionDigits;
    JXTExpression locale;

    JXTExpression var;

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

    public StartFormatNumber(StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException {

        super(raw);

        Locator locator = getLocation();

        this.value = JXTExpression.compileExpr(attrs.getValue("value"), null, locator);
        this.type = JXTExpression.compileExpr(attrs.getValue("type"), null, locator);
        this.pattern = JXTExpression.compileExpr(attrs.getValue("pattern"), null, locator);
        this.currencyCode =
            JXTExpression.compileExpr(attrs.getValue("currencyCode"), null, locator);
        this.currencySymbol =
            JXTExpression.compileExpr(attrs.getValue("currencySymbol"), null, locator);
        this.isGroupingUsed =
            JXTExpression.compileBoolean(attrs.getValue("isGroupingUsed"), null, locator);
        this.maxIntegerDigits =
            JXTExpression.compileInt(attrs.getValue("maxIntegerDigits"), null, locator);
        this.minIntegerDigits =
            JXTExpression.compileInt(attrs.getValue("minIntegerDigits"), null, locator);
        this.maxFractionDigits =
            JXTExpression.compileInt(attrs.getValue("maxFractionDigits"), null, locator);
        this.minFractionDigits =
            JXTExpression.compileInt(attrs.getValue("minFractionDigits"), null, locator);
        this.locale = JXTExpression.compileExpr(attrs.getValue("locale"), null, locator);
        this.var = JXTExpression.compileExpr(attrs.getValue("var"), null, locator);
    }

    public String format(JexlContext jexl, JXPathContext jxp) throws Exception {
        // Determine formatting locale
        String var = this.var.getStringValue(jexl, jxp);
        Number input = this.value.getNumberValue(jexl, jxp);
        String type = this.type.getStringValue(jexl, jxp);
        String pattern = this.pattern.getStringValue(jexl, jxp);
        String currencyCode = this.currencyCode.getStringValue(jexl, jxp);
        String currencySymbol = this.currencySymbol.getStringValue(jexl, jxp);
        Boolean isGroupingUsed = this.isGroupingUsed.getBooleanValue(jexl, jxp);
        Number maxIntegerDigits = this.maxIntegerDigits.getNumberValue(jexl, jxp);
        Number minIntegerDigits = this.minIntegerDigits.getNumberValue(jexl, jxp);
        Number maxFractionDigits = this.maxFractionDigits.getNumberValue(jexl, jxp);
        Number minFractionDigits = this.minFractionDigits.getNumberValue(jexl, jxp);
        String localeStr = this.locale.getStringValue(jexl, jxp);
        Locale loc = localeStr != null
            ? ValueHelper.parseLocale(localeStr, null)
            : Locale.getDefault();
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
            jexl.getVars().put(var, formatted);
            jxp.getVariables().declareVariable(var, formatted);
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
