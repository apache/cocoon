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
package org.apache.cocoon.portal.tools.helper;

import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 *
 * @version $Id$
 */
public class PortalObjects {

	private PortalService portalService;
	private PortalComponentManager componentManager;
	private ProfileManager profileManager;
	private Layout portalLayout;

	public PortalObjects(PortalService portalService) {
		this.portalService = portalService;
		this.componentManager = portalService;
		this.profileManager = componentManager.getProfileManager();
		this.portalLayout = profileManager.getPortalLayout(null, null);
	}

	public PortalComponentManager getComponentManager() {
		return componentManager;
	}

	public Layout getPortalLayout() {
		return portalLayout;
	}

	public PortalService getPortalService() {
		return portalService;
	}

	public ProfileManager getProfileManager() {
		return profileManager;
	}
}
