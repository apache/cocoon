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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;


/**
 * This action just checks if a session exists and whether the current
 * seesion is still valid.
 *
 * @author <a href="mailto:haul@informatik.tu-darmstadt.de">Christian Haul</a>
 * @version CVS $Id: SessionIsValidAction.java,v 1.4 2004/03/08 13:57:35 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type="Action"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="session-isvalid"
 * 
 */
public class SessionIsValidAction extends AbstractAction
{
    /**
     * Main invocation routine.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        Request req = ObjectModelHelper.getRequest(objectModel);

        /* check session validity */
        Session session = req.getSession (false);
        if (session == null) {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("No session object");
            }
            return null;
        }
        if (!req.isRequestedSessionIdValid()) {
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("Requested session id is invalid");
            }
            return null;
        }

        return EMPTY_MAP;
    }
}
