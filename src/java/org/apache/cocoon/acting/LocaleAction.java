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
package org.apache.cocoon.acting;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.i18n.I18nUtils;

/**
 * LocaleAction is a class which obtains the request's locale information
 * (language, country, variant) and makes it available to the
 * sitemap/pipeline.
 *
 * Definition in sitemap:
 * <pre>
 * &lt;map:actions&gt;
 *  &lt;map:action name="locale" src="org.apache.cocoon.acting.LocaleAction"/&gt;
 * &lt;/map:actions&gt;
 * </pre>
 *
 * Examples:
 *
 * <pre>
 * &lt;map:match pattern="file"&gt;
 *  &lt;map:act type="locale"&gt;
 *   &lt;map:generate src="file_{lang}{country}{variant}.xml"/&gt;
 *  &lt;/map:act&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * or
 *
 * <pre>
 * &lt;map:match pattern="file"&gt;
 *  &lt;map:act type="locale"&gt;
 *   &lt;map:generate src="file.xml?locale={locale}"/&gt;
 *  &lt;/map:act&gt;
 * &lt;/map:match&gt;
 * </pre>
 *
 * <br>
 *
 * The variables <code>lang</code>, <code>country</code>,
 * <code>variant</code>, and <code>locale</code> are all available. Note that
 * <code>country</code> and <code>variant</code> can be empty, however
 * <code>lang</code> and <code>locale</code> will always contain a valid value.
 *
 * <br>
 *
 * The following search criteria are used in order when ascertaining locale
 * values:
 *
 * <ol>
 *   <li>Request CGI parameter <i>locale</i></li>
 *   <li>Session attribute <i>locale</i></li>
 *   <li>First matching Cookie parameter <i>locale</i>
 *       within each cookie sent with the current request</li>
 *   <li>Locale setting of the requesting object</li>
 * </ol>
 *
 * (in the case of language, if the above cases do not yield a valid value
 * the locale value of the server is used)
 *
 * <br>
 * The attribute names can be configured/customized at action definition
 * using the configuration parameters
 * {language, country, variant, locale}-attribute.
 *
 * eg.
 *
 * <pre>
 * &lt;map:action name="locale" src="org.apache.cocoon.acting.LocaleAction"&gt;
 *  &lt;language-attribute&gt;lg&lt;/language-attribute&gt;
 * &lt;/map:action&gt;
 * </pre>
 *
 * <p>Code originated from org.apache.cocoon.acting.LangSelect</p>
 *
 * @author <a href="mailto:Marcus.Crafter@osa.de">Marcus Crafter</a>
 * @author <a href="mailto:kpiroumian@flagship.ru">Konstantin Piroumian</a>
 * @author <a href="mailto:lassi.immonen@valkeus.com">Lassi Immonen</a>
 * @version CVS $Id$
 */
public class LocaleAction extends ServiceableAction implements ThreadSafe, Configurable {

    /**
     * Constant representing the language parameter
     */
    public static final String LANG = "lang";

    /**
     * Constant representing the country parameter
     */
    public static final String COUNTRY = "country";

    /**
     * Constant representing the variant parameter
     */
    public static final String VARIANT = "variant";

    /**
     * Constant representing the locale parameter
     */
    public static final String LOCALE = "locale";

    /**
     * Constant representing the language configuration attribute
     */
    public static final String LANG_ATTR = "language-attribute";

    /**
     * Constant representing the country configuration attribute
     */
    public static final String COUNTRY_ATTR = "country-attribute";

    /**
     * Constant representing the variant configuration attribute
     */
    public static final String VARIANT_ATTR = "variant-attribute";

