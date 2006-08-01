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

import org.apache.cocoon.portal.coplet.CopletDefinition;
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.CopletType;
import org.apache.cocoon.portal.layout.Layout;

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
     * @param profileName A key describing the layout or null for the default
     * @param layoutID    The id of a layout object or null for the root object
     * @return The layout
     */
	Layout getPortalLayout(String profileName, String layoutID);

    /**
     * Get the coplet instance with the given id.
     * @return The coplet instance or null.
     */
    CopletInstance getCopletInstance(String copletID);

    /**
     * Get all coplet instances of the given coplet for the current user.
     */
    List getCopletInstances(CopletDefinition data);

    /**
     * Return the coplet data object
     */
    CopletDefinition getCopletDefinition(String copletDataId);

    /**
     * Save the profile. Usually this just calls {@link #saveUserCopletInstanceDatas(String)}
     * and {@link #saveUserLayout(String)}, but implementations are free to
     * implement this method in a different way.
     * @param layoutKey
     */
    void saveUserProfiles(String profileName);

    /**
     * Get all coplet instances for the current user.
     */
    Collection getCopletInstances();

    /**
     * Get all coplets definitions for the current user.
     */
    Collection getCopletDefinitions();

    /**
     * Get all coplet types for the current user.
     */
    Collection getCopletTypes();

    /**
     * Get a specific coplet type for the current user.
     */
    CopletType getCopletType(String id);

    /**
     * Get current user information.
     */
    PortalUser getUser();
}
