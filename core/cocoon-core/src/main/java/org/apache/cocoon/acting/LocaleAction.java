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
package org.apache.cocoon.acting;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.i18n.I18nUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * An action that locates and provides to the pipeline locale information
 * looked up in a range of ways.
 *
 * <h1>Configuration</h1>
 * <p>A sample configuration (given in the &lt;map:matchers&gt; section of the
 * sitemap) is given below. This configuration shows default values.
 * </p>
 * <pre>
 *   &lt;map:action name="locale" src="org.apache.cocoon.acting.LocaleAction"&gt;
 *     &lt;locale-attribute&gt;locale&lt;/locale-attribute&gt;
 *     &lt;use-locale&gt;true&lt;/use-locale&gt;
 *     &lt;default-locale language="en" country="US"/&gt;
 *     &lt;store-in-request&gt;false&lt;store-in-request&gt;
 *     &lt;create-session&gt;false&lt;create-session&gt;
 *     &lt;store-in-session&gt;false&lt;store-in-session&gt;
 *     &lt;store-in-cookie&gt;false&lt;store-in-cookie&gt;
 *   &lt;/map:action&gt;
 * </pre>
 *
 * <p>Above configuration parameters mean:
 *   <ul>
 *     <li><b>locale-attribute</b> specifies the name of the request
 *     parameter / session attribute / cookie that is to be used as a locale
 *     (defaults to <code>locale</code>)</li>
 *     <li><b>use-locale</b> specifies whether the primary locale provided
 *     by the user agent (or server default, is no locale passed by the agent)
 *     is to be used</li>
 *     <li><b>default-locale</b> specifies the default locale to be used when
 *     none found.</li>
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
 * <p>This action will be used in a pipeline like so:</p>
 * <pre>
 *   &lt;map:act type="locale"&gt;
 *     &lt;map:generate src="file_{language}_{country}_{variant}.xml"/&gt;
 *     ...
 *   &lt;/map:match&gt;
 * </pre>
 * <p>or</p>
 * <pre>
 *   &lt;map:act type="locale"&gt;
 *     &lt;map:generate src="file_{locale}.xml"/&gt;
 *     ...
 *   &lt;/map:match&gt;
 * </pre>
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
 *   <li>The default locale, if specified in the matcher's configuration</li>
 * </ul>
 * <p>First found locale will be returned.</p>
 *
 * <h1>Sitemap Variables</h1>
 * <p>Once locale has been found, the following sitemap variables
 * will be available to sitemap elements contained within the action:</p>
 * <ul>
 *   <li>{locale}: The locale string</li>
 *   <li>{language}: The language of the found locale</li>
 *   <li>{country}: The country of the found locale</li>
 *   <li>{variant}: The variant of the found locale</li>
 * </ul>
 *
 * @cocoon.sitemap.component.documentation
 * An action that locates and provides to the pipeline locale information
 * looked up in a range of ways.
 *
 * @version $Id$
 */
public class LocaleAction extends ServiceableAction implements ThreadSafe, Configurable {

    private static final String DEFAULT_DEFAULT_LANG = "en";
    private static final String DEFAULT_DEFAULT_COUNTRY = "US";
    private static final String DEFAULT_DEFAULT_VARIANT = "";

    /**
      * Default locale attribute name.
     */
    public static final String LOCALE = "locale";

    /**
     * Configuration element name for locale attribute name.
     */
    public static final String LOCALE_ATTR = "locale-attribute";


    /**
     * Constant representing the request storage configuration attribute
     */
    public static final String STORE_REQUEST = "store-in-request";

    /**
     * Constant representing the session creation configuration attribute
     */
    public static final String CREATE_SESSION = "create-session";

    /**
     * Constant representing the session storage configuration attribute
     */
    public static final String STORE_SESSION = "store-in-session";

    /**
     * Constant representing the cookie storage configuration attribute
     */
    public static final String STORE_COOKIE = "store-in-cookie";


    /**
     * Name of the locale request parameter, session attribute, cookie.
     */
    private String localeAttribute;

    /**
     * Whether to query locale provided by the user agent or not.
     */
    private boolean useLocale;

    /**
     * Default locale if no other found and {@link #useLocale} is false.
     */
    private Locale defaultLocale;

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

    /**
     * Configure this action.
     *
     * @param config configuration information (if any)
     */
    public void configure(Configuration config)
    throws ConfigurationException {
        this.storeInRequest = config.getChild(STORE_REQUEST).getValueAsBoolean(false);
        this.createSession = config.getChild(CREATE_SESSION).getValueAsBoolean(false);
        this.storeInSession = config.getChild(STORE_SESSION).getValueAsBoolean(false);
        this.storeInCookie = config.getChild(STORE_COOKIE).getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((this.storeInRequest ? "will" : "won't") + " set values in request");
            getLogger().debug((this.createSession ? "will" : "won't") + " create session");
            getLogger().debug((this.storeInSession ? "will" : "won't") + " set values in session");
            getLogger().debug((this.storeInCookie ? "will" : "won't") + " set values in cookies");
        }

        this.localeAttribute = config.getChild(LOCALE_ATTR).getValue(LOCALE);
        this.useLocale = config.getChild("use-locale").getValueAsBoolean(true);

        Configuration child = config.getChild("default-locale", false);
        if (child != null) {
            this.defaultLocale = new Locale(child.getAttribute("language", DEFAULT_DEFAULT_LANG),
                                            child.getAttribute("country", DEFAULT_DEFAULT_COUNTRY),
                                            child.getAttribute("variant", DEFAULT_DEFAULT_VARIANT));
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Locale attribute name is " + this.localeAttribute);
            getLogger().debug((this.useLocale ? "will" : "won't") + " use request locale");
            getLogger().debug("default locale " + this.defaultLocale);
        }
    }

    /**
     * Action which obtains the current environments locale information, and
     * places it in the objectModel (and optionally in a session/cookie).
     */
    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters params)
    throws Exception {
        // Obtain locale information from request, session, cookies, or params
        Locale locale = I18nUtils.findLocale(objectModel,
                                             localeAttribute,
                                             params,
                                             defaultLocale,
                                             useLocale);

        if (locale == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No locale found.");
            }

            return null;
        }

        String localeStr = locale.toString();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Found locale: " + localeStr);
        }

        I18nUtils.storeLocale(objectModel,
                              localeAttribute,
                              localeStr,
                              storeInRequest,
                              storeInSession,
                              storeInCookie,
                              createSession);

        // Set up a map for sitemap parameters
        Map map = new HashMap();
        map.put("language", locale.getLanguage());
        map.put("country", locale.getCountry());
        map.put("variant", locale.getVariant());
        map.put("locale", localeStr);
        return map;
    }

    /**
     * Helper method to retreive the attribute value containing locale
     * information. See class documentation for locale determination algorythm.
     *
     * @deprecated See I18nUtils.findLocale
     * @param objectModel requesting object's environment
     * @return locale value or <code>null</null> if no locale was found
     */
    public static String getLocaleAttribute(Map objectModel,
                                            String localeAttrName) {
        Locale locale = I18nUtils.findLocale(objectModel,
                                             localeAttrName,
                                             null,
                                             null,
                                             true);
        return locale.toString();
    }
}
