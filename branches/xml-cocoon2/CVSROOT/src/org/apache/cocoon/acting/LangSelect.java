/**
 ****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 ****************************************************************************
 */
package org.apache.cocoon.acting;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.acting.Action;

import org.apache.avalon.Parameters;

import org.xml.sax.EntityResolver;

/**
 * LangSelect Action returns two character language code to sitemap.
 *
 * Definition in sitemap:
 * <pre>
 * &lt;map:actions&gt;
 *		&lt;map:action name="lang_select" src="org.apache.cocoon.acting.LangSelect"/&gt;
 * </pre>
 *
 * And you use it in following way:
 *
 * <pre>
 * &lt;map:match pattern="file"&gt;
 * &lt;map:act type="lang_select"&gt;
 *		&lt;map:generate src="file_{lang}.xml"/&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * {lang} is substituted with language code.
 * eg. if user selects url ../file?lang=en
 * then Sitemap engine generates file_en.xml source.
 *
 *
 * Creation date: (3.11.2000 14:32:19)
 * @author: <a href="mailto:lassi.immonen@valkeus.com">Lassi Immonen</a>
 */
public class LangSelect extends java.lang.Object implements Action {
    private final static String DEFAULT_LANG = "en";



    /**
     * Selects language if it is not set already in objectModel
     * Puts lang parameter to session and to objectModel
     * if it is not already there.
     */
    public Map act(EntityResolver resolver, Map objectModel, String source,
            Parameters par) throws Exception {

        String lang;

        if (objectModel.containsKey("lang")) {
            lang = (String)(objectModel.get("lang"));
        } else {
            lang = getLang(objectModel, par);
            objectModel.put("lang", lang);
        }

        HttpServletRequest req =
                (HttpServletRequest)(objectModel.get("request"));

        HttpSession session = req.getSession();
        if (session != null) {
            if (session.getAttribute("lang") == null) {
                session.setAttribute("lang", lang);
            }
        }

        Map m = new HashMap(1);
        m.put("lang", lang);
        return m;
    }



    /**
     * Returns two character language code by checking environment in following order
     * <ol>
     *   <li>Http request has parameter lang</li>
     *   <li>Http session has parameter lang</li>
     *   <li>Cookies has parameter lang</li>
     *   <li>User locales has matching language we are providing</li>
     *   <li>Otherwise we return default_lang from sitemap or if that is not found then 'en'</li>
     * </ol>
     * @return java.lang.String
     * @param objectModel java.util.Map
     * @param par org.apache.avalon.Parameters
     */
    public static String getLang(Map objectModel, Parameters par) {

        List langs_avail = new ArrayList();
        List langs_user = new ArrayList();

        Iterator params = par.getParameterNames();
        while (params.hasNext()) {
            String paramname = (String)(params.next());
            if (paramname.startsWith("available_lang")) {
                langs_avail.add(par.getParameter(paramname, null));
            }
        }
        String def_lang = par.getParameter("default_lang", LangSelect.DEFAULT_LANG);

        HttpServletRequest req =
                (HttpServletRequest)(objectModel.get("request"));

        String lang = null;

        lang = req.getParameter("lang");

        if (lang == null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                Object session_lang = session.getAttribute("lang");
                if (session_lang != null) {
                    lang = session_lang.toString();
                }
            }

        }

        if (lang == null) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for ( int i = 0; i < cookies.length; i++) {
                    Cookie cookie = cookies[i];
                    if (cookie.getName().equals("lang")) {
                        lang = cookie.getValue();
                    }
                }
            }
        }

        if (lang == null) {

            Enumeration locales = req.getLocales();
            while (locales.hasMoreElements()) {
                Locale locale = (Locale)(locales.nextElement());
                langs_user.add(locale.getLanguage());
            }

            boolean match = false;
            int i = 0;

            for ( ; i < langs_user.size() && !match; i++) {
                for ( int j = 0; j < langs_avail.size(); j++) {
                    if (((String)(langs_user.get(i))).equals(
                            (String)(langs_avail.get(j)))) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                lang = (String)(langs_user.get(i - 1));
            } else {
                lang = def_lang;
            }
        }
        return lang;
    }
}
