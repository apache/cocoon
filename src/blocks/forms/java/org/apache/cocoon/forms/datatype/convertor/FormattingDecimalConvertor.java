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
package org.apache.cocoon.forms.datatype.convertor;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.xml.AttributesImpl;

import com.ibm.icu.util.Currency;
import java.util.Locale;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.NumberFormat;
import java.text.ParseException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A Convertor for {@link BigDecimal}s backed by the
 * {@link java.text.DecimalFormat DecimalFormat} class.
 *
 * <p>It can be configured to use one of these variants: integer,
 * number, currency or percent.
 *
 * <p>Alternatively, a <strong>formatting pattern</strong> can be used. This can either be a locale-dependent
 * or locale-independent formatting pattern. When looking up a formatting pattern, a mechansim
 * similar to resource bundle lookup is used. Suppose the locale is nl-BE, then first a formatting
 * pattern for nl-BE will be sought, then one for nl, and if that is not
 * found, finally the locale-independent formatting pattern will be used.
 *
 * @version $Id$
 */
public class FormattingDecimalConvertor implements Convertor {
    private int variant;
    private Currency currency = Currency.getInstance(Locale.getDefault()); // lets get some consistency here.
    /** Locale-specific formatting patterns. */
    private LocaleMap localizedPatterns;
    /** Non-locale specific formatting pattern. */
    private String nonLocalizedPattern;
    
    public static final int INTEGER = 0;
    public static final int NUMBER = 1;
    public static final int CURRENCY = 2;
    public static final int PERCENT = 3;

    public FormattingDecimalConvertor() {
        this.variant = getDefaultVariant();
        this.localizedPatterns = new LocaleMap();
    }

    protected int getDefaultVariant() {
        return NUMBER;
    }

    public ConversionResult convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        
        // Some locales (e.g. "fr") produce non-breaking spaces sent back as space by the browser
        //value = value.replace(' ', (char)160); [JQ] This was breaking with the Dojo upgrade 
        DecimalFormat decimalFormat = getDecimalFormat(locale, formatCache);
        //decimalFormat.setParseBigDecimal(true);
        Number decimalValue;
        try {
            decimalValue = decimalFormat.parse(value);
        } catch (ParseException e) {
            System.out.println("Exception converting: " + value + " : " + e.getMessage());
            return ConversionResult.create("decimal");
        }

