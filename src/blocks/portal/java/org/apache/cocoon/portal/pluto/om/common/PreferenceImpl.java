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
 * @version CVS $Id: PreferenceImpl.java,v 1.1 2004/01/22 14:01:20 cziegeler Exp $
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
            if (NULL_ARRAYENTRY.equals((String)returnValue.get(i))) {
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

    public void setValues(Collection _value)
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

    protected Collection getClonedCastorValuesAsCollection()
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
