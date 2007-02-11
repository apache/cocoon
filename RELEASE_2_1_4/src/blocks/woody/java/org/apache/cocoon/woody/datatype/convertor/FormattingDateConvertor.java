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

import org.outerj.i18n.DateFormat;
import org.outerj.i18n.I18nSupport;

import java.util.Locale;
import java.util.Date;
import java.text.ParseException;

/**
 * A Convertor for {@link java.util.Date Date} objects backed by the
 * {@link java.text.SimpleDateFormat SimpleDateFormat} class.
 *
 * <p>It can be configured to use one of three <strong>variants</strong>: date,
 * time or datetime and one of four <strong>styles</strong>: long, full, medium or short.
 *
 * <p>Alternatively, a <strong>formatting pattern</strong> can be used. This can either be a locale-dependent
 * or locale-independent formatting pattern. When looking up a formatting pattern, a mechansim
 * similar to resource bundle lookup is used. Suppose the locale is nl-BE, then first a formatting
 * pattern for nl-BE will be sought, then one for nl, and if that is not
 * found, finally the locale-independent formatting pattern will be used.
 *
 * <p><strong>NOTE:</strong> the earlier statement about the fact that this class uses java.text.SimpleDateFormat
 * is not entirely correct. In fact, it uses a small wrapper class that will either delegate to
 * java.text.SimpleDateFormat or com.ibm.icu.text.SimpleDateFormat. The com.ibm version will automatically
 * be used if it is present on the classpath, otherwise the java.text version will be used.
 *
 * @version CVS $Id: FormattingDateConvertor.java,v 1.3 2003/12/31 17:15:46 vgritsenko Exp $
 */
public class FormattingDateConvertor implements Convertor {
    /** See {@link #setStyle}. */
    private int style;
    /** See {@link #setVariant}. */
    private int variant;
    /** Locale-specific formatting patterns. */
    private LocaleMap localizedPatterns;
    /** Non-locale specific formatting pattern. */
    private String nonLocalizedPattern;

    public static final int DATE = 1;
    public static final int TIME = 2;
    public static final int DATE_TIME = 3;

    public FormattingDateConvertor() {
        this.style = java.text.DateFormat.SHORT;
        this.variant = DATE;
        this.localizedPatterns = new LocaleMap();
    }

    public Object convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        DateFormat dateFormat = getDateFormat(locale, formatCache);
        try {
            return dateFormat.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }

    public String convertToString(Object value, Locale locale, Convertor.FormatCache formatCache) {
        DateFormat dateFormat = getDateFormat(locale, formatCache);
        return dateFormat.format((Date)value);
    }

    private final DateFormat getDateFormat(Locale locale, Convertor.FormatCache formatCache) {
        DateFormat dateFormat = null;
        if (formatCache != null)
            dateFormat = (DateFormat)formatCache.get();
        if (dateFormat == null) {
            dateFormat = getDateFormat(locale);
            if (formatCache != null)
                formatCache.store(dateFormat);
        }
        return dateFormat;
    }

    protected DateFormat getDateFormat(Locale locale) {
        DateFormat dateFormat = null;

        switch (variant) {
            case DATE:
                dateFormat = I18nSupport.getInstance().getDateFormat(style, locale);
                break;
            case TIME:
                dateFormat = I18nSupport.getInstance().getTimeFormat(style, locale);
                break;
            case DATE_TIME:
                dateFormat = I18nSupport.getInstance().getDateTimeFormat(style, style, locale);
                break;
        }

        String pattern = (String)localizedPatterns.get(locale);

        if (pattern != null)
            dateFormat.applyLocalizedPattern(pattern);
        else if (nonLocalizedPattern != null)
            dateFormat.applyPattern(nonLocalizedPattern);

        return dateFormat;
    }

    public Class getTypeClass() {
        return Date.class;
    }

    /**
     *
     * @param style one of the constants FULL, LONG, MEDIUM or SHORT defined in the {@link Date} class.
     */
    public void setStyle(int style) {
        this.style = style;
    }

    public void setVariant(int variant) {
        if (variant != DATE && variant != TIME && variant != DATE_TIME)
            throw new IllegalArgumentException("Invalid value for variant parameter.");
        this.variant = variant;
    }

    public void addFormattingPattern(Locale locale, String pattern) {
        localizedPatterns.put(locale, pattern);
    }

    public void setNonLocalizedPattern(String pattern) {
        this.nonLocalizedPattern = pattern;
    }
}
