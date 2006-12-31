/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;

/**
 * Abstract class for the settings element parsers.
 *
 * @see ChildSettingsElementParser
 * @see SettingsElementParser
 * @version $Id$
 * @since 1.0
 */
public abstract class AbstractSettingsElementParser extends AbstractElementParser {

    /**
     * Get additonal includes of property directories.
     */
    protected List getPropertyIncludes(Element childSettingsElement) {
        List propertyDirs = null;
        if ( childSettingsElement != null ) {
            final Element[] propertyDirConfigs = this.getChildElements(childSettingsElement, "include-properties");
            if ( propertyDirConfigs != null && propertyDirConfigs.length > 0 ) {
                propertyDirs = new ArrayList();
                for(int i=0; i < propertyDirConfigs.length; i++) {
                    propertyDirs.add(this.getAttributeValue(propertyDirConfigs[i], "dir", null));
                }
            }
        }
        return propertyDirs;        
    }

    /**
     * Get additional properties.
     */
    protected Properties getAdditionalProperties(Element childSettingsElement) {
        Properties variables = null;
        final Element[] properties = this.getChildElements(childSettingsElement, "property");
        if ( properties != null && properties.length > 0 ) {
            variables = new Properties();
            for(int i=0; i<properties.length; i++) {
                variables.setProperty(this.getAttributeValue(properties[i], "name", null),
                                      this.getAttributeValue(properties[i], "value", null));
            }
        }
        return variables;
    }

    /**
     * Get additional includes of bean configurations.
     */
    protected List getBeanIncludes(Element childSettingsElement) {
        final List includes = new ArrayList();
        // search for includes
        if ( childSettingsElement.hasChildNodes() ) {
            final Element[] includeElements = this.getChildElements(childSettingsElement, "include-beans");
            if ( includeElements != null ) {
                for(int i = 0 ; i < includeElements.length; i++ ) {
                    final String dir = this.getAttributeValue(includeElements[i], "dir", null);
                    final boolean optional = Boolean.valueOf(this.getAttributeValue(includeElements[i], "optional", "false")).booleanValue();

                    includes.add(new IncludeInfo(dir, optional));
                }
            }
        }
        return includes;
    }

    protected static final class IncludeInfo {
        public final String dir;
        public final boolean optional;

        public IncludeInfo(String d, boolean o) {
            this.dir = d;
            this.optional = o;
        }
    }
}
