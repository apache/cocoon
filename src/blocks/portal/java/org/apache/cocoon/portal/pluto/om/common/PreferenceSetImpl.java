/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
 * @version CVS $Id: PreferenceSetImpl.java,v 1.3 2004/06/07 13:10:41 cziegeler Exp $
 */
public class PreferenceSetImpl extends HashSet
implements PreferenceSet, PreferenceSetCtrl, java.io.Serializable {

    private String castorPreferencesValidator; 
    private ClassLoader classLoader;

    // PreferenceSet implementation.

    public Preference get(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();
            if (preference.getName().equals(name)) {
                return preference;
            }
        }
        return null;
    }

    public PreferencesValidator getPreferencesValidator()
    {
        if (this.classLoader == null)
            throw new IllegalStateException("Portlet class loader not yet available to load preferences validator.");

        if (castorPreferencesValidator == null)
            return null;

        try {
            Object validator = classLoader.loadClass(castorPreferencesValidator).newInstance();
            if (validator instanceof PreferencesValidator)
                return(PreferencesValidator)validator;
        } catch (Exception ignore) {
        }

        return null;
    }

    // PreferenceSetCtrl implementation.

    public Preference add(String name, List values)
    {
        PreferenceImpl preference = new PreferenceImpl();
        preference.setName(name);
        preference.setValues(values);

        super.add(preference);

        return preference;
    }

    public Preference remove(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Preference preference = (Preference)iterator.next();
            if (preference.getName().equals(name)) {
                super.remove(preference);
                return preference;
            }
        }
        return null;
    }

    public void remove(Preference preference)
    {
        super.remove(preference);
    }

    // additional methods.
    
    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": ");
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            buffer.append(((PreferenceImpl)iterator.next()).toString(indent+2));
        }
        return buffer.toString();
    }


    // additional internal methods

    public String getCastorPreferencesValidator()
    {
        return castorPreferencesValidator;
    }

    public void setCastorPreferencesValidator(String castorPreferencesValidator)
    {
        this.castorPreferencesValidator = castorPreferencesValidator;
    }

    public Collection getCastorPreferences()
    {
        return this;
    }

    public void setClassLoader(ClassLoader loader)
    {
        this.classLoader = loader;
    }

    /**
     * @see java.util.Collection#addAll(Collection)
     * makes a deep copy
     */
    public boolean addAll(Collection c) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            PreferenceImpl pref = (PreferenceImpl) it.next();
            this.add(pref.getName(), pref.getClonedCastorValuesAsList());
        }

        return true;  //always assume something changed
    }

}
