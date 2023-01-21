/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;

/**
 *
 * @version $Id$
 */
public abstract class AbstractParameters 
    implements Cloneable, Serializable {

    protected Map parameters = new LinkedMap();

    public final Map getParameters() {
        return parameters;
    }

    /**
     * Return the parameter value for the given key.
     * @param key The name of the parameter.
     * @return The value of the parameter or null.
     */
    public String getParameter(String key) {
        return (String)this.parameters.get(key);
    }

    /**
     * Set the parameter to a value.
     * @param key The name of the parameter.
     * @param value The value.
     */
    public void setParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    /**
     * Remove the parameter.
     * @param key The name of the parameter.
     * @return The old value for the parameter or null.
     */
    public String removeParameter(String key) {
        return (String)this.parameters.remove(key);
    }

    /**
     * @see java.lang.Object#clone()
     */
    protected Object clone() throws CloneNotSupportedException {
        AbstractParameters clone = (AbstractParameters)super.clone();

        clone.parameters = new LinkedMap(this.parameters);

        return clone;
    }
}
