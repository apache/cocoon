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
package org.apache.cocoon.i18n;

import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A helper class for i18n formatting and parsing routing.
 * Contains static methods only.
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @version CVS $Id: I18nUtils.java,v 1.2 2003/12/10 15:37:37 vgritsenko Exp $
 */
public class I18nUtils {

    // Locale string delimiter
    private static final String LOCALE_DELIMITER = "_-@.";

    private I18nUtils() {
        // Disable instantiation
    }

    /**
     * Parses given locale string to Locale object. If the string is null
     * then the given locale is returned.
     *
     * @param localeString a string containing locale in
     * <code>language_country_variant</code> format.
     * @param defaultLocale returned if localeString is <code>null</code>
     */
    public static Locale parseLocale(String localeString, Locale defaultLocale) {
        if (localeString != null) {
            StringTokenizer st = new StringTokenizer(localeString,
                                                     LOCALE_DELIMITER);
            String l = st.hasMoreElements() ? st.nextToken()
                                            : defaultLocale.getLanguage();
            String c = st.hasMoreElements() ? st.nextToken() : "";
            String v = st.hasMoreElements() ? st.nextToken() : "";
            return new Locale(l, c, v);
        }

        return defaultLocale;
    }

    /**
     * Parses given locale string to Locale object. If the string is null
     * then the VM default locale is returned.
     *
     * @param localeString a string containing locale in
     * <code>language_country_variant</code> format.
     *
     * @see #parseLocale(String, Locale)
     * @see java.util.Locale#getDefault()
     */
    public static Locale parseLocale(String localeString) {
        return parseLocale(localeString, Locale.getDefault());
    }
}
