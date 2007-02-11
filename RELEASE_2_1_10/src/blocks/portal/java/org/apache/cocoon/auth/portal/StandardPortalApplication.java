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
package org.apache.cocoon.auth.portal;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.auth.StandardApplication;
import org.apache.cocoon.auth.User;

/**
 * This is a default implementation for a portal application.
 * Note: This class belongs to cauth but has to be defined in the portal block for now.
 *       This will be cleaned up with Cocoon 2.2.
 * @version $Id$
*/
public class StandardPortalApplication
    extends StandardApplication
    implements PortalApplication {

    /** The configuration. */
    protected Map portalConfig;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(final Configuration conf) throws ConfigurationException {
        super.configure(conf);
        final Configuration config = conf.getChild("profiles");
        final Configuration[] children = config.getChildren();
        this.portalConfig = new HashMap();
        if ( children != null ) {
            for(int i=0; i < children.length; i++) {
                this.portalConfig.put(children[i].getName(), children[i].getAttribute("uri"));
            }
        }
    }

    /**
     * @see org.apache.cocoon.auth.portal.PortalApplication#getPortalConfiguration()
     */
    public Map getPortalConfiguration() {
        return this.portalConfig;
    }

    /**
     * @see org.apache.cocoon.auth.Application#userDidLogin(org.apache.cocoon.auth.User, java.util.Map)
     */
    public void userDidLogin(final User user, final Map context) {
        super.userDidLogin(user, context);
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            service.getComponentManager().getProfileManager().login();
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }
    }

    /**
     * @see org.apache.cocoon.auth.Application#userWillLogout(org.apache.cocoon.auth.User, java.util.Map)
     */
    public void userWillLogout(final User user, final Map context) {
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            service.getComponentManager().getProfileManager().logout();
        } catch (ServiceException ce) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }
        super.userWillLogout(user, context);
    }
}
