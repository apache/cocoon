/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: SessionStateAction.java,v 1.1 2003/03/09 00:08:40 pier Exp $
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
