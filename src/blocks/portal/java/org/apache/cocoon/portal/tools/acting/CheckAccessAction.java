/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.tools.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.tools.PortalToolManager;
import org.apache.cocoon.portal.tools.model.User;
import org.apache.cocoon.portal.tools.service.UserrightsService;

/**
 * Check whether the current user is allowed to access the given page.
 * 
 * @version CVS $Id$
 */
public class CheckAccessAction
extends ServiceableAction
implements ThreadSafe {
    
    /**
     * The userrights service.
     */
    private UserrightsService userrightsService;

    /** 
     * Overridden from superclass.
     * 
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(
        Redirector redirector,
        SourceResolver resolver,
        Map objectModel,
        String source,
        Parameters parameters)
    throws Exception
    {
    	String name = parameters.getParameter("name", "anonymous");
    	String role = parameters.getParameter("role", "guest");
        String url = parameters.getParameter("url");
        User user = new User(name, role);
        PortalToolManager ptm = (PortalToolManager) this.manager.lookup(PortalToolManager.ROLE);
        userrightsService = ptm.getUserRightsService();
        // FIXME: replace the throw with something else
        if (!this.userrightsService.userIsAllowed(url, user)) {
            this.manager.release(ptm);
            throw new ProcessingException(
                "You are not allowed to request this page.");
        }
        this.manager.release(ptm);
        return EMPTY_MAP;
    }

}