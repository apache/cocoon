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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.pluto.om.common.Preference;
import org.apache.pluto.om.common.PreferenceCtrl;
import org.apache.pluto.util.StringUtils;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PreferenceImpl.java,v 1.5 2004/06/24 11:29:56 cziegeler Exp $
 */
public class PreferenceImpl implements Preference, PreferenceCtrl, java.io.Serializable {
    private final static String NULL_VALUE = "#*!0_NULL_0!*#";
    private final static String NULL_ARRAYENTRY = "#*!1_NULL_1!*#";

    private String name;
    private ArrayList value;
    private Boolean readOnly;

    public PreferenceImpl()
    {
    }

    // Preference implementation.

    public String getName()
    {
        return name;
    }

    public Iterator getValues()
    {
        // replace the NULL_VALUE String by NULL
        if (value.contains(NULL_VALUE)) {
            return null;
        }

        ArrayList returnValue = new ArrayList(value.size());
        returnValue.addAll(value);

        // replace all NULL_ARRAYENTRY Strings by NULL
        for (int i = 0; i < returnValue.size(); i++) {
            if (NULL_ARRAYENTRY.equals(returnValue.get(i))) {
                returnValue.set(i, null);
            }
        }

        return returnValue.iterator();
    }

    public boolean isReadOnly()
    {
        if (readOnly == null) {
            return false;
        }
        return readOnly.booleanValue();
    }

    public boolean isValueSet() {
        return value != null;
    }

    // PreferenceCtrl implementation.
    
    public void setName(String name)
    {
        this.name = name;
    }

    public void setValues(java.util.List _value)
    {
        if (this.value == null) {
            this.value = new ArrayList();
        } else {
            this.value.clear();
        }

        List addValue = null;

        // replace NULL by the NULL_VALUE String
        if (_value == null) {
            addValue = new ArrayList(1);
            addValue.add(NULL_VALUE); 
        } else {
            // replace all NULL by the NULL_ARRAYENTRY String
            addValue = new ArrayList(_value.size());
            addValue.addAll(_value);
            for (int i=0;i<addValue.size();i++) {
                if (addValue.get(i) == null) {
                    addValue.set(i, NULL_ARRAYENTRY);
                }
            }
        }

        this.value.addAll(addValue);
    }

    public void setReadOnly(String readOnly)
    {
        this.readOnly = new Boolean(readOnly);
    }

    // additional methods.
    
    // internal methods only used by castor

    public String getReadOnly()
    {
        if (readOnly == null) {
            return Boolean.FALSE.toString();
        }
        return readOnly.toString();
    }

    public Collection getCastorValues() 
    {
        return value;
    }

    public void setCastorValues(Collection _value) 
    {
        if (value == null) {
            value = new ArrayList();
        } else {
            value.clear();
        }
        value.addAll(_value);
    }

    protected List getClonedCastorValuesAsList()
    {
        List returnValue = new ArrayList(value.size());

        Iterator iter = value.iterator();
        while (iter.hasNext()) {
            String value = (String) iter.next();
            returnValue.add(value);
        }
        return returnValue;
    }

    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        StringBuffer buffer = new StringBuffer(50);
        StringUtils.newLine(buffer,indent);
        buffer.append(getClass().toString());
        buffer.append(": name='");
        buffer.append(name);
        buffer.append("', value='");

        if (value == null) {
            buffer.append("null");
        } else {
            StringUtils.newLine(buffer,indent);
            buffer.append("{");
            Iterator iterator = value.iterator();
            if (iterator.hasNext()) {
                StringUtils.newLine(buffer,indent);
                buffer.append((String)iterator.next());
            }
            while (iterator.hasNext()) {
                StringUtils.indent(buffer,indent+2);
                buffer.append((String)iterator.next());
            }
            StringUtils.newLine(buffer,indent);
            buffer.append("}");
        }

        buffer.append("', isReadOnly='");
        buffer.append(isReadOnly());
        buffer.append("'");
        return buffer.toString();
    }

}
