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
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterCtrl;
import org.apache.pluto.util.StringUtils;

public class ParameterImpl implements Parameter, ParameterCtrl, java.io.Serializable
{

    private String name;
    private String value;
    private DescriptionSet descriptions;

    public ParameterImpl()
    {
        descriptions = new DescriptionSetImpl();
    }

    // Parameter implementation.

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.Parameter#getDescription(Locale)
     */
    public Description getDescription(Locale locale)
    {
        return descriptions.get(locale);
    }

    // ParameterCtrl implementation.

    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.om.common.ParameterCtrl#setDescriptionSet(DescriptionSet)
     */
    public void setDescriptionSet(DescriptionSet descriptions)
    {
        this.descriptions = descriptions;
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
        buffer.append(": name='");
        buffer.append(name);
        buffer.append("', value='");
        buffer.append(value);
        buffer.append("', descriptions='");
        buffer.append(((DescriptionSetImpl) descriptions).toString());
        buffer.append("'");
        return buffer.toString();
    }

    public Collection getCastorDescriptions()
    {
        return(DescriptionSetImpl)descriptions;
    }

    public void setCastorDescriptions(DescriptionSet castorDescriptions)
    {
        this.descriptions = castorDescriptions;
    }
}
