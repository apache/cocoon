/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

/**
 * LangSelect Action returns two character language code to sitemap.
 * 
 * Definition in sitemap:
 * &lt;map:actions&gt;
 *		&lt;map:action name="lang_select" src="org.apache.cocoon.acting.LangSelect"/&gt;
 *
 * And you use it in following way:
 *
 * &lt;map:match pattern="file"&gt;
 * &lt;map:act type="lang_select"&gt; 
 *		&lt;map:generate src="file_{1}.xml"/&gt;
 * &lt;/map:act&gt; 
 *
 * {1} is substituted with language code.
 * eg. if user selects url ../file?lang=en 
 * then Sitemap engine generates file_en.xml source.
 *
 *
 * Creation date: (3.11.2000 14:32:19)
 * @author: <a href="mailto:lassi.immonen@valkeus.com">Lassi Immonen</a>
 */

import org.apache.cocoon.acting.Action;
import java.util.*;
import javax.servlet.http.*;
import org.apache.avalon.Parameters;
 
public class LangSelect implements Action {
	private final static String default_lang = "en";
/**
* Selects language if it is not set already in objectModel
* Puts lang parameter to session and to objectModel
* if it is not already there. 
*/
public java.util.List act(
	org.xml.sax.EntityResolver resolver, 
	java.util.Map objectModel, 
	String source, 
	org.apache.avalon.Parameters par)
	throws Exception {

	String lang;

	if (objectModel.containsKey("lang"))
		lang = (String)objectModel.get("lang");
	else {
		lang = getLang(objectModel, par);
		objectModel.put("lang", lang);
	}
 
	HttpServletRequest req = (HttpServletRequest) objectModel.get("request");

	HttpSession session = req.getSession();
	if (session != null) {
	    if (session.getAttribute("lang") == null)
			session.setAttribute("lang", lang);
	}
	
	List l = new Vector();
	l.add(lang);
	return l;
}
/**
 * Returns two character language code by checking environment in following order
 * 1. Http request has parameter lang
 * 2. Http session has parameter lang
 * 3. Cookies has parameter lang
 * 4. User locales has matching language we are providing
 * 5. Otherwise we return default_lang from sitemap or if that is not found then 'en'
 * @return java.lang.String
 * @param objectModel java.util.Map
 * @param par org.apache.avalon.Parameters
 */
public static String getLang(
	java.util.Map objectModel, 
	org.apache.avalon.Parameters par) {

	Vector langs_avail = new Vector();
	Vector langs_user = new Vector();

	java.util.Iterator params = par.getParameterNames();
	while (params.hasNext()) {
		String paramname = (String) params.next();
		if (paramname.startsWith("available_lang"))
			langs_avail.add(par.getParameter(paramname, null));
	}
	String def_lang = par.getParameter("default_lang", default_lang);

	HttpServletRequest req = (HttpServletRequest) objectModel.get("request");

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
			for (int i = 0; i < cookies.length; i++) {
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
			Locale locale = (Locale) locales.nextElement();
			langs_user.add(locale.getLanguage());
		}

		boolean match = false;
		int i = 0;

		for (; i < langs_user.size() && !match; i++) {
			for (int j = 0; j < langs_avail.size(); j++) {
				if (((String) langs_user.get(i)).equals((String) langs_avail.get(j))) {
					match = true;
					break;
				}
			}
		}
		if (match)
			lang = (String) langs_user.get(i - 1);
		else
			lang = def_lang;
	}
	return lang;
}
}
