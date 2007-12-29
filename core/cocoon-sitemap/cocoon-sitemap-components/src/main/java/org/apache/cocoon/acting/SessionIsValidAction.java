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

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This action just checks if a session exists and whether the current
 * seesion is still valid.
 *
 * @cocoon.sitemap.component.documentation
 * This action just checks if a session exists and whether the current
 * seesion is still valid.
 *
 * @version $Id$
 */
public class SessionIsValidAction extends AbstractAction implements ThreadSafe
{
    /**
     * Main invocation routine.
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src,
            Parameters parameters) throws Exception {
        Request req = ObjectModelHelper.getRequest(objectModel);

        /* check session validity */
        HttpSession session = req.getSession (false);
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
