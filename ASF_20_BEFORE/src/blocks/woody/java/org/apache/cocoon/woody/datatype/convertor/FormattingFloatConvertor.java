/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.datatype.convertor;

import org.outerj.i18n.I18nSupport;
import org.outerj.i18n.DecimalFormat;

import java.util.Locale;
import java.text.ParseException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A Convertor for {@link Float}s backed by the
 * {@link java.text.NumberFormat NumberFormat} class.
 *
 * <p>A <strong>formatting pattern</strong> can be used. This can either be a locale-dependent
 * or locale-independent formatting pattern. When looking up a formatting pattern, a mechansim
 * similar to resource bundle lookup is used. Suppose the locale is nl-BE, then first a formatting
 * pattern for nl-BE will be sought, then one for nl, and if that is not
 * found, finally the locale-independent formatting pattern will be used.
 *
 * <p>Note: the earlier statement about the fact that this class uses java.text.DecimalFormat
 * is not entirely correct. In fact, it uses a small wrapper class that will either delegate to
 * java.text.DecimalFormat or com.ibm.icu.text.DecimalFormat. The com.ibm version will automatically
 * be used if it is present on the classpath, otherwise the java.text version will be used.
 *
 * @version CVS $Id: FormattingFloatConvertor.java,v 1.1 2004/01/04 04:37:37 vgritsenko Exp $
 */
public class FormattingFloatConvertor implements Convertor {
    private int variant;
    /** Locale-specific formatting patterns. */
    private LocaleMap localizedPatterns;
    /** Non-locale specific formatting pattern. */
    private String nonLocalizedPattern;

    public static final int INTEGER = 0;
    public static final int NUMBER = 1;
    public static final int CURRENCY = 2;
    public static final int PERCENT = 3;

    public FormattingFloatConvertor() {
        this.variant = getDefaultVariant();
        this.localizedPatterns = new LocaleMap();
    }

    protected int getDefaultVariant() {
        return NUMBER;
    }

    public Object convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        DecimalFormat decimalFormat = getDecimalFormat(locale, formatCache);
        try {
            Number decimalValue = decimalFormat.parse(value);
            if (decimalValue instanceof BigDecimal)
                ;
			else if (decimalValue instanceof Integer)
				decimalValue = new BigDecimal(decimalValue .intValue());
            else if (decimalValue instanceof Long)
                decimalValue = new BigDecimal(decimalValue.longValue());
            else if (decimalValue instanceof Double)
                decimalValue = new BigDecimal(decimalValue.doubleValue());
            else if (decimalValue instanceof BigInteger)
                decimalValue = new BigDecimal((BigInteger)decimalValue);
            else
                return null;

            return decimalValue;
        } catch (ParseException e) {
            return null;
        }
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
                decimalFormat = I18nSupport.getInstance().getIntegerFormat(locale);
                break;
            case NUMBER:
                decimalFormat = I18nSupport.getInstance().getNumberFormat(locale);
                break;
            case CURRENCY:
                decimalFormat = I18nSupport.getInstance().getCurrencyFormat(locale);
                break;
            case PERCENT:
                decimalFormat = I18nSupport.getInstance().getPercentFormat(locale);
                break;
        }

        String pattern = (String)localizedPatterns.get(locale);

        if (pattern != null)
            decimalFormat.applyLocalizedPattern(pattern);
        else if (nonLocalizedPattern != null)
            decimalFormat.applyPattern(nonLocalizedPattern);

        return decimalFormat;
    }

    public void setVariant(int variant) {
        if (variant != INTEGER && variant != NUMBER && variant != CURRENCY && variant != PERCENT)
            throw new IllegalArgumentException("Invalid value for variant parameter.");
        this.variant = variant;
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
}
