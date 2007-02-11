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
package org.apache.cocoon.taglib.i18n;

import java.util.Locale;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.taglib.TagSupport;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: LocaleTag.java,v 1.3 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class LocaleTag extends TagSupport {
    private Locale locale;
    private String language;
    private String country;
    private String variant;

    public Locale getLocale() {
        if (locale == null) {
            locale = createLocale();
        }
        return locale;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    /**
     *  Provides a key to retrieve a locale via findAttribute()
     */
    public void setLocaleRef(String value) {
        this.locale = (Locale) findAttribute(value);
    }

    protected Locale createLocale() {
        Locale locale = null;

        if (language == null) {
            locale = ObjectModelHelper.getRequest(objectModel).getLocale();
        } else if (country == null) {
            locale = new Locale(language, "");
        } else if (variant == null) {
            locale = new Locale(language, country);
        } else {
            locale = new Locale(language, country, variant);
        }

        return locale;
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        locale = null;
        language = null;
        country = null;
        variant = null;
        super.recycle();
    }

}
