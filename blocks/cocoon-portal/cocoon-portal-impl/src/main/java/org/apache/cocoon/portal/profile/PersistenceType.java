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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This component is used for loading/saving of a profile.
 *
 * The load/save method is able to load/save the various parts of the profile.
 * The correct profile type is specified by a constant. For resolving
 * references during loading an object map is passsed to the loadProfile method.
 * This object map varies depending on the profile type:
 * PROFILETYPE_LAYOUT: A map with layout types.
 * PROFILETYPE_LAYOUTINSTANCE: -
 * PROFILETYPE_COPLETDEFINITION: A map with coplet types.
 * PROFILETYPE_COPLETINSTANCE: A map with coplet definitions
 *
 * @version $Id$
 */
public class PersistenceType {

    public static final String PERSISTENCETYPE_LAYOUT = "layout";
    public static final String PPERSISTENCETYPE_LAYOUTINSTANCE = "layoutinstance";
    public static final String PERSISTENCETYPE_COPLETDEFINITION = "copletdefinition";
    public static final String PERSISTENCETYPE_COPLETINSTANCE = "copletinstance";

    protected final String type;

    protected final Map references = new HashMap();

    public PersistenceType(final String t) {
        this.type = t;
    }

    public String getType() {
        return this.type;
    }

    public void setReferences(final String fieldKey, final Map objects) {
        // TODO - check the fieldKey for the type
        this.references.put(fieldKey, objects);
    }

    public Map getReferences(final String fieldKey) {
        return (Map) this.references.get(fieldKey);
    }

    public Object getReference(final String fieldKey, final String key) {
        final Map objects = (Map)this.references.get(fieldKey);
        if ( objects != null ) {
            return objects.get(key);
        }
        return null;
    }

    public Collection getReferenceFieldKeys() {
        return this.references.keySet();
    }
}
