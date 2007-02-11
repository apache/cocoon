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

import java.util.HashSet;
import java.util.Iterator;

import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.ParameterSetCtrl;
import org.apache.pluto.util.StringUtils;

public class ParameterSetImpl extends HashSet
implements ParameterSet, ParameterSetCtrl, java.io.Serializable {

    public ParameterSetImpl()
    {
    }

    // ParameterSet implementation.

    public Parameter get(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = (Parameter)iterator.next();
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    // ParameterSetCtrl implementation.

    public Parameter add(String name, String value)
    {
        ParameterImpl parameter = new ParameterImpl();
        parameter.setName(name);
        parameter.setValue(value);

        super.add(parameter);

        return parameter;
    }

    public Parameter remove(String name)
    {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = (Parameter)iterator.next();
            if (parameter.getName().equals(name)) {
                super.remove(parameter);
                return parameter;
            }
        }
        return null;
    }

    public void remove(Parameter parameter)
    {
        super.remove(parameter);
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
            buffer.append(((ParameterImpl)iterator.next()).toString(indent+2));
        }
        return buffer.toString();
    }     
}
