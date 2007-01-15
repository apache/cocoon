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
package org.apache.cocoon.portal.security;

import java.util.Map;

import org.apache.cocoon.auth.impl.AbstractSecurityHandler;
import org.apache.cocoon.auth.SecurityHandler;

/**
 * FIXME - We commented everything out to avoid a dependency to OJB for
 * @version $Id$
 */
public class DBSecurityHandler 
    extends AbstractSecurityHandler {

    /**
     * @see SecurityHandler#login(Map)
     */
    public org.apache.cocoon.auth.User login(Map loginContext) throws Exception {
        /*
        PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();

        try {
    		Parameters para = (Parameters) loginContext.get(ApplicationManager.LOGIN_CONTEXT_PARAMETERS_KEY);

            final Criteria criteria = new Criteria();
            criteria.addEqualTo("username", para.getParameter("name"));
            criteria.addEqualTo("password", para.getParameter("password"));
            final Query query = new QueryByCriteria(User.class, criteria);
            final Collection c = broker.getCollectionByQuery(query);

            if ( c.size() == 1 ) {
                User u = (User)c.iterator().next();
                PortalUser pUser = new PortalUser(u.getUsername());
                pUser.setUid(u.getUid());
                pUser.setFirstname(u.getFirstname());
                pUser.setLastname(u.getLastname());
                pUser.setPassword(u.getPassword());
                pUser.setRole(u.getRole());
                if ( this.getLogger().isInfoEnabled() ) {
                    this.getLogger().info("Loggedin as: " + u.getFirstname() + " " + u.getLastname() + " (" + u.getUsername() + " " + u.getRole() +")");
                }
                return pUser;
            }
        } finally {
            broker.close();
        }*/
        return null;
    }

    /**
     * @see SecurityHandler#logout(Map, org.apache.cocoon.auth.User)
     */
    public void logout(Map logoutContext, org.apache.cocoon.auth.User user) {
        // nothing to do
    }
}
