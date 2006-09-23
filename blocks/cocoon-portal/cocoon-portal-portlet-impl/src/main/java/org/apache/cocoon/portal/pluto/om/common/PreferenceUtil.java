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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.pluto.om.common.Preference;

/**
 *
 * @version $Id$
 */
public class PreferenceUtil {

    /**
     * Returns the preferences as map with name as the preference's name and value as
     * the preference object itself.
     * 
     * @param preferences the preferences to be converted
     * @return the preferences as map
     */
    static public HashMap createPreferenceMap(Collection preferences) {
        HashMap returnValue = new HashMap();
        Iterator iterator = preferences.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();            
            returnValue.put(preference.getName(), preference.getValues());
        }
        return returnValue;
    }
}
