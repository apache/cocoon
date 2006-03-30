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

    /** Map for role to default classname mapping. */
    private final Map classNames;

    /** Map for role->key to classname mapping. */
    private final Map keyClassNames;

    /** List of components. */
    private final Map components = new HashMap();

    /** List of imports for spring configurations. */
    private final List imports = new ArrayList();

    public ConfigurationInfo() {
        this.shorthands = new HashMap();
        this.classNames = new HashMap();
        this.keyClassNames = new HashMap();
    }

    public ConfigurationInfo(ConfigurationInfo parent) {
        this.shorthands = new HashMap(parent.shorthands);
        this.classNames = new HashMap(parent.classNames);
        this.keyClassNames = new HashMap(parent.keyClassNames);
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

    public Map getClassNames() {
        return this.classNames;
    }

    public void clearClassNames() {
        this.classNames.clear();
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
