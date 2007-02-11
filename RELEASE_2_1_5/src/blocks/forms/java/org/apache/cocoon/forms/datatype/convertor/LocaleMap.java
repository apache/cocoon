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
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.commons.collections.FastHashMap;

import java.util.Locale;

/**
 * Map using Locale objects as keys.
 *
 * <p>This map should be filled once using calls to {@link #put(Locale, Object)},
 * before any calls are made to {@link #get(Locale)}.
 *
 * @version CVS $Id: LocaleMap.java,v 1.1 2004/03/09 10:34:06 reinhard Exp $
 */
public class LocaleMap {
    private FastHashMap map = new FastHashMap();

    /**
     * Gets an object based on the given locale. An automatic fallback mechanism is used:
     * if nothing is found for language-COUNTRY-variant, then language-COUNTRY is searched,
     * the language, and finally "" (empty string). If nothing is found null is returned.
     */
    public Object get(Locale locale) {
        if (map.size() == 0)
            return null;

        String full = getFullKey(locale);

        if (!map.containsKey(full)) {
            // check more general variant (lang-COUNTRY and lang), and store result in the map
            // under the full key, so that next time we have a direct hit
            String altKey = locale.getLanguage() + '-' + locale.getCountry();
            Object object = map.get(altKey);
            if (object != null) {
                map.put(full, object);
                return object;
            }

            altKey = locale.getLanguage();
            object = map.get(altKey);
            if (object != null) {
                map.put(full, object);
                return object;
            }

            object = map.get("");
            if (object != null) {
                map.put(full, object);
                return object;
            }

            map.put(full, null);
        }

        return map.get(full);
    }

    private final String getFullKey(Locale locale) {
        return locale.getLanguage() + '-' + locale.getCountry() + '-' + locale.getVariant();
    }

    private final String getKey(Locale locale) {
        boolean hasLanguage = !locale.getLanguage().equals("");
        boolean hasCountry = !locale.getCountry().equals("");
        boolean hasVariant = !locale.getVariant().equals("");

        if (hasLanguage && hasCountry && hasVariant)
            return locale.getLanguage() + '-' + locale.getCountry() + '-' + locale.getVariant();
        else if (hasLanguage && hasCountry)
            return locale.getLanguage() + '-' + locale.getCountry();
        else if (hasLanguage)
            return locale.getLanguage();
        else
            return "";
    }

    public void put(Locale locale, Object object) {
        map.put(getKey(locale), object);
    }
}
