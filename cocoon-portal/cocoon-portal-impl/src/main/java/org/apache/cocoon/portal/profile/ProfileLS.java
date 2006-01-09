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
package org.apache.cocoon.portal.profile;

import java.util.Map;

import org.apache.excalibur.source.SourceValidity;

/**
 * This component is used for loading/saving of a profile.
 * @version $Id$
 */
public interface ProfileLS {

    /** Component role */
    String ROLE = ProfileLS.class.getName();

    /** This parameter is used during loading to resolve references */
    String PARAMETER_OBJECTMAP = "objectmap";
    /** This parameter is used to define the profiletype */
    String PARAMETER_PROFILETYPE = "profiletype";

    String PROFILETYPE_LAYOUT = "layout";
    String PROFILETYPE_COPLETBASEDATA = "copletbasedata";
    String PROFILETYPE_COPLETDATA = "copletdata";
    String PROFILETYPE_COPLETINSTANCEDATA = "copletinstancedata";

    /**
     * Load a profile
     */
    Object loadProfile(Object key, Map parameters) throws Exception;  

    /**
     * Save a profile
     */
    void saveProfile(Object key, Map parameters, Object profile) throws Exception;  

    SourceValidity getValidity(Object key, Map parameters);
}
