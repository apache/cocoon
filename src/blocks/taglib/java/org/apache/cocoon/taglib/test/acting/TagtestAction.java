/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.apache.cocoon.taglib.test.acting;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.ComposerAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

/**
 * @version 	1.0
 * @author
 */
public class TagtestAction extends ComposerAction
{

	/*
	 * @see Action#act(Redirector, SourceResolver, Map, String, Parameters)
	 */
	public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters par)
		throws Exception
	{
		Request request = (Request) ObjectModelHelper.getRequest(objectModel);
		Session session = request.getSession();
		Enumeration locales = request.getLocales();
		List info = new ArrayList();
		
		request.setAttribute("BrowserLocales", locales);
		
		info.add(request.getRemoteAddr());
		info.add(request.getRemoteHost());
		info.add(request.getRemoteUser());
		info.add(request.getContentType());
		info.add(request.getLocale());
	
		session.setAttribute("RequestInfo", info);
			
		return null;
	}

}
