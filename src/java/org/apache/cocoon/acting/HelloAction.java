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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Map;

/**
 * A simple Action that tracks if a <code>Session</code> object
 * has been created or not.
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Id: HelloAction.java,v 1.3 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public class HelloAction extends ServiceableAction implements ThreadSafe {

    /**
     * A simple Action that logs if the <code>Session</code> object
     * has been created
     */
    public Map act (Redirector redirector, SourceResolver resolver, Map objectModel, String src, Parameters par) throws Exception {
        Request request = ObjectModelHelper.getRequest(objectModel);
        if (request != null) {
            Session session = request.getSession (false);

            if (session != null) {
                if (session.isNew()) {
                    getLogger().debug("Session is new");
                } else {
                    getLogger().debug("Session is old");
                }
            } else {
                getLogger().debug("A session object was not created");
            }
        }

        return null;
    }
}



