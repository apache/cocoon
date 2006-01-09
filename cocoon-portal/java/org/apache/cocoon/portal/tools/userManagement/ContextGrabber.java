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
package org.apache.cocoon.portal.tools.userManagement;

import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;

/**
 * Grabbing the context of an user, which is set in the file sunrise-user.xml
 * and is stored in the class AuthenticationContext.
 *
 * @version $Id$
 */
public class ContextGrabber {

	/**
	 * Grabbing the context of the current user
	 *
	 * @return Object of context information
	 */
	public UserBean grab(PortalService service) {
		UserBean ub = new UserBean ();
        final Map infos = service.getProfileManager().getUser().getUserInfos();
        final Iterator i = infos.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry current = (Map.Entry)i.next();
            ub.addContext(current.getKey().toString(), current.getValue().toString());
        }

		return ub;
	}
}
