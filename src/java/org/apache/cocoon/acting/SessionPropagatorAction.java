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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This is the action used to propagate parameters into session. It
 * simply propagates given expression to the session. If session does not
 * exist, action fails. Additionaly it will make all propagated values
 * available via returned Map.
 *
 * <pre>
 * &lt;map:act type="session-propagator"&gt;
 *      &lt;paramater name="example" value="{example}"&gt;
 *      &lt;paramater name="example1" value="xxx"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * @author <a href="mailto:Martin.Man@seznam.cz">Martin Man</a>
 * @version CVS $Id: SessionPropagatorAction.java,v 1.2 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public class SessionPropagatorAction extends AbstractConfigurableAction implements ThreadSafe {

    private Object[] defaults = {};

    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        this.defaults = super.settings.keySet().toArray();
    }

    /**
     * Main invocation routine.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
                    Parameters parameters) throws Exception {
        Request req = ObjectModelHelper.getRequest(objectModel);
        HashMap actionMap = new HashMap ();

        /* check session validity */
        Session session = req.getSession (false);
        if (session == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("No session object");
            }
            return null;
        }

        try {
            String[] names = parameters.getNames();

            // parameters
            for (int i = 0; i < names.length; i++) {
                String sessionParamName = names[i];
                String value = parameters.getParameter(sessionParamName);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Propagating value "
                                      + value
                                      + " to session attribute "
                                      + sessionParamName);
                }
                session.setAttribute(sessionParamName, value);
                actionMap.put(sessionParamName, value);
            }

            // defaults, that are not overridden
            for (int i = 0; i < defaults.length; i++) {
                if (!actionMap.containsKey(defaults[i])) {
                    String sessionParamName = (String) defaults[i];
                    String value = parameters.getParameter(sessionParamName);
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Propagating value "
                                          + value
                                          + " to session attribute "
                                          + sessionParamName);
                    }  
                    session.setAttribute(sessionParamName, value);
                    actionMap.put(sessionParamName, value);
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("All params propagated " + "to session");
            }
            return Collections.unmodifiableMap(actionMap);
        } catch (Exception e) {
            getLogger().warn("exception: ", e);
        }
        return null;
    }
}

