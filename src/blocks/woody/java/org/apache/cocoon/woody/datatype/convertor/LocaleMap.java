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

import org.apache.commons.collections.FastHashMap;

import java.util.Locale;

/**
 * Map using Locale objects as keys.
 *
 * <p>This map should be filled once using calls to {@link #put(Locale, Object)},
 * before any calls are made to {@link #get(Locale)}.
 *
 * @version CVS $Id: LocaleMap.java,v 1.3 2004/02/19 22:13:28 joerg Exp $
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
