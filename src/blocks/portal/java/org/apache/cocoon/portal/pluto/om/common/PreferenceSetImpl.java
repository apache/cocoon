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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.PreferencesValidator;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceSet;
import org.apache.pluto.om.common.PreferenceSetCtrl;
import org.apache.pluto.util.StringUtils;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class PreferenceSetImpl
implements PreferenceSet, PreferenceSetCtrl, java.io.Serializable {

    private String castorPreferencesValidator; 
    private ClassLoader classLoader;
    private Set preferences = new HashSet();

    /**
     * @see org.apache.pluto.om.common.PreferenceSet#get(java.lang.String)
     */
    public Preference get(String name) {
        Iterator iterator = this.preferences.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();
            if (preference.getName().equals(name)) {
                return preference;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.common.PreferenceSet#iterator()
     */
    public Iterator iterator() {
        return this.preferences.iterator();
    }

    /**
     * @see org.apache.pluto.om.common.PreferenceSet#getPreferencesValidator()
     */
    public PreferencesValidator getPreferencesValidator() {
        if (this.classLoader == null)
            throw new IllegalStateException("Portlet class loader not yet available to load preferences validator.");

        if (castorPreferencesValidator == null)
            return null;

        try {
            Object validator = classLoader.loadClass(castorPreferencesValidator).newInstance();
            if (validator instanceof PreferencesValidator) {
                return(PreferencesValidator)validator;
            }
        } catch (Exception ignore) {
            // ignore it
        }

        return null;
    }

    /**
     * @see org.apache.pluto.om.common.PreferenceSetCtrl#add(java.lang.String, java.util.List)
     */
    public Preference add(String name, List values) {
        PreferenceImpl preference = new PreferenceImpl();
        preference.setName(name);
        preference.setValues(values);

        this.preferences.add(preference);

        return preference;
    }

    public boolean add(Preference preference) {
        return this.preferences.add(preference);
    }

    /**
     * @see org.apache.pluto.om.common.PreferenceSetCtrl#remove(java.lang.String)
     */
    public Preference remove(String name) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();
            if (preference.getName().equals(name)) {
                this.preferences.remove(preference);
                return preference;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.common.PreferenceSetCtrl#remove(org.apache.pluto.om.common.Preference)
     */
    public void remove(Preference preference) {
        this.preferences.remove(preference);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(50);
        buffer.append(StringUtils.lineSeparator);
        buffer.append(getClass().toString());
        buffer.append(": ");
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            buffer.append(((PreferenceImpl)iterator.next()).toString(2));
        }
        return buffer.toString();
    }


    // additional internal methods

    public String getCastorPreferencesValidator() {
        return castorPreferencesValidator;
    }

    public void setCastorPreferencesValidator(String castorPreferencesValidator) {
        this.castorPreferencesValidator = castorPreferencesValidator;
    }

    public Set getPreferences() {
        return this.preferences;
    }

    public void setClassLoader(ClassLoader loader) {
        this.classLoader = loader;
    }

    /**
     * Makes a deep copy.
     * @see java.util.Collection#addAll(Collection)
     */
    public boolean addAll(Collection c) {
        boolean changed = false;
        Iterator it = c.iterator();
        while (it.hasNext()) {
            changed = true;
            PreferenceImpl pref = (PreferenceImpl) it.next();
            this.add(pref.getName(), pref.getClonedCastorValuesAsList());
        }

        return changed;
    }

}
