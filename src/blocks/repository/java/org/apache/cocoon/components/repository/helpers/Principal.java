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

import java.util.Set;

/**
 * A Principal class to be used with a repository implementation.
 */
public class Principal {
    
    private String name;
    private String group;
    private Set roles;
    
    /**
     * creates a Principal
     * 
     * @param name  the name of the principal.
     * @param group  the group of the principal.
     * @param roles  a Set containing the roles of the principal
     */
    public Principal(String name, String group, Set roles) {
        this.name = name;
        this.group = group;
        this.roles = roles;
    }

    /**
     * get the name of the principal
     * 
     * @return  the name of the principal.
     */
    public String getName() {
        return this.name;
    }

    /**
     * get the group name of the principal
     * 
     * @return  the group name of the principal.
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * get the roles of the principal
     * 
     * @return  A Set containing the roles of the principal.
     */
    public Set getRoles() {
        return this.roles;
    }

}