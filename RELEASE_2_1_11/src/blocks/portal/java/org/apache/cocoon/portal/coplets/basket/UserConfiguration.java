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
package org.apache.cocoon.portal.coplets.basket;

import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * This data object holds the configuration of a user for the basket
 *
 * @version CVS $Id: BasketTransformer.java 47047 2004-09-22 12:27:27Z vgritsenko $
 */
public class UserConfiguration {

    /** The attribute name used to store this configuration in the session */
    public static final String ATTR_NAME = "basket-config";
    
    protected boolean basketEnabled = true;
    protected boolean briefcaseEnabled = false;
    protected boolean folderEnabled = false;
    
    /**
     * Get/create the user configuration
     */
    public static UserConfiguration get(Map           objectModel,
                                        PortalService service) {
        final Request req = ObjectModelHelper.getRequest(objectModel);
        final Session session = req.getSession();
        UserConfiguration uc = (UserConfiguration)session.getAttribute(ATTR_NAME);
        if ( uc == null ) {
            final ProfileManager pm = service.getComponentManager().getProfileManager();
            final CopletInstanceData cid = pm.getCopletInstanceData("basket");
            if ( cid != null ) {
                uc = new UserConfiguration(cid.getAttributes());
                session.setAttribute(ATTR_NAME, uc);
            }
        }
        return uc;
    }
    
    /**
     * Constructor
     * Read the configuration from the map
     */
    public UserConfiguration(Map attributes) {
        final String enabledKinds = (String)attributes.get("basket:enabled-storages");
        if ( enabledKinds != null ) {
            this.basketEnabled = (enabledKinds.indexOf("basket") != -1);
            this.briefcaseEnabled = (enabledKinds.indexOf("briefcase") != -1);
            this.folderEnabled = (enabledKinds.indexOf("folder") != -1);
        }
    }
    
    public boolean isBasketEnabled() {
        return this.basketEnabled;
    }

    public boolean isBriefcaseEnabled() {
        return this.briefcaseEnabled;
    }

    public boolean isFolderEnabled() {
        return this.folderEnabled;
    }
}
