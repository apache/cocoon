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

import org.apache.cocoon.template.jxtg.environment.ValueHelper;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;

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

    public StartFormatNumber(StartElement raw, JXTExpression var,
            JXTExpression value, JXTExpression type, JXTExpression pattern,
            JXTExpression currencyCode, JXTExpression currencySymbol,
            JXTExpression isGroupingUsed, JXTExpression maxIntegerDigits,
            JXTExpression minIntegerDigits, JXTExpression maxFractionDigits,
            JXTExpression minFractionDigits, JXTExpression locale) {
        super(raw);
        this.var = var;
        this.value = value;
        this.type = type;
        this.pattern = pattern;
        this.currencyCode = currencyCode;
        this.currencySymbol = currencySymbol;
        this.isGroupingUsed = isGroupingUsed;
        this.maxIntegerDigits = maxIntegerDigits;
        this.minIntegerDigits = minIntegerDigits;
        this.maxFractionDigits = maxFractionDigits;
        this.minFractionDigits = minFractionDigits;
        this.locale = locale;
    }

    public String format(JexlContext jexl, JXPathContext jxp) throws Exception {
        // Determine formatting locale
        String var = ValueHelper.getStringValue(this.var, jexl, jxp);
        Number input = ValueHelper
                .getNumberValue(this.value, jexl, jxp);
        String type = ValueHelper.getStringValue(this.type, jexl, jxp);
        String pattern = ValueHelper.getStringValue(this.pattern, jexl,
                jxp);
        String currencyCode = ValueHelper.getStringValue(
                this.currencyCode, jexl, jxp);
        String currencySymbol = ValueHelper.getStringValue(
                this.currencySymbol, jexl, jxp);
        Boolean isGroupingUsed = ValueHelper.getBooleanValue(
                this.isGroupingUsed, jexl, jxp);
        Number maxIntegerDigits = ValueHelper.getNumberValue(
                this.maxIntegerDigits, jexl, jxp);
        Number minIntegerDigits = ValueHelper.getNumberValue(
                this.minIntegerDigits, jexl, jxp);
        Number maxFractionDigits = ValueHelper.getNumberValue(
                this.maxFractionDigits, jexl, jxp);
        Number minFractionDigits = ValueHelper.getNumberValue(
                this.minFractionDigits, jexl, jxp);
        String localeStr = ValueHelper.getStringValue(this.locale,
                jexl, jxp);
        Locale loc = localeStr != null ? ValueHelper.parseLocale(
                localeStr, null) : Locale.getDefault();
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