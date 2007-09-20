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
package org.apache.cocoon.components.profiler;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;

/**
 * Holds information about the environment (such as request
 * parameters and session attributes) to be stored in the ProfilerData.
 *
 * @version $Id$
 */
public class EnvironmentInfo {

  	protected Map requestParameters = new HashMap();
    protected Map sessionAttributes = new HashMap();
    protected String uri;

    public EnvironmentInfo(Environment environment)	{
        final Map objectModel = environment.getObjectModel();
        final Request request = ObjectModelHelper.getRequest(objectModel);
        
        // make a copy of the request parameters
        final Enumeration requestParameterNames = request.getParameterNames();
        while (requestParameterNames.hasMoreElements()) {
            final String paramName = (String)requestParameterNames.nextElement();
            final String rawValue = request.getParameter(paramName);
            final String value = rawValue != null ? rawValue : "null";
            this.requestParameters.put(paramName, value);
        }

        // make a copy of the session contents
        final HttpSession session = request.getSession(false);
        if (session != null) {
            final Enumeration sessionAttributeNames = session.getAttributeNames();
            while (sessionAttributeNames.hasMoreElements()) {
                final String attrName = (String)sessionAttributeNames.nextElement();
                final Object rawValue = session.getAttribute(attrName);
                final String value = rawValue != null ? rawValue.toString() : "null";
                this.sessionAttributes.put(attrName, value);
            }
        }

        this.uri = environment.getURI();
    }

    public String getURI() {
        return this.uri;
    } 

    public Map getRequestParameters() {
        return requestParameters;
    }

    public Map getSessionAttributes() {
        return sessionAttributes;
    }
}

