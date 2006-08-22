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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.mock.MockCookie;
import org.apache.cocoon.environment.mock.MockSession;

/**
 * JUnit-based tests for {@link LocaleAction} class.
 * 
 * @author Andrew Stevens
 */
public class LocaleActionTestCase extends SitemapComponentTestCase {
    /*
     * Locales are looked for in following order:
     *   Locale provided as a request parameter
     *   Locale provided as a session attribute
     *   Locale provided as a cookie
     *   Locale provided using a sitemap parameter (<map:parameter name="locale" value="{1}">
     *     style parameter within the <map:act> node)
     *   Locale provided by the user agent, or server default, if use-locale == true
     *   The default locale, if specified in the action's configuration
     * First found locale will be returned.  The returned map will contain
     * {locale}, {language}, {country} & {variant}.
     */

    /**
     * Test of act method, of class org.apache.cocoon.acting.LocaleAction.
     */
    public void testFindLocale() throws Exception {
        // Test different locations for locale info in reverse order
        Parameters parameters = new Parameters();
        Map result;

        // 0. When no configuration, expect action to fail
        result = act("locale0", null, parameters);
        assertNull("Action should have failed", result);
        
        // 1. When nothing specified, use action's default constants
        result = act("locale1", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "en_US", result.get("locale"));
        assertEquals("Test for language", "en", result.get("language"));
        assertEquals("Test for country", "US", result.get("country"));
        assertEquals("Test for variant", "", result.get("variant"));
        
        // 2. Configuration
        result = act("locale2", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "de_DE_EURO", result.get("locale"));
        assertEquals("Test for language", "de", result.get("language"));
        assertEquals("Test for country", "DE", result.get("country"));
        assertEquals("Test for variant", "EURO", result.get("variant"));

        // 3. User Agent or server default
        getRequest().setLocale(new java.util.Locale("fr", "FR", "MAC"));  // only if use-locale == true in configuration
//        getRequest().setHeader("Accept-Language", "fr-FR,fr;q=0.75,en;q=0.5");
        result = act("locale3", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "fr_FR_MAC", result.get("locale"));
        assertEquals("Test for language", "fr", result.get("language"));
        assertEquals("Test for country", "FR", result.get("country"));
        assertEquals("Test for variant", "MAC", result.get("variant"));
        
        // 4. Sitemap parameter
        parameters.setParameter("locale", "zh_CN_WIN");
        result = act("locale3", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "zh_CN_WIN", result.get("locale"));
        assertEquals("Test for language", "zh", result.get("language"));
        assertEquals("Test for country", "CN", result.get("country"));
        assertEquals("Test for variant", "WIN", result.get("variant"));

        // 5. Cookie
        Map cookies = getRequest().getCookieMap();
        MockCookie mockCookie = new MockCookie();
        mockCookie.setName("locale");
        mockCookie.setValue("no_NO_B");
        cookies.put("locale", mockCookie );
        result = act("locale3", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "no_NO_B", result.get("locale"));
        assertEquals("Test for language", "no", result.get("language"));
        assertEquals("Test for country", "NO", result.get("country"));
        assertEquals("Test for variant", "B", result.get("variant"));

        // 6. Session attribute
        MockSession session = (MockSession) getRequest().getSession();
        session.setAttribute("locale", "th_TH_TH");
        result = act("locale3", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "th_TH_TH", result.get("locale"));
        assertEquals("Test for language", "th", result.get("language"));
        assertEquals("Test for country", "TH", result.get("country"));
        assertEquals("Test for variant", "TH", result.get("variant"));

        // 7. Request parameter
        getRequest().addParameter("locale", "es_MX_POSIX");
        result = act("locale3", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for locale", "es_MX_POSIX", result.get("locale"));
        assertEquals("Test for language", "es", result.get("language"));
        assertEquals("Test for country", "MX", result.get("country"));
        assertEquals("Test for variant", "POSIX", result.get("variant"));
    }

    /**
     * Test of act method, of class org.apache.cocoon.acting.LocaleAction.
     */
    public void testStoreLocale() throws Exception {
        // Test different locations for storing locale
        Parameters parameters = new Parameters();
        Map result;
        Session session;
        Cookie cookie;

        // 1. Don't store
        result = act("locale2", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertNull("Test for request attribute", getRequest().getAttribute("locale"));
        assertNull("Test for session", getRequest().getSession(false));
        assertTrue("Test for cookie", getResponse().getCookies().isEmpty());
        
        // 2. Store, but don't create session
        result = act("locale4", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for request attribute", "no_NO_B", getRequest().getAttribute("locale"));
        assertNull("Test for session", getRequest().getSession(false));
        assertEquals("Test for cookie", 1, getResponse().getCookies().size());
        cookie = (Cookie) getResponse().getCookies().toArray()[0];
        assertEquals("Check cookie name", "locale", cookie.getName());
        assertEquals("Check cookie value", "no_NO_B", cookie.getValue());

        // 3. Store, creating session
        getRequest().reset();
        getRequest().clearSession();
        getResponse().reset();
        result = act("locale5", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for request attribute", "en_GB_SCOUSE", getRequest().getAttribute("locale"));
        session = getRequest().getSession(false);
        assertNotNull("Test for session", session);
        assertEquals("Test session attribute", "en_GB_SCOUSE", session.getAttribute("locale"));
        assertEquals("Test for cookie", 1, getResponse().getCookies().size());
        cookie = (Cookie) getResponse().getCookies().toArray()[0];
        assertEquals("Check cookie name", "locale", cookie.getName());
        assertEquals("Check cookie value", "en_GB_SCOUSE", cookie.getValue());

        // 4. Store, with existing session
        getRequest().reset();
        getRequest().clearSession();
        getResponse().reset();
        session = getRequest().getSession(true);
        result = act("locale4", null, parameters);
        assertNotNull("Action should always succeed", result);
        assertEquals("Test for request attribute", "no_NO_B", getRequest().getAttribute("locale"));
        session = getRequest().getSession(false);
        assertNotNull("Test for session", session);
        assertEquals("Test session attribute", "no_NO_B", session.getAttribute("locale"));
        assertEquals("Test for cookie", 1, getResponse().getCookies().size());
        cookie = (Cookie) getResponse().getCookies().toArray()[0];
        assertEquals("Check cookie name", "locale", cookie.getName());
        assertEquals("Check cookie value", "no_NO_B", cookie.getValue());
    }

}
