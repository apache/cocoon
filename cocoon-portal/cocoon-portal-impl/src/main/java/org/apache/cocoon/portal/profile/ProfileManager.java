/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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

import java.util.Collection;
import java.util.List;

import org.apache.cocoon.portal.coplet.CopletBaseData;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.scratchpad.Profile;

/**
 * The profile manager provides access to the portal profile (or parts
 * of it). The portal profile stores all information about the portal
 * view of the current user, like the layout and the contained coplets.
 *
 * @version $Id$
 */
public interface ProfileManager {

    String ROLE = ProfileManager.class.getName();

    /**
     * Get the portal layout defined by the layout key. This
     * usually addresses the layout profile.
     * With the optional subKey it's possible to retrieve
     * a specific layout object in the profile defined by
     * the layout key.
     * FIXME - Add a method to get the profile for a key and then
     *         the layout can be retrieved from the profile.
     * @param layoutKey A key describing the layout or null for the default
     * @param layoutID    The id of a layout object or null for the root object
     * @return The layout
     */
	Layout getPortalLayout(String layoutKey, String layoutID);

    /**
     * FIXME - This will be part of the profile interface.
     */
    CopletInstanceData getCopletInstanceData(String copletID);

    /**
     * FIXME - This will be part of the profile interface.
     */
    List getCopletInstanceData(CopletData data);

    /**
     * Return the coplet data object
     */
    CopletData getCopletData(String copletDataId);

    /**
     * New layouts have to be registered using this method.
     */
    void register(Layout layout);

    /**
     * New layouts have to be unregistered using this method.
     */
    void unregister(Layout layout);

    /**
     * Save the profile. Usually this just calls {@link #saveUserCopletInstanceDatas(String)}
     * and {@link #saveUserLayout(String)}, but implementations are free to
     * implement this method in a different way.
     * @param layoutKey
     */
    void saveUserProfiles(String layoutKey);

    /**
     * Get all instances
     * FIXME - This will be part of the profile interface.
     */
    Collection getCopletInstanceDatas();

    /**
     * Get all coplets.
     */
    Collection getCopletDatas();

    /**
     * Get all coplet base datas.
     */
    Collection getCopletBaseDatas();

    /**
     * Get a specific coplet base data.
     */
    CopletBaseData getCopletBaseData(String id);

    /**
     * Get current user information
     */
    PortalUser getUser();

    /**
     * Get the profile for the given profile key.
     * @return The profile or null if the profile could not be found.
     * @throws ProfileException if an error occurs.
     */
    Profile getProfile(String profileName)
    throws ProfileException;
}
