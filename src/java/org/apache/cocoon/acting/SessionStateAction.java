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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Store the session's current state in a session attribute.
 *
 * <p> To keep track of the state of a user's session, a string is
 * stored in a session attribute in order to allow to chose between
 * different pipelines in the sitemap accordingly.</p>
 *
 * <p> For added flexibility it is possible to use sub states as
 * well. For this declare your own name for the session state
 * attribute and give the number of sublevels plus the level to
 * modify. (This is <b>one</b> based!) Sub states below the current
 * one are removed from the session so that the default sub state will
 * be reentered when the user returns. If you don't like this
 * behaviour and prefer independent sub states, use this action
 * several times with different attribute names rather than sub
 * levels. </p>
 *
 * <p><b>Global and local parameters:</b></p>
 *
 * <table border="1">
 *   <tr>
 *     <td><code>state-key-prefix</code></td>
 *     <td>String that identifies the attribute that stores the session state in the
 *     	 session object. When sublevels are used, this is a prefix ie. the
 *     	 number of the level is appended to the prefix. Example prefix is
 *     	 "<code>__sessionState</code>", sub-levels is 2, attributes
 *     	 "<code>__sessionState1</code>", "<code>__sessionState2</code>", and
 *     	 "<code>__sessionState3</code>" will be used to store the
 *     	 information.
 *     </td>
 *   </tr>
 *   <tr>
 *     <td><code>new-state</code></td>
 *     <td>String that identifies the current state</td>
 *   </tr>
 *   <tr>
 *     <td><code>sub-levels</code></td>
 *     <td>Number of sub levels to  use</td>
 *   </tr>
 *   <tr>
 *     <td><code>state-level</code></td>
 *     <td>Sub level to modify, this is <b>one</b> based</td>
 *   </tr>
 * </table>
 *
 * @see org.apache.cocoon.matching.WildcardSessionAttributeMatcher
 * @see org.apache.cocoon.selection.SessionAttributeSelector
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: SessionStateAction.java,v 1.3 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public class SessionStateAction
    extends AbstractConfigurableAction
    implements ThreadSafe {

    protected String statekey = "org.apache.cocoon.SessionState";
    protected String newstate = null;
    protected int sublevels = 0;
    protected int mylevel = 0;

    /**
     * Configures the Action.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        if (settings.containsKey("state-key-prefix")) {
            statekey = (String) settings.get("state-key-prefix");
        }
        if (settings.containsKey("new-state")) {
            newstate = (String) settings.get("new-state");
        }
        if (settings.containsKey("sub-levels")) {
            sublevels = Integer.parseInt((String) settings.get("sub-levels"));
        }
        if (settings.containsKey("state-level")) {
            mylevel = Integer.parseInt((String) settings.get("state-level"));
        }
    }

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String src,
                   Parameters par) throws Exception {

        Request request = ObjectModelHelper.getRequest(objectModel);

        // read local settings
        String newstate = par.getParameter("new-state", this.newstate);
        String statekey = par.getParameter("state-key", this.statekey);
        int sublevels = par.getParameterAsInteger("sublevels", this.sublevels);
        int mylevel = par.getParameterAsInteger("state-level", this.mylevel);

        if (newstate == null) {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().error("new-state is null");
            }
            return null;
        }

        if (request != null) {
            Session session = request.getSession(false);

            if (session != null && request.isRequestedSessionIdValid()) {
                String oldstate = null;
                if (sublevels == 0) {
                    oldstate = (String) session.getAttribute(statekey);
                    session.setAttribute(statekey, newstate);
                    if (this.getLogger().isDebugEnabled()) {
                         getLogger().debug(statekey + "=" + newstate);
                    }

                } else { // sublevels != 0
                    oldstate = (String)session.getAttribute(statekey + mylevel);
                    for (int i = mylevel + 1; i <= sublevels; i++) {
                        session.removeAttribute(statekey + i);
                        if (this.getLogger().isDebugEnabled()) {
                            getLogger().debug("Remove " + statekey + i);
                        }
                    }
                    session.setAttribute(statekey + mylevel, newstate);
                    if (this.getLogger().isDebugEnabled()) {
                        getLogger().debug(statekey + mylevel + "=" + newstate);
                    }
                }
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("Transition " + oldstate + " -> " + newstate);
                }

                HashMap map = new HashMap(1);
                map.put("newstate", newstate);
                return map;
            } else {
                getLogger().warn(
                    "A session object was not present or no longer valid");
                return null;
            }
        } else {
            getLogger().warn("No request object");
            return null;
        }
    }
}
