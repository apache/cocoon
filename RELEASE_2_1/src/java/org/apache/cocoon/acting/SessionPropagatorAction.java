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
 * @version CVS $Id: SessionPropagatorAction.java,v 1.1 2003/03/09 00:08:39 pier Exp $
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

