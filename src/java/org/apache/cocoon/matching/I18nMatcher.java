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
package org.apache.cocoon.matching;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * A matcher that locates and identifies to the pipeline a source document to be used as
 * the content for an i18n site, based upon a locale provided in a range of ways.
 *
 * <h1>Configuration</h1>
 * <p>
 * A sample configuration (given in the &lt;matchers&gt; section of the sitemap) is given
 * below.
 * </p>
 * <pre>
 *  &lt;map:matcher name="i18n" src="org.apache.cocoon.matching.I18nMatcher"&gt;
 *    &lt;locale-attribute&gt;locale&lt;/locale-attribute&gt;
 *    &lt;use-locale&gt;true&lt;/use-locale&gt;
 *    &lt;use-locales&gt;true&lt;/use-locales&gt;
 *    &lt;default-locale lang="pt" country="BR"/&gt;
 *    &lt;test-blank-locale&gt;true&lt;/test-blank-locale&gt;
 *  &lt;/map:matcher&gt;
 * </pre>
 * <p>
 * Within this configuration, it is possible to:
 *   <ul>
 *     <li>Specify the name of request parameter / session attribute / cookie
 *     that is to be used as a locale (defaults to <code>locale</code>)</li>
 *     <li>Specify whether the primary locale provided by the user agent is to be used</li>
 *     <li>Specify whether each locale provided by the user agent should be tested in turn</li>
 *     <li>Specify the default locale to be used when none matches any of the previous</li>
 *     <li>Specify whether a file should be looked for without a locale in
 *     its filename or filepath, e.g. after looking for index.en.html, try index.html.</li>
 *   </ul>
 * </p>
 *
 * <h1>Usage</h1>
 * <p>This matcher will be used in a pipeline like so:</p>
 * <pre>
 *   &lt;map:match pattern="*.html"&gt;
 *     &lt;map:match type="i18n" pattern="xml/{1}.*.xml"&gt;
 *       &lt;map:generate src="{source}"/&gt;
 *       ...
 *     &lt;/map:match&gt;
 *   &lt;/map:match&gt;
 * </pre>
 *
 * <h1>Locale Identification</h1>
 * <p>A source will be looked for using each of the following as a source for locale. Where the
 * full locale (language, country, variant) doesn't match, it will fall back to first language
 * and country, and then just language, before moving on to the next locale.
 * <ul>
 *   <li>Locale provided as a request parameter</li>
 *   <li>Locale provided as a session attribute</li>
 *   <li>Locale provided as a cookie</li>
 *   <li>Locale provided using a map parameter<br>
 *   (&lt;map:parameter name="locale" value="{1}"/&gt; style parameter within
 *   the &lt;map:match&gt; node)</li>
 *   <li>Locale(s) provided by the user agent or server default</li>
 *   <li>The default locale specified in the matcher's configuration</li>
 *   <li>Resources with no defined locale (blank locale)</li>
 * </ul>
 * </p>
 *
 * <h1>Sitemap Variables</h1>
 * <p>Once a matching locale has been found, the following sitemap variables
 * will be available to sitemap elements contained within the matcher:
 * <ul>
 *   <li>{source}: The URI of the source that matched</li>
 *   <li>{locale}: The locale that matched that resource</li>
 *   <li>{matched-locale}: The part of the locale that matched the resource</li>
 *   <li>{language}: The language of the matching resource</li>
 *   <li>{country}: The country of the matching resource</li>
 *   <li>{variant}: The variant of the matching resource</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id$
 */
public class I18nMatcher extends AbstractLogEnabled implements Matcher, ThreadSafe, Serviceable, Configurable {

    private static final boolean DEFAULT_USE_LOCALE = true;
    private static final boolean DEFAULT_USE_LOCALES = true;
    private static final String DEFAULT_DEFAULT_LANG = "en";
    private static final String DEFAULT_DEFAULT_COUNTRY = "US";
    private static final String DEFAULT_DEFAULT_VARIANT = null;
    private static final String DEFAULT_LOCALE_ATTRIBUTE = "locale";
    private static final boolean DEFAULT_TEST_BLANK_LOCALE = true;

    private static final String MAP_LOCALE = "locale";
    private static final String MAP_MATCHED_LOCALE = "matched-locale";
    private static final String MAP_SOURCE = "source";
    private static final String MAP_COUNTRY ="country";
    private static final String MAP_LANGUAGE ="language";
    private static final String MAP_VARIANT = "variant";

