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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The <code>AbstractMultiAction</code> provides a way
 * to call methods of an action specified by
 * the <code>method</code> parameter or request parameter.
 * This can be extremly useful for action-sets or as
 * action-sets replacement.
 *
 * Example:
 * <input type="submit" name="doSave" value="Save it"/>
 * will call the method "doSave" of the MultiAction
 *
 * @author <a href="mailto:tcurdt@dff.st">Torsten Curdt</a>
 * @version CVS $Id: AbstractMultiAction.java,v 1.8 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public abstract class AbstractMultiAction extends ConfigurableServiceableAction {

    private static final String ACTION_METHOD_PREFIX = "do";
    private static final String ACTION_METHOD_PARAMETER = "method";

    private HashMap methodIndex;

    private static final String removePrefix( String name ) {
        int prefixLen = ACTION_METHOD_PREFIX.length();
        return name.substring(prefixLen, prefixLen + 1).toLowerCase() + name.substring(prefixLen + 1);
    }

    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        try {
            Method[] methods = this.getClass().getMethods();
            methodIndex = new HashMap();

            for (int i = 0; i < methods.length; i++) {
                String methodName = methods[i].getName();
                if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                    String actionName = removePrefix(methodName);
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


    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters) throws Exception {
        String actionMethod = parameters.getParameter(ACTION_METHOD_PARAMETER, null);

        if (actionMethod == null) {
            Request req = ObjectModelHelper.getRequest(objectModel);
            if (req != null) {
                // checking request for action method parameters
                String name;
                for (Enumeration e = req.getParameterNames(); e.hasMoreElements();) {
                    name = (String) e.nextElement();
                    if (name.startsWith(ACTION_METHOD_PREFIX)) {
                        if (name.endsWith(".x") || name.endsWith(".y")) {
                            name = name.substring(ACTION_METHOD_PREFIX.length(), name.length() - 2);
                        }
                        actionMethod = removePrefix(name);
                        break;
                    }
                }
            }
        }

        if((actionMethod != null) && (actionMethod.length() > 0)) {
            Method method = (Method) methodIndex.get(actionMethod);
            if (method != null) {
                try {
                    return ((Map) method.invoke(this, new Object[]{redirector, resolver, objectModel, source, parameters}));
                } catch (InvocationTargetException ite) {
                    if ((ite.getTargetException() != null) && (ite.getTargetException() instanceof Exception)) {
                        throw (Exception)ite.getTargetException();
                    } else {
                        throw ite;
                    }
                }
            } else {
                throw new Exception("action has no method \"" + actionMethod + "\"");
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("you need to specify the method with parameter \"" + ACTION_METHOD_PARAMETER + "\" or have a request parameter starting with \"" + ACTION_METHOD_PREFIX + "\"");
        }
        return null;
    }
}