        if (decimalValue instanceof BigDecimal) {
            // no need for conversion
        } else if (decimalValue instanceof com.ibm.icu.math.BigDecimal) {
            // no need for conversion
            decimalValue = ((com.ibm.icu.math.BigDecimal)decimalValue).toBigDecimal();
        } else if (decimalValue instanceof Integer) {
            decimalValue = new BigDecimal(decimalValue.intValue());
        } else if (decimalValue instanceof Long) {
            decimalValue = new BigDecimal(decimalValue.longValue());
        } else if (decimalValue instanceof Double) {
            decimalValue = new BigDecimal(decimalValue.doubleValue());
        } else if (decimalValue instanceof BigInteger) {
            decimalValue = new BigDecimal((BigInteger)decimalValue);
        } else {
            return ConversionResult.create("decimal");
        }
        return new ConversionResult(decimalValue);
    }

    public String convertToString(Object value, Locale locale, Convertor.FormatCache formatCache) {
        DecimalFormat decimalFormat = getDecimalFormat(locale, formatCache);
        return decimalFormat.format(value);
    }

    protected final DecimalFormat getDecimalFormat(Locale locale, Convertor.FormatCache formatCache) {
        DecimalFormat decimalFormat = null;
        if (formatCache != null)
            decimalFormat = (DecimalFormat)formatCache.get();
        if (decimalFormat == null) {
            decimalFormat = getDecimalFormat(locale);
            if (formatCache != null)
                formatCache.store(decimalFormat);
        }
        return decimalFormat;
    }

    private DecimalFormat getDecimalFormat(Locale locale) {
        DecimalFormat decimalFormat = null;

        switch (variant) {
            case INTEGER:
                decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
                decimalFormat.setMaximumFractionDigits(0);
                decimalFormat.setDecimalSeparatorAlwaysShown(false);
                decimalFormat.setParseIntegerOnly(true);
                break;
            case NUMBER:
                decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(locale);
                break;
            case CURRENCY:
                decimalFormat = (DecimalFormat)NumberFormat.getCurrencyInstance(locale);
                decimalFormat.setCurrency(this.currency);
                int frac = this.currency.getDefaultFractionDigits();
                decimalFormat.setMinimumFractionDigits(frac);
                decimalFormat.setMaximumFractionDigits(frac);
                
                break;
            case PERCENT:
                decimalFormat = (DecimalFormat)NumberFormat.getPercentInstance(locale);
                break;
        }
        
        String pattern = (String)localizedPatterns.get(locale);

        if (pattern != null) {
            decimalFormat.applyPattern(pattern);
        } else if (nonLocalizedPattern != null) {
            decimalFormat.applyPattern(nonLocalizedPattern);
        }
        return decimalFormat;
    }

    public void setVariant(int variant) {
        if (variant != INTEGER && variant != NUMBER && variant != CURRENCY && variant != PERCENT)
            throw new IllegalArgumentException("Invalid value for variant parameter.");
        this.variant = variant;
    }

    /** 
     *   Set the currency of the Convertor
     *   @param currencyCode String the ISO 4217 code of the currency (http://www.iso.org/iso/support/faqs/faqs_widely_used_standards/widely_used_standards_other/currency_codes/currency_codes_list-1.htm)
     */
    public void setCurrency(String currencyCode) throws IllegalArgumentException, NullPointerException {
        Currency currency = Currency.getInstance(currencyCode);
        this.setCurrency(currency);
    }
    
    /** 
     *   Set the currency of the Convertor
     *   @param currency java.util.Currency the currency to display for this field
     */
    public void setCurrency(Currency currency) throws IllegalArgumentException {
        if (this.variant != CURRENCY)
            throw new IllegalArgumentException("Cannot set currency on this variant of convertor.");
        this.currency = currency;
    }

    /** 
     *   Get the L10N name of the Currency of this Convertor
     */
    public String getCurrencyName(Locale locale) throws IllegalArgumentException {
        if (this.variant != CURRENCY)
            throw new IllegalArgumentException("Cannot set currency on this variant of convertor.");
            
        return this.currency.getName(locale, Currency.LONG_NAME, new boolean[1]);
    }

    public void addFormattingPattern(Locale locale, String pattern) {
        localizedPatterns.put(locale, pattern);
    }

    public void setNonLocalizedPattern(String pattern) {
        this.nonLocalizedPattern = pattern;
    }

    public Class getTypeClass() {
        return java.math.BigDecimal.class;
    }

    private static final String CONVERTOR_EL = "convertor";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        String pattern = (String)localizedPatterns.get(locale);
        if (pattern == null) pattern = nonLocalizedPattern;
        String variantString = null; // we have to convert the variant back into a string
        String currencyCode = null; // there may be a currency code
        String currencySymbol = null; // there may be a currency symbol
        if (this.variant == INTEGER) variantString = "integer";
        if (this.variant == NUMBER) variantString = "number";
        if (this.variant == CURRENCY) { 
            variantString = "currency"; 
            currencyCode = this.currency.getCurrencyCode();
            currencySymbol = this.currency.getSymbol(locale);
        }
        if (this.variant == PERCENT) variantString = "percent";

        if (pattern != null || variantString != null) {
            AttributesImpl attrs = new AttributesImpl();
            if (pattern != null) attrs.addCDATAAttribute("pattern", pattern);
            if (variantString != null) attrs.addCDATAAttribute("variant", variantString);
            if (currencyCode != null) attrs.addCDATAAttribute("currency", currencyCode);
            if (currencySymbol != null) attrs.addCDATAAttribute("symbol", currencySymbol);
            contentHandler.startElement(FormsConstants.INSTANCE_NS, CONVERTOR_EL, FormsConstants.INSTANCE_PREFIX_COLON + CONVERTOR_EL, attrs);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, CONVERTOR_EL, FormsConstants.INSTANCE_PREFIX_COLON + CONVERTOR_EL);
        }
    }
}
