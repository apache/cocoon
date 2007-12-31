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
package org.apache.cocoon.i18n;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.util.Deprecation;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A utility class for i18n formatting and parsing routing.
 *
 * @author <a href="mailto:kpiroumian@apache.org">Konstantin Piroumian</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version $Id$
 */
public class I18nUtils {

    /**
     * Locale string delimiter
     */
    private static final String LOCALE_DELIMITER = "_-@.";

    /**
     * Did we already encountered an old namespace? This is static to ensure
     * that the associated message will be logged only once.
     */
    private static boolean deprecationFound = false;

    /**
     * The namespace for i18n is "http://apache.org/cocoon/i18n/2.1".
     */
    public static final String NAMESPACE_URI =
            "http://apache.org/cocoon/i18n/2.1";

    /**
     * The old namespace for i18n is "http://apache.org/cocoon/i18n/2.0".
     */
    public static final String OLD_NAMESPACE_URI =
            "http://apache.org/cocoon/i18n/2.0";


    private I18nUtils() {
        // Disable instantiation
    }

    /**
     * Parses given locale string to Locale object. If the string is null
     * or empty then the given locale is returned.
     *
     * @param localeString - a string containing locale in
     *        <code>language_country_variant</code> format.
     * @param defaultLocale - returned if localeString is <code>null</code>
     *        or <code>""</code>
     */
    public static Locale parseLocale(String localeString, Locale defaultLocale) {
        if (localeString != null && localeString.length() > 0) {
            StringTokenizer st = new StringTokenizer(localeString, LOCALE_DELIMITER);
            String l = st.hasMoreElements() ? st.nextToken() : defaultLocale.getLanguage();
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


    /**
     * Callback interface for
     * {@link I18nUtils#findLocale(Map, String, Parameters, Locale, boolean, boolean, boolean, I18nUtils.LocaleValidator)}
     * @since 2.1.6
     */
    public interface LocaleValidator {

        /**
         * @param name of the locale (for debugging)
         * @param locale to test
         * @return true if locale satisfies validator's criteria
         */
        public boolean test(String name, Locale locale);
    }

    /**
     * Find a suitable locale from an objectModel.
     * @since 2.1.6
     * @return locale found, or null if none found.
     */
    public static Locale findLocale(Map objectModel,
                                    String attribute,
                                    Parameters parameters,
                                    Locale defaultLocale,
                                    boolean useLocale,
                                    boolean useLocales,
                                    boolean useBlankLocale,
                                    LocaleValidator test) {
        String localeStr;
        Locale locale;

        Request request = ObjectModelHelper.getRequest(objectModel);

        // 1. Request parameter 'locale'
        localeStr = request.getParameter(attribute);
        if (localeStr != null) {
            locale = parseLocale(localeStr);
            if (test == null || test.test("request", locale)) {
                return locale;
            }
        }

        // 2. Session attribute 'locale'
        Session session = request.getSession(false);
        if (session != null &&
                ((localeStr = (String) session.getAttribute(attribute)) != null)) {
            locale = parseLocale(localeStr);
            if (test == null || test.test("session", locale)) {
                return locale;
            }
        }

        // 3. First matching cookie parameter 'locale' within each cookie sent
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(attribute)) {
                    localeStr = cookie.getValue();
                    locale = parseLocale(localeStr);
                    if (test == null || test.test("cookie", locale)) {
                        return locale;
                    }
                    break;
                }
            }
        }

        // 4. Sitemap parameter "locale"
        if (parameters != null) {
            localeStr = parameters.getParameter("locale", null);
            if (localeStr != null) {
                locale = parseLocale(localeStr);
                if (test == null || test.test("sitemap", locale)) {
                    return locale;
                }
            }
        }

        // 5. Locale setting of the requesting browser or server default
        if (useLocale && !useLocales) {
            locale = request.getLocale();
            if (test == null || test.test("request", locale)) {
                return locale;
            }
        }
        if (useLocales) {
            Enumeration locales = request.getLocales();
            while (locales.hasMoreElements()) {
                locale = (Locale)locales.nextElement();
                if (test == null || test.test("request", locale)) {
                    return locale;
                }
            }
        }

        // 6. Default
        if (defaultLocale != null) {
            locale = defaultLocale;
            if (test == null || test.test("default", locale)) {
                return locale;
            }
        }

        // 7. Blank
        if (useBlankLocale) {
            locale = new Locale("", ""); // Use JDK1.3 constructor
            if (test == null || test.test("blank", locale)) {
                return locale;
            }
        }

        // 8. Fail
        return null;
    }

    /**
     * Find a suitable locale from an objectModel.
     * @since 2.1.6
     * @return locale found, or server default (never null).
     */
    public static Locale findLocale(Map objectModel,
                                    String attribute,
                                    Parameters parameters,
                                    Locale defaultLocale,
                                    boolean useLocale) {
        return findLocale(objectModel, attribute, parameters, defaultLocale, useLocale, false, false, null);
    }

    /**
     * Store locale in request, session, or cookie.
     * @since 2.1.6
     */
    public static void storeLocale(Map objectModel,
                                   String attribute,
                                   String locale,
                                   boolean storeInRequest,
                                   boolean storeInSession,
                                   boolean storeInCookie,
                                   boolean createSession) {
        // store in a request if so configured
        if (storeInRequest) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            request.setAttribute(attribute, locale);
        }

        // store in session if so configured
        if (storeInSession) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            Session session = request.getSession(createSession);
            if (session != null) {
                session.setAttribute(attribute, locale);
            }
        }

        // store in a cookie if so configured
        if (storeInCookie) {
            Response response = ObjectModelHelper.getResponse(objectModel);
            response.addCookie(response.createCookie(attribute, locale));
        }
    }

    public static boolean matchesI18nNamespace(String uri) {
        if (NAMESPACE_URI.equals(uri)) {
            return true;
        } else if (OLD_NAMESPACE_URI.equals(uri)) {
            if (!deprecationFound) {
                deprecationFound = true;
                Deprecation.logger.warn("The namespace <" + OLD_NAMESPACE_URI +
                                        "> is deprecated, use: <" + NAMESPACE_URI + ">");
            }
            return true;
        }
        return false;
    }
}
