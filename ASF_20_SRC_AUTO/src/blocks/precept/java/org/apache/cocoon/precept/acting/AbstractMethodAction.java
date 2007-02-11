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
package org.apache.cocoon.precept.acting;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.acting.ConfigurableServiceableAction;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @since Feb 25, 2002
 * @version CVS $Id: AbstractMethodAction.java,v 1.5 2004/03/05 13:02:19 bdelacretaz Exp $
 */
public abstract class AbstractMethodAction extends ConfigurableServiceableAction {

    private static final String ACTION_METHOD_PREFIX = "do";
    private static final String ACTION_METHOD_PARAMETER = "method";
    private static final String ACTION_METHOD_REQUEST_PARAMETER_PREFIX = "cocoon-method-";

    private HashMap methodIndex = null;

    public static final String extractMethod(Request request) {
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            if (parameterName.startsWith(ACTION_METHOD_REQUEST_PARAMETER_PREFIX)) {
                return (parameterName.substring(ACTION_METHOD_REQUEST_PARAMETER_PREFIX.length()));
            }
        }
        return (null);
    }


    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        if (methodIndex == null) {
            try {
                Method[] methods = this.getClass().getMethods();
                methodIndex = new HashMap();

                int prefixLen = ACTION_METHOD_PREFIX.length();

                for (int i = 0; i < methods.length; i++) {
                    String methodName = methods[i].getName();
                    if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                        String actionName = methodName.substring(prefixLen, prefixLen + 1).toLowerCase() +
                                methodName.substring(prefixLen + 1);
                        methodIndex.put(actionName, methods[i]);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("registered method \"" + methodName + "\" as action \"" + actionName + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                throw new ConfigurationException("cannot get methods by reflection", e);
            }
        }
    }

    public Map introspection(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        return (EMPTY_MAP);
    }


    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {

        String actionMethod = parameters.getParameter(ACTION_METHOD_PARAMETER, null);

        if (actionMethod == null) {
            actionMethod = extractMethod(ObjectModelHelper.getRequest(objectModel));
            getLogger().debug("no method specified as parameter, found in request: " + String.valueOf(actionMethod));
        }

        if (actionMethod != null) {
            Method method = (Method) methodIndex.get(actionMethod);
            if (method != null) {
                getLogger().debug("calling method [" + String.valueOf(actionMethod) + "]");
                return ((Map) method.invoke(this, new Object[]{redirector, resolver, objectModel, source, parameters}));
            }
            else {
                throw new Exception("action has no method \"" + actionMethod + "\"");
            }
        }
        else {
            Request request = ObjectModelHelper.getRequest(objectModel);
            if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
                // just the first view of the page
                // call introspection
                getLogger().debug("calling introspection");
                return (introspection(redirector, resolver, objectModel, source, parameters));
            }
            else {
                getLogger().debug("already in flow - no introspection");
                return (EMPTY_MAP);
            }
        }
    }
}
