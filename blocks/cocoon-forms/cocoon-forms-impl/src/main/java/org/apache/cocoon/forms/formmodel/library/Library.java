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
    protected Object shared = new Object();

    protected String sourceURI;
    protected WidgetDefinitionBuilderContext context;


    public Library(LibraryManager lm) {
        manager = lm;
        context = new WidgetDefinitionBuilderContext();
        context.setLocalLibrary(this);
    }

    public void setSourceURI(String uri) {
        sourceURI = uri;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setWidgetDefinitionBuilderSelector(ServiceSelector selector) {
        this.widgetDefinitionBuilderSelector = selector;
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
     * @param librarysource the source of the library to be know as "key"
     * @return true if there was no such key used before, false otherwise
     */
    public boolean includeAs(String key, String librarysource)
    throws LibraryException {
        try {
            // library keys may not contain ":"!
            if ((!inclusions.containsKey(key) || key.indexOf(SEPARATOR) > -1) &&
                    manager.load(librarysource, sourceURI) != null) {
                inclusions.put(key, new Dependency(librarysource));
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new LibraryException("Could not include library '" + librarysource + "'", e);
        }
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
            if (inclusions.containsKey(librarykey)) {
                try {
                    return manager.load(((Dependency) inclusions.get(librarykey)).dependencyURI, sourceURI).getDefinition(definitionkey);
                } catch (Exception e) {
                    throw new LibraryException("Couldn't get Library key='" + librarykey + "' source='" + inclusions.get(librarykey) + "", e);
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
        // All child elements of the widgets element are widgets
        Element[] widgetElements = DomHelper.getChildElements(widgetsElement, FormsConstants.DEFINITION_NS);
        for (int i = 0; i < widgetElements.length; i++) {
            Element widgetElement = widgetElements[i];
            WidgetDefinition widgetDefinition = buildWidgetDefinition(widgetElement);
            addDefinition(widgetDefinition);
        }
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

    protected WidgetDefinition buildWidgetDefinition(Element widgetDefinition) throws Exception {
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

        return builder.buildWidgetDefinition(widgetDefinition,context);
    }


    /**
     * Encapsulates a uri to designate an import plus a timestamp so previously reloaded
     */
    public class Dependency {
        private String dependencyURI;
        private Object shared;

        public Dependency(String dependencySourceURI) throws Exception {
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