    private ServiceManager manager;
    private SourceResolver resolver;
    private String localeAttribute;
    private boolean useLocale;
    private boolean useLocales;
    private Locale defaultLocale;
    private boolean testBlankLocale;

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    public void configure(Configuration config) {
        this.useLocale = config.getChild("use-locale").getValueAsBoolean(DEFAULT_USE_LOCALE);
        this.useLocales = config.getChild("use-locales").getValueAsBoolean(DEFAULT_USE_LOCALES);

        Configuration child = config.getChild("default-locale");
        this.defaultLocale = getLocale(child.getAttribute("lang", DEFAULT_DEFAULT_LANG),
                                       child.getAttribute("country", DEFAULT_DEFAULT_COUNTRY),
                                       child.getAttribute("variant", DEFAULT_DEFAULT_VARIANT));

        this.localeAttribute = config.getChild("locale-attribute").getValue(DEFAULT_LOCALE_ATTRIBUTE);
        this.testBlankLocale = config.getChild("test-blank-locale").getValueAsBoolean(DEFAULT_TEST_BLANK_LOCALE);
    }

    private Locale getLocale(String lang, String country, String variant) {
        if (lang == null) {
            return null;
        }

        if (country == null) {
            return new Locale(lang, "");
        }

        if (variant == null) {
            return new Locale(lang, country);
        }

        return new Locale(lang, country, variant);
    }

    public Map match(String pattern, Map objectModel, Parameters parameters) throws PatternException {
        Map map = new HashMap();
        Request request = ObjectModelHelper.getRequest(objectModel);

        // 1. Request parameter 'locale'
        String locale = request.getParameter(localeAttribute);
        if (locale != null && isValidResource(pattern, new Locale(locale, ""), map)) {
             return map;
        }

        // 2. Session attribute 'locale'
        Session session = request.getSession(false);
        if (session != null &&
            ((locale = (String) session.getAttribute(localeAttribute)) != null) &&
                isValidResource(pattern, new Locale(locale, ""), map)) {
            return map;
        }

        // 3. First matching cookie parameter 'locale' within each cookie sent
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(localeAttribute)) {
                    if (isValidResource(pattern, new Locale(locale, ""), map)) {
                        return map;
                    }
                    break;
                }
            }
        }

        // 4. Sitemap parameter "locale"
        locale = parameters.getParameter("locale", null);
        if (locale != null && isValidResource(pattern, new Locale(locale, ""), map)) {
              return map;
        }

        // 5. Locale setting of the requesting browser or server default
        if (useLocale && !useLocales) {
            Locale l = request.getLocale();
            if(isValidResource(pattern, l, map)) {
                return map;
            }
        }

        if (useLocales) {
            Enumeration locales = request.getLocales();
            while (locales.hasMoreElements()) {
                Locale l = (Locale)locales.nextElement();
                if (isValidResource(pattern, l, map)) {
                    return map;
                }
            }
        }

        // 6. Default
        if (defaultLocale != null && isValidResource(pattern, defaultLocale, map)) {
            return map;
        }

        // 7. Blank
        if (testBlankLocale && isValidResource(pattern, null, map)) {
            return map;
        }
        return null;
    }

    private boolean isValidResource(String pattern, Locale locale, Map map) {
        Locale testLocale;

        if (locale == null) {
            return isValidResource(pattern, null, null, map);
        }

        testLocale = locale;
        if (isValidResource(pattern, locale, testLocale.toString(), map)) {
            return true;
        }

        testLocale = new Locale(locale.getLanguage(), locale.getCountry());
        if (isValidResource(pattern, locale, testLocale.toString(), map)) {
            return true;
        }

        testLocale = new Locale(locale.getLanguage(), "");
        if (isValidResource(pattern, locale, testLocale.toString(), map)) {
            return true;
        }

        return false;
    }

    private boolean isValidResource(String pattern, Locale locale, String localeString, Map map) {
        String url;
        if (localeString != null) {
            url = StringUtils.replace(pattern, "*", localeString);
        } else {
            // If same character found before and after the '*', leave only one.
            int starPos = pattern.indexOf("*");
            if (starPos < pattern.length() - 1 && starPos > 1 &&
                    pattern.charAt(starPos - 1) == pattern.charAt(starPos + 1)) {
                url = pattern.substring(0, starPos - 1) + pattern.substring(starPos + 1);
            } else {
                url = StringUtils.replace(pattern, "*", "");
            }

            // Blank locale - empty string
            localeString = "";
        }

        boolean result = false;
        Source source = null;
        try {
            source = resolver.resolveURI(url);
            if (source.exists()) {
                map.put(MAP_SOURCE, url);
                map.put(MAP_MATCHED_LOCALE, localeString);
                if (locale != null) {
                    map.put(MAP_LOCALE, locale.toString());
                    map.put(MAP_LANGUAGE, locale.getLanguage());
                    map.put(MAP_COUNTRY, locale.getCountry());
                    map.put(MAP_VARIANT, locale.getVariant());
                }
                result = true;
            }
        } catch (IOException e) {
            // result is false
        } finally {
            if (source != null) {
                resolver.release(source);
            }
        }

        return result;
    }
}
