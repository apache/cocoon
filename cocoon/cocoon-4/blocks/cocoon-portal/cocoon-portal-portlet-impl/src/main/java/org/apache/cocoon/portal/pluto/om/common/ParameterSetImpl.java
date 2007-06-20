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

import java.util.HashSet;
import java.util.Iterator;

import org.apache.pluto.om.common.Parameter;
import org.apache.pluto.om.common.ParameterSet;
import org.apache.pluto.om.common.ParameterSetCtrl;
import org.apache.pluto.util.StringUtils;

/**
 * @version $Id$
 */
public class ParameterSetImpl extends HashSet
implements ParameterSet, ParameterSetCtrl, java.io.Serializable {

    public ParameterSetImpl() {
        // nothing to do 
    }

    /**
     * @see org.apache.pluto.om.common.ParameterSet#get(java.lang.String)
     */
    public Parameter get(String name) {
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = (Parameter)iterator.next();
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * @see org.apache.pluto.om.common.ParameterSetCtrl#add(java.lang.String, java.lang.String)
     */
    public Parameter add(String name, String value) {
        ParameterImpl parameter = new ParameterImpl();
        parameter.setName(name);
        parameter.setValue(value);

        super.add(parameter);

        return parameter;
    }

    /**
     * @see org.apache.pluto.om.common.ParameterSetCtrl#remove(java.lang.String)
     */
    public Parameter remove(String name) {
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

    /**
     * @see org.apache.pluto.om.common.ParameterSetCtrl#remove(org.apache.pluto.om.common.Parameter)
     */
    public void remove(Parameter parameter) {
        super.remove(parameter);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
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
