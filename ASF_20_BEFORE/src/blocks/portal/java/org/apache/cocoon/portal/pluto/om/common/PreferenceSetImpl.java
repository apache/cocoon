/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.portal.pluto.om.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
 * @version CVS $Id: PreferenceSetImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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

    public Preference add(String name, Collection values)
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
            this.add(pref.getName(), pref.getClonedCastorValuesAsCollection());
        }

        return true;  //always assume something changed
    }

}
