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
package org.apache.cocoon.matching;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.sitemap.PatternException;

import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A matcher that locates and identifies to the pipeline a source document to
 * be used as the content for an i18n site, based upon a locale provided in a
 * range of ways.
 *
 * <h1>Configuration</h1>
 * <p>A sample configuration (given in the &lt;map:matchers&gt; section of the
 * sitemap) is given below. This configuration shows default values.
 * </p>
 * <pre>
 *   &lt;map:matcher name="i18n" src="org.apache.cocoon.matching.LocaleMatcher"&gt;
 *     &lt;locale-attribute&gt;locale&lt;/locale-attribute&gt;
 *     &lt;negotiate&gt;false&lt;/negotiate&gt;
 *     &lt;use-locale&gt;true&lt;/use-locale&gt;
 *     &lt;use-locales&gt;false&lt;/use-locales&gt;
 *     &lt;use-blank-locale&gt;true&lt;/use-blank-locale&gt;
 *     &lt;default-locale language="en" country="US"/&gt;
 *     &lt;store-in-request&gt;false&lt;store-in-request&gt;
 *     &lt;create-session&gt;false&lt;create-session&gt;
 *     &lt;store-in-session&gt;false&lt;store-in-session&gt;
 *     &lt;store-in-cookie&gt;false&lt;store-in-cookie&gt;
 *   &lt;/map:matcher&gt;
 * </pre>
 *
 * <p>Above configuration parameters mean:
 *   <ul>
 *     <li><b>locale-attribute</b> specifies the name of the request
 *     parameter / session attribute / cookie that is to be used as a locale
 *     (defaults to <code>locale</code>)</li>
 *     <li><b>negotiate</b> specifies whether matcher should check that
 *     resource exists. If set to true, matcher will look for the locale
 *     till matching resource is found. If no resource found even with
 *     default or blank locale, matcher will not match.</li>
 *     <li><b>use-locale</b> specifies whether the primary locale provided
 *     by the user agent (or server default, is no locale passed by the agent)
 *     is to be used</li>
 *     <li><b>use-locales</b> specifies whether each locale provided by the
 *     user agent should be tested in turn (makes sense only when
 *     <code>negotiate</code> is set to <code>true</code>)</li>
 *     <li><b>default-locale</b> specifies the default locale to be used when
 *     none matches any of the previous ones.</li>
 *     <li><b>use-blank-locale</b> specifies whether a file should be looked
 *     for without a locale in its filename or filepath (e.g. after looking
 *     for index.en.html, try index.html) if none matches any of the previous
 *     locales.</li>
 *     <li><b>store-in-request</b> specifies whether found locale should be
 *     stored as request attribute.</li>
 *     <li><b>create-session</b> specifies whether session should be created
 *     when storing found locale as session attribute.</li>
 *     <li><b>store-in-session</b> specifies whether found locale should be
 *     stored as session attribute.</li>
 *     <li><b>store-in-cookie</b> specifies whether found locale should be
 *     stored as cookie.</li>
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
 * <p><code>*</code> in the pattern identifies the place where locale should
 * be inserted. In case of a blank locale, if character before and after
 * <code>*</code> is the same (like in example above), duplicate will
 * be removed (<code>xml/{1}.*.xml</code> becomes <code>xml/{1}.xml</code>).</p>
 *
 * <h1>Locale Identification</h1>
 * <p>Locales will be tested in following order:</p>
 * <ul>
 *   <li>Locale provided as a request parameter</li>
 *   <li>Locale provided as a session attribute</li>
 *   <li>Locale provided as a cookie</li>
 *   <li>Locale provided using a sitemap parameter<br>
 *   (&lt;map:parameter name="locale" value="{1}"/&gt; style parameter within
 *   the &lt;map:match&gt; node)</li>
 *   <li>Locale provided by the user agent, or server default,
 *   if <code>use-locale</code> is set to <code>true</code></li>
 *   <li>Locales provided by the user agent, if <code>use-locales</code>
 *   is set to <code>true</code>.</li>
 *   <li>The default locale, if specified in the matcher's configuration</li>
 *   <li>Resources with no defined locale (blank locale)</li>
 * </ul>
 * <p>If <code>negotiate</code> mode is set to <code>true</code>, a source will
 * be looked up using each locale. Where the full locale (language, country,
 * variant) doesn't match, it will fall back first to language and country,
 * and then just language, before moving on to the next locale.</p>
 * <p>If <code>negotiate</code> mode is set to <code>false</code> (default),
 * first found locale will be returned.</p>
 *
 * <h1>Sitemap Variables</h1>
 * <p>Once a matching locale has been found, the following sitemap variables
 * will be available to sitemap elements contained within the matcher:</p>
 * <ul>
 *   <li>{source}: The URI of the source that matched</li>
 *   <li>{locale}: The locale that matched that resource</li>
 *   <li>{matched-locale}: The part of the locale that matched the resource</li>
 *   <li>{language}: The language of the matching resource</li>
 *   <li>{country}: The country of the matching resource</li>
 *   <li>{variant}: The variant of the matching resource</li>
 * </ul>
 *
 * @since 2.1.6
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class LocaleMatcher extends AbstractLogEnabled
                           implements Matcher, ThreadSafe, Serviceable, Configurable, Disposable {

    private static final String DEFAULT_LOCALE_ATTRIBUTE = "locale";
    private static final String DEFAULT_DEFAULT_LANG = "en";
    private static final String DEFAULT_DEFAULT_COUNTRY = "US";
    private static final String DEFAULT_DEFAULT_VARIANT = "";

    private ServiceManager manager;
    private SourceResolver resolver;

    /**
     * Name of the locale request parameter, session attribute, cookie.
     */
    private String localeAttribute;

    /**
     * Whether to query locale provided by the user agent or not.
     */
    private boolean useLocale;

    private boolean useLocales;
    private Locale defaultLocale;
    private boolean useBlankLocale;
    private boolean testResourceExists;

    /**
     * Store the locale in request. Default is not to do this.
     */
    private boolean storeInRequest;

    /**
     * Store the locale in session, if available. Default is not to do this.
     */
    private boolean storeInSession;

    /**
     * Should we create a session if needed. Default is not to do this.
     */
    private boolean createSession;

    /**
     * Should we add a cookie with the locale. Default is not to do this.
     */
    private boolean storeInCookie;


    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    public void configure(Configuration config) {
        this.storeInRequest = config.getChild("store-in-request").getValueAsBoolean(false);
        this.createSession = config.getChild("create-session").getValueAsBoolean(false);
        this.storeInSession = config.getChild("store-in-session").getValueAsBoolean(false);
        this.storeInCookie = config.getChild("store-in-cookie").getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((this.storeInRequest ? "will" : "won't") + " set values in request");
            getLogger().debug((this.createSession ? "will" : "won't") + " create session");
            getLogger().debug((this.storeInSession ? "will" : "won't") + " set values in session");
            getLogger().debug((this.storeInCookie ? "will" : "won't") + " set values in cookies");
        }

        this.localeAttribute = config.getChild("locale-attribute").getValue(DEFAULT_LOCALE_ATTRIBUTE);
        this.testResourceExists = config.getChild("negotiate").getValueAsBoolean(false);

        this.useLocale = config.getChild("use-locale").getValueAsBoolean(true);
        this.useLocales = config.getChild("use-locales").getValueAsBoolean(false);
        this.useBlankLocale = config.getChild("use-blank-locale").getValueAsBoolean(true);

        Configuration child = config.getChild("default-locale", false);
        if (child != null) {
            this.defaultLocale = new Locale(child.getAttribute("language", DEFAULT_DEFAULT_LANG),
                                            child.getAttribute("country", DEFAULT_DEFAULT_COUNTRY),
                                            child.getAttribute("variant", DEFAULT_DEFAULT_VARIANT));
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Locale attribute name is " + this.localeAttribute);
            getLogger().debug((this.testResourceExists ? "will" : "won't") + " negotiate locale");
            getLogger().debug((this.useLocale ? "will" : "won't") + " use request locale");
            getLogger().debug((this.useLocales ? "will" : "won't") + " use request locales");
            getLogger().debug((this.useBlankLocale ? "will" : "won't") + " blank locales");
            getLogger().debug("default locale " + this.defaultLocale);
        }
    }

    public void dispose() {
        this.manager.release(this.resolver);
        this.resolver = null;
        this.manager = null;
    }


    public Map match(final String pattern, Map objectModel, Parameters parameters)
    throws PatternException {
        final Map map = new HashMap();

        I18nUtils.LocaleValidator validator = new I18nUtils.LocaleValidator() {
            public boolean test(String name, Locale locale) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Testing " + name + " locale: '" + locale + "'");
                }
                return isValidResource(pattern, locale, map);
            }
        };

        Locale locale = I18nUtils.findLocale(objectModel,
                                             localeAttribute,
                                             parameters,
                                             defaultLocale,
                                             useLocale,
                                             useLocales,
                                             useBlankLocale,
                                             validator);

        if (locale == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No locale found for resource: " + pattern);
            }
            return null;
        }

        String localeStr = locale.toString();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Locale " + localeStr + " found for resource: " + pattern);
        }

        I18nUtils.storeLocale(objectModel,
                              localeAttribute,
                              localeStr,
                              storeInRequest,
                              storeInSession,
                              storeInCookie,
                              createSession);

        return map;
    }

    private boolean isValidResource(String pattern, Locale locale, Map map) {
        Locale testLocale;

        // Test "language, country, variant" locale
        if (locale.getVariant().length() > 0) {
            if (isValidResource(pattern, locale, locale, map)) {
                return true;
            }
        }

        // Test "language, country" locale
        if (locale.getCountry().length() > 0) {
            testLocale = new Locale(locale.getLanguage(), locale.getCountry());
            if (isValidResource(pattern, locale, testLocale, map)) {
                return true;
            }
        }

        // Test "language" locale (or empty - if language is "")
        testLocale = new Locale(locale.getLanguage(), ""); // Use JDK1.3 constructor
        if (isValidResource(pattern, locale, testLocale, map)) {
            return true;
        }

        return false;
    }

    private boolean isValidResource(String pattern, Locale locale, Locale testLocale, Map map) {
        String url;

        String testLocaleStr = testLocale.toString();
        if ("".equals(testLocaleStr)) {
            // If same character found before and after the '*', leave only one.
            int starPos = pattern.indexOf("*");
            if (starPos < pattern.length() - 1 && starPos > 1 &&
                    pattern.charAt(starPos - 1) == pattern.charAt(starPos + 1)) {
                url = pattern.substring(0, starPos - 1) + pattern.substring(starPos + 1);
            } else {
                url = StringUtils.replace(pattern, "*", "");
            }
        } else {
            url = StringUtils.replace(pattern, "*", testLocaleStr);
        }

        boolean result = true;
        if (testResourceExists) {
            Source source = null;
            try {
                source = resolver.resolveURI(url);
                result = source.exists();
            } catch (IOException e) {
                result = false;
            } finally {
                if (source != null) {
                    resolver.release(source);
                }
            }
        }

        if (result) {
            map.put("source", url);
            map.put("matched-locale", testLocaleStr);
            if (locale != null) {
                map.put("locale", locale.toString());
                map.put("language", locale.getLanguage());
                map.put("country", locale.getCountry());
                map.put("variant", locale.getVariant());
            }
        }

        return result;
    }
}
