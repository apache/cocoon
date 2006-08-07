/*
 * Copyright 2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This bean stores information about a complete Avalon style configuration.
 * It can be passed to an {@link XmlConfigCreator} to create a Spring like
 * configuration.
 *
 * @since 2.2
 * @version $Id$
 */
public class ConfigurationInfo {

    /** Root logger. */
    protected String rootLogger;

    /** Map for shorthand to role mapping. */
    private final Map shorthands;

    /** Map for role to default classname mapping. 
     * As soon as a component is configured for this role, the info
     * will be removed from this map.
     * @see #allRoles */
    private final Map currentRoles;

    /** Map for role to default classname mapping. This map contains
     * all definitions to be available for maps in child containers.
     * @see #currentRoles
     */
    private final Map allRoles;

    /** Map for role->key to classname mapping. */
    private final Map keyClassNames;

    /** List of components. */
    private final Map components = new HashMap();

    /** List of imports for spring configurations. */
    private final List imports = new ArrayList();

    public ConfigurationInfo() {
        this.shorthands = new HashMap();
        this.currentRoles = new HashMap();
        this.keyClassNames = new HashMap();
        this.allRoles = new HashMap();
    }

    public ConfigurationInfo(ConfigurationInfo parent) {
        this.shorthands = new HashMap(parent.shorthands);
        this.currentRoles = new HashMap();
        this.keyClassNames = new HashMap(parent.keyClassNames);
        this.allRoles = new HashMap(parent.allRoles);
    }

    public Map getComponents() {
        return components;
    }

    public String getRootLogger() {
        return rootLogger;
    }

    public void setRootLogger(String rootLogger) {
        this.rootLogger = rootLogger;
    }

    public void addComponent(ComponentInfo info) {
        this.components.put(info.getRole(), info);
    }

    public Collection getRoles() {
        return this.currentRoles.values();
    }

    public void addRole(String role, ComponentInfo info) {
        this.currentRoles.put(role, info);
        this.allRoles.put(role, info);
    }

    public ComponentInfo getRole(String role) {
        ComponentInfo info = (ComponentInfo) this.currentRoles.get(role);
        if (info == null ) {
            info = (ComponentInfo) this.allRoles.get(role);
        }
        return info;
    }

    public void removeRole(String role) {
        this.currentRoles.remove(role);
    }

    public void clearRoles() {
        this.currentRoles.clear();
    }

    public Map getShorthands() {
        return this.shorthands;
    }

    public Map getKeyClassNames() {
        return this.keyClassNames;
    }

    public void addImport(String uri) {
        this.imports.add(uri);
    }

    public List getImports() {
        return this.imports;
    }

    public String getRoleForName(String alias) {
        final String value = (String)this.shorthands.get(alias);
        if ( value != null ) {
            return value;
        }
        return alias;
    }
}