    /**
     * Constant representing the locale configuration attribute
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


    // Store the lang in request. Default is not to do this.
    private boolean storeInRequest;

    // Store the lang in session, if available. Default is not to do this.
    private boolean storeInSession;

    // Should we create a session if needed. Default is not to do this.
    private boolean createSession;

    // Should we add a cookie with the lang. Default is not to do this.
    private boolean storeInCookie;

    // Configuration attributes.
    private String langAttr;
    private String countryAttr;
    private String variantAttr;
    private String localeAttr;


    /**
     * Configure this action.
     *
     * @param conf configuration information (if any)
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        this.storeInRequest = conf.getChild(STORE_REQUEST).getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((this.storeInRequest ? "will" : "won't") + " set values in request");
        }

        this.createSession = conf.getChild(CREATE_SESSION).getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((this.createSession ? "will" : "won't") + " create session");
        }

        this.storeInSession = conf.getChild(STORE_SESSION).getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((this.storeInSession ? "will" : "won't") + " set values in session");
        }

        this.storeInCookie = conf.getChild(STORE_COOKIE).getValueAsBoolean(false);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug((this.storeInCookie ? "will" : "won't") + " set values in cookies");
        }

        this.langAttr = conf.getChild(LANG_ATTR).getValue(LANG);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("global language attribute name is " + this.langAttr);
        }

        this.countryAttr = conf.getChild(COUNTRY_ATTR).getValue(COUNTRY);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("global country attribute name is " + this.countryAttr);
        }

        this.variantAttr = conf.getChild(VARIANT_ATTR).getValue(VARIANT);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("global variant attribute name is " + this.variantAttr);
        }

        this.localeAttr = conf.getChild(LOCALE_ATTR).getValue(LOCALE);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("global locale attribute name is " + this.localeAttr);
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

        // obtain locale information from params, session or cookies
        String lc = getLocaleAttribute(objectModel, localeAttr);
        Locale locale = I18nUtils.parseLocale(lc);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Found locale = " + lc);
            checkParams(params);
        }

        if (storeInRequest) {
            Request request = ObjectModelHelper.getRequest(objectModel);

            request.setAttribute(localeAttr, lc);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("updated request");
            }
        }

        // store in session if so configured
        if (storeInSession) {
            Request request = ObjectModelHelper.getRequest(objectModel);
            Session session = request.getSession(createSession);

            if (session != null) {
                session.setAttribute(localeAttr, lc);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("updated session");
                }
            }
        }

        // store in a cookie if so configured
        if (storeInCookie) {
            Response response = ObjectModelHelper.getResponse(objectModel);

            response.addCookie(response.createCookie(localeAttr, lc));
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("created cookies");
            }
        }

        // set up a map for sitemap parameters
        Map map = new HashMap();
        map.put(langAttr, locale.getLanguage());
        map.put(countryAttr, locale.getCountry());
        map.put(variantAttr, locale.getVariant());
        map.put(localeAttr, lc);

        return map;
    }

    /**
     * Helper method to retreive the attribute value containing locale
     * information. See class documentation for locale determination algorythm.
     *
     * @param objectModel requesting object's environment
     * @return locale value or <code>null</null> if no locale was found
     */
    public static String getLocaleAttribute(Map objectModel,
                                            String localeAttrName) {
        String locale;

        Request request = ObjectModelHelper.getRequest(objectModel);

        // 1. Request parameter 'locale'
        if ((locale = request.getParameter(localeAttrName)) != null) {
            return locale;
        }

        // 2. Session attribute 'locale'
        Session session = request.getSession(false);
        if (session != null &&
            ((locale = (String) session.getAttribute(localeAttrName)) != null)) {
            return locale;
        }

        // 3. First matching cookie parameter 'locale' within each cookie sent
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(localeAttrName)) {
                    return cookie.getValue();
                }
            }
        }

        // 4. Locale setting of the requesting browser or server default
        return request.getLocale().toString();
    }

    /**
     * Method to check <map:act type="locale"/> invocations for local
     * customisation.
     *
     * eg.
     *
     * <pre>
     * &lt;map:act type="locale"&gt;
     *     &lt;map:parameter name="language-attribute" value="lg"/&gt;
     * &lt;/map:act&gt;
     * </pre>
     */
    private void checkParams(Parameters par) {
        langAttr = par.getParameter(LANG_ATTR, langAttr);
        countryAttr = par.getParameter(COUNTRY_ATTR, countryAttr);
        variantAttr = par.getParameter(VARIANT_ATTR, variantAttr);
        localeAttr = par.getParameter(LOCALE_ATTR, localeAttr);

        storeInRequest = par.getParameterAsBoolean(STORE_REQUEST, storeInRequest);
        storeInSession = par.getParameterAsBoolean(STORE_SESSION, storeInSession);
        createSession = par.getParameterAsBoolean(CREATE_SESSION, createSession);
        storeInCookie = par.getParameterAsBoolean(STORE_COOKIE, storeInCookie);

        getLogger().debug("checking for local overrides:\n" +
            "  " + LANG_ATTR + " = " + langAttr + ",\n" +
            "  " + COUNTRY_ATTR + " = " + countryAttr + ",\n" +
            "  " + VARIANT_ATTR + " = " + variantAttr + ",\n" +
            "  " + LOCALE_ATTR + " = " + localeAttr + ",\n" +
            "  " + STORE_REQUEST + " = " + storeInRequest + ",\n" +
            "  " + STORE_SESSION + " = " + storeInSession + ",\n" +
            "  " + CREATE_SESSION + " = " + createSession + ",\n" +
            "  " + STORE_COOKIE + " = " + storeInCookie + "\n"
        );
    }
}
