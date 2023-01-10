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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This is the action used to propagate parameters into session. It
 * simply propagates given expression to the session. If session does not
 * exist, action fails. Additionaly it will make all propagated values
 * available via returned Map.
 *
 * <pre>
 * &lt;map:act type="session-propagator"&gt;
 *   &lt;paramater name="example" value="{example}"&gt;
 *   &lt;paramater name="example1" value="xxx"&gt;
 * &lt;/map:act&gt;
 * </pre>
 *
 * @cocoon.sitemap.component.documentation
 * This is the action used to propagate parameters into session. It
 * simply propagates given expression to the session. If session does not
 * exist, action fails. Additionaly it will make all propagated values
 * available via returned Map.
 *
 * @version $Id$
 */
public class SessionPropagatorAction extends AbstractConfigurableAction implements ThreadSafe {

    /**
     * A private helper holding default parameter entries.
     * 
     */
    private static class Entry {
        public String key = null;
        public String value = null;

        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private List defaults;

    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        Configuration[] dflts = conf.getChildren();
        if (dflts != null) {
            this.defaults = new ArrayList(dflts.length);
            for (int i = 0; i < dflts.length; i++) {
                this.defaults.add(
                    new Entry(
                        dflts[i].getName(),
                        dflts[i].getValue()));
            }
        } else {
            this.defaults = new ArrayList(0);
        }
    }

    /**
     * Main invocation routine.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
                    Parameters parameters) throws Exception {
        Request req = ObjectModelHelper.getRequest(objectModel);
        HashMap actionMap = new HashMap ();

        /* check session validity */
        HttpSession session = req.getSession (false);
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
            for (int i = 0; i < defaults.size(); i++) {
                final Entry entry = (Entry)defaults.get(i);
                if (!actionMap.containsKey(entry.key)) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Propagating value "
                                          + entry.value
                                          + " to session attribute "
                                          + entry.key);
                    }  
                    session.setAttribute(entry.key, entry.value);
                    actionMap.put(entry.key, entry.value);
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

