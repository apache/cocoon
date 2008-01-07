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
package org.apache.cocoon.portal.profile;

import org.apache.excalibur.source.SourceValidity;

/**
 * This component is used for storing and retrieving (parts of) the portal profile.
 *
 * The load/save method is able to load/save the various parts of the profile.
 * The correct profile type is specified by a {@link PersistenceType}. The persistence
 * type is also used for resolving references between the profile parts.
 *
 * @see PersistenceType
 *
 * @version $Id$
 */
public interface ProfileStore {

    String PROFILETYPE_LAYOUT = "layout";
    String PROFILETYPE_LAYOUTINSTANCE = "layoutinstance";
    String PROFILETYPE_COPLETDEFINITION = "copletdefinition";
    String PROFILETYPE_COPLETINSTANCE = "copletinstance";

    /**
     * Load a profile.
     * @param key The key to identifier the profile. This key contains information
     *            like user etc.
     * @param type The type of the profile (instances, types, layouts etc. )
     * @param objectMap Map with objects which might be references by the profile.
     */
    Object loadProfile(ProfileKey key, PersistenceType type)
    throws Exception;

    /**
     * Save a profile.
     * @param key The key to identifier the profile. This key contains information
     *            like user etc.
     * @param type The type of the profile (instances, types, layouts etc. )
     * @param profile The profile itself.
     */
    void saveProfile(ProfileKey key, PersistenceType type, Object profile) throws Exception;

    /**
     * Get the validity of a profile.
     * @param key The key to identifier the profile. This key contains information
     *            like user etc.
     * @param persistenceType The type of the profile (instances, types, layouts etc. )
     */
    SourceValidity getValidity(ProfileKey key, String persistenceType);
}
