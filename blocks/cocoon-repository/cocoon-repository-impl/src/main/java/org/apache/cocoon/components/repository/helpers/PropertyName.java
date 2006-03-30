/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.repository.helpers;

/**
 * A PropertyName object intentifies a specific property.
 */
public class PropertyName {

    private String name;
    private String namespace;
    
    /**
     * creates a PropertyName
     *
     * @param name  the name of the property.
     * @param namespace  the namespace of the property.
     */
    public PropertyName(String name, String namespace) {
        this.name = name;
        this.namespace = namespace;
    }

    /**
     * get the name of the property
     * 
     * @return  the name of the property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * get the namespace of the property
     * 
     * @return  the namespace of the property.
     */
    public String getNamespace() {
        return this.namespace;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (this.namespace+":"+this.name).hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return (obj != null && (obj instanceof PropertyName)
                && this.name.equals(((PropertyName)obj).getName())
                && this.namespace.equals(((PropertyName)obj).getNamespace()));
    }

}
