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
package org.apache.cocoon.components.profiler;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;

/**
 * Holds information about the environment (such as request
 * parameters and session attributes) to be stored in the ProfilerData.
 *
 * @author <a href="mailto:bruno@outerthought.org">Bruno Dumon</a>,
 * @version CVS $Id: EnvironmentInfo.java,v 1.2 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class EnvironmentInfo {

  	HashMap requestParameters = new HashMap();
	  HashMap sessionAttributes = new HashMap();
  	String uri;
	  String uriPrefix;

    public EnvironmentInfo(Environment environment)	{

		    Map objectModel = environment.getObjectModel();
    		Request request = ObjectModelHelper.getRequest(objectModel);

		    // make a copy of the request parameters
    		Enumeration requestParameterNames = request.getParameterNames();
		    while (requestParameterNames.hasMoreElements()) {
      			String paramName = (String)requestParameterNames.nextElement();
      			String rawValue = request.getParameter(paramName);
			      String value = rawValue != null ? rawValue : "null";
      			requestParameters.put(paramName, value);
		    }

    		// make a copy of the session contents
    		Session session = request.getSession(false);
    		if (session != null) {
      			Enumeration sessionAttributeNames = session.getAttributeNames();
      			while (sessionAttributeNames.hasMoreElements()) {
        				String attrName = (String)sessionAttributeNames.nextElement();
        				Object rawValue = session.getAttribute(attrName);
				        String value = rawValue != null ? rawValue.toString() : "null";
        				sessionAttributes.put(attrName, value);
			      }
    		}

    		uri = environment.getURI();
		    uriPrefix = environment.getURIPrefix();
    }

    public String getURI() {
        return uri;
    } 

    public Map getRequestParameters() {
        return requestParameters;
    }

    public Map getSessionAttributes() {
        return sessionAttributes;
    }
}

