/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.profile;

import java.util.List;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;

/**
 * The profile manager.
 * Via this component you can get the profile (or parts of it) of the
 * current 'user'.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: ProfileManager.java,v 1.10 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public interface ProfileManager extends Component {
    
    String ROLE = ProfileManager.class.getName();
    
    /**
     * Get the portal layout defined by the layout key. This
     * usually addresses the layout profile.
     * With the optional subKey it's possible to retrieve
     * a specific layout object in the profile defined by
     * the layout key.
     * @param layoutKey A key describing the layout or null for the default
     * @param layoutID    The id of a layout object or null for the root object
     * @return The layout
     */
	Layout getPortalLayout(String layoutKey, String layoutID);
    
    /**
     * FIXME this is for the full-screen function
     */
    void setEntryLayout(Layout object);
    Layout getEntryLayout();
    
    /**
     * Change the default layout key for most functions
     */
    void setDefaultLayoutKey(String layoutKey);
    
    /**
     * Get the default layout key
     */
    String getDefaultLayoutKey();
    
    CopletInstanceData getCopletInstanceData(String copletID);
    
    List getCopletInstanceData(CopletData data);
    
    /**
     * Return the coplet data object
     */
    CopletData getCopletData(String copletDataId);
    
    void login();
    
    void logout();
    
    void register(CopletInstanceData coplet);
    
    void unregister(CopletInstanceData coplet);

    void register(Layout layout);
    
    void unregister(Layout layout);

    /**
     * Save the profile
     */
    void saveUserProfiles();
    
}
