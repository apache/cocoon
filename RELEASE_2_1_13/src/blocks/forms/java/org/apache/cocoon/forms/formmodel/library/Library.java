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
package org.apache.cocoon.forms.formmodel.library;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilderContext;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationAttributes;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

/**
 * Form model library.
 *
 * @version $Id$
 */
public class Library extends AbstractLogEnabled {

    public static final String SEPARATOR = ":";

    // managed instances
    protected ServiceSelector widgetDefinitionBuilderSelector;

    // own references
    protected LibraryManager manager;

    // own instances
    protected Map definitions = new HashMap();
    protected Map inclusions = new HashMap();

    // shared object with dependencies
    protected final Object shared = new Object();

    protected String sourceURI;


    public Library(LibraryManager lm, ServiceSelector builderSelector) {
        manager = lm;
        widgetDefinitionBuilderSelector = builderSelector;
    }

    public void setSourceURI(String uri) {
        sourceURI = uri;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public boolean dependenciesHaveChanged() throws LibraryException {
        Iterator i = this.inclusions.values().iterator();
        while (i.hasNext()) {
            Dependency dep = (Dependency) i.next();
            if (!dep.isValid()) {
                return true;
            }
        }

        return false;
    }

    /**
     * "Registers" a library to be referenced later under a certain key or prefix.
     * Definitions will be accessible locally through prefixing: "prefix:definitionid"
     *
     * @param key the key
     * @param sourceURI the source of the library to be know as "key"
     * @return true if there was no such key used before, false otherwise
     * @throws LibraryException if unable to load included library
     */
    public boolean includeAs(String key, String sourceURI)
    throws LibraryException {
        if (!inclusions.containsKey(key) || key.indexOf(SEPARATOR) > -1) {
            manager.load(sourceURI, this.sourceURI);
            inclusions.put(key, new Dependency(sourceURI));
            return true;
        }
        return false;
    }

    public WidgetDefinition getDefinition(String key) throws LibraryException {
        String librarykey = null;
        String definitionkey = key;

        if (key.indexOf(SEPARATOR) > -1) {
            String[] parts = StringUtils.split(key, SEPARATOR);
            librarykey = parts[0];
            definitionkey = parts[1];
            for (int i = 2; i < parts.length; i++) {
                definitionkey += SEPARATOR + parts[i];
            }
        }

        if (librarykey != null) {
            Dependency dependency = (Dependency) inclusions.get(librarykey);
            if (dependency != null) {
                try {
                    return manager.load(dependency.dependencyURI, sourceURI).getDefinition(definitionkey);
                } catch (Exception e) {
                    throw new LibraryException("Couldn't get library '" + librarykey + "' source='" + dependency + "'", e);
                }
            } else {
                throw new LibraryException("Library '" + librarykey + "' does not exist! (lookup: '" + key + "')");
            }
        } else {
            return (WidgetDefinition) definitions.get(definitionkey);
        }
    }

    public void buildLibrary(Element libraryElement) throws Exception {
        sourceURI = LocationAttributes.getURI(libraryElement);
        Element widgetsElement = DomHelper.getChildElement(libraryElement, FormsConstants.DEFINITION_NS, "widgets", true);

        WidgetDefinitionBuilderContext context = new WidgetDefinitionBuilderContext(this);

        // All child elements of the widgets element are widgets
        Element[] widgetElements = DomHelper.getChildElements(widgetsElement, FormsConstants.DEFINITION_NS);
        for (int i = 0; i < widgetElements.length; i++) {
            Element widgetElement = widgetElements[i];
            buildWidgetDefinition(widgetElement, context);
        }
    }

    private void buildWidgetDefinition(Element widgetDefinition, WidgetDefinitionBuilderContext context)
    throws Exception {
        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder;
        try {
            builder = (WidgetDefinitionBuilder) widgetDefinitionBuilderSelector.select(widgetName);
        } catch (ServiceException e) {
            throw new LibraryException("Unknown kind of widget '" + widgetName + "'.",
                                       e, DomHelper.getLocationObject(widgetDefinition));
        }

        context.setSuperDefinition(null);
        String extend = DomHelper.getAttribute(widgetDefinition, "extends", null);
        if (extend != null) {
            context.setSuperDefinition(getDefinition(extend));
        }

        WidgetDefinition definition = builder.buildWidgetDefinition(widgetDefinition, context);
        addDefinition(definition);
    }

    public void addDefinition(WidgetDefinition definition) throws LibraryException {
        if (definition == null) {
            return;
        }

        if (definitions.containsKey(definition.getId())) {
            throw new LibraryException("Library already contains a widget with this ID!");
        }

        // let the definition know where it comes from
        definition.setEnclosingLibrary(this);

        // add def to our list of defs
        definitions.put(definition.getId(), definition);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + ": Added definition '" + definition.getId() + "'");
        }
    }

    
    /**
     * Encapsulates a uri to designate an import plus a timestamp so previously reloaded
     */
    protected class Dependency {
        private final String dependencyURI;
        private final Object shared;

        public Dependency(String dependencySourceURI) throws LibraryException {
            this.dependencyURI = dependencySourceURI;
            Library lib = manager.load(this.dependencyURI, sourceURI);
            this.shared = lib.shared;
        }

        public boolean isValid() throws LibraryException {
            Library lib = manager.get(dependencyURI, sourceURI);
            return lib != null && this.shared == lib.shared;
        }
    }
}
