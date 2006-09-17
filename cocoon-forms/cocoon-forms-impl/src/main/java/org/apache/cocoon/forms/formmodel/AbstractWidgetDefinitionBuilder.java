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
package org.apache.cocoon.forms.formmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.DatatypeManager;
import org.apache.cocoon.forms.event.CreateListener;
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilder;
import org.apache.cocoon.forms.expression.ExpressionManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.validation.WidgetValidatorBuilder;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for WidgetDefinitionBuilders. Provides functionality
 * common to many implementations.
 *
 * @version $Id$
 */
public abstract class AbstractWidgetDefinitionBuilder implements WidgetDefinitionBuilder, Serviceable, Disposable {

    protected ServiceSelector widgetDefinitionBuilderSelector;
    protected ServiceSelector widgetValidatorBuilderSelector;
    protected ServiceSelector widgetListenerBuilderSelector;
    protected DatatypeManager datatypeManager;
    protected ExpressionManager expressionManager;
    protected ServiceManager serviceManager;

    protected WidgetDefinitionBuilderContext context;


    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        this.widgetDefinitionBuilderSelector = (ServiceSelector) serviceManager.lookup(WidgetDefinitionBuilder.class.getName() + "Selector");
        this.datatypeManager = (DatatypeManager) serviceManager.lookup(DatatypeManager.ROLE);
        this.expressionManager = (ExpressionManager) serviceManager.lookup(ExpressionManager.ROLE);
        this.widgetValidatorBuilderSelector = (ServiceSelector) serviceManager.lookup(WidgetValidatorBuilder.ROLE + "Selector");
        this.widgetListenerBuilderSelector = (ServiceSelector) serviceManager.lookup(WidgetListenerBuilder.ROLE + "Selector");
    }

    public void dispose() {
        this.serviceManager.release(this.widgetDefinitionBuilderSelector);
        this.widgetDefinitionBuilderSelector = null;
        this.serviceManager.release(this.datatypeManager);
        this.datatypeManager = null;
        this.serviceManager.release(this.expressionManager);
        this.expressionManager = null;
        this.serviceManager.release(this.widgetValidatorBuilderSelector);
        this.widgetValidatorBuilderSelector = null;
        this.serviceManager = null;
    }

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        // so changes don't pollute upper levels
        this.context = new WidgetDefinitionBuilderContext(context);

        WidgetDefinition def = buildWidgetDefinition(widgetElement);

        // register this class with the local library, if any.
        if (DomHelper.getAttributeAsBoolean(widgetElement, "register", false) &&
                this.context != null &&
                this.context.getLocalLibrary() != null) {
            this.context.getLocalLibrary().addDefinition(def);
        }

        this.context = null;
        return def;
    }

    protected void setupDefinition(Element widgetElement, AbstractWidgetDefinition definition)
    throws Exception {
        // location
        definition.setLocation(DomHelper.getLocationObject(widgetElement));

        if (this.context.getSuperDefinition() != null) {
            definition.initializeFrom(this.context.getSuperDefinition());
        }

        setCommonProperties(widgetElement, definition);
        setValidators(widgetElement, definition);
        setCreateListeners(widgetElement, definition);
    }

    private void setCommonProperties(Element widgetElement, AbstractWidgetDefinition widgetDefinition)
    throws Exception {

        // id
        if (widgetDefinition instanceof FormDefinition) {
            // FormDefinition is the *only* kind of widget that has an optional id
            widgetDefinition.setId(DomHelper.getAttribute(widgetElement, "id", ""));
        } else {
            String id = DomHelper.getAttribute(widgetElement, "id");
            if (id.length() < 1) {
                throw new Exception("Missing id attribute on element '" + widgetElement.getTagName() + "' at " +
                                    DomHelper.getLocation(widgetElement));
            }
            if (id.indexOf('/') != -1 || id.indexOf('.') != -1) {
                throw new Exception("A widget name cannot contain '.' or '/' as this conflicts with widget paths, at " +
                                    DomHelper.getLocation(widgetElement));
            }
            // NewDefinitions are allowed to have a : in their id because they can look up
            // class widgets from the library directly
            if (id.indexOf(':') != -1 && !(widgetDefinition instanceof NewDefinition)) {
                throw new Exception("A widget name cannot contain ':' as this conflicts with library prefixes, at " +
                                    DomHelper.getLocation(widgetElement));
            }
            widgetDefinition.setId(id);
        }

        // state
        String stateValue = DomHelper.getAttribute(widgetElement, "state", null);
        if (stateValue != null) {
            WidgetState state = WidgetState.stateForName(stateValue);
            if (state == null) {
                throw new Exception ("Unknown value '" + stateValue +"' for state attribute at " +
                                     DomHelper.getLocation(widgetElement));
            }
            widgetDefinition.setState(state);
        }

        // attributes
        Element attrContainer = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "attributes", false);
        if (attrContainer != null) {
            // There's a <fd:attributes> element. Get its <fd:attribute> children
            Element[] attrs = DomHelper.getChildElements(attrContainer, FormsConstants.DEFINITION_NS, "attribute");
            if (attrs != null && attrs.length > 0) {
                // We actually do have some
                Map attrMap = new HashMap();
                for (int i = 0; i < attrs.length; i++) {
                    attrMap.put(DomHelper.getAttribute(attrs[i], "name"), DomHelper.getAttribute(attrs[i], "value"));
                }
                widgetDefinition.setAttributes(attrMap);
            }
        }
    }

    protected WidgetDefinition buildAnotherWidgetDefinition(Element widgetDefinition)
    throws Exception {
        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder;
        try {
            builder = (WidgetDefinitionBuilder)widgetDefinitionBuilderSelector.select(widgetName);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown kind of widget '" + widgetName + "' at " +
                                         DomHelper.getLocation(widgetDefinition), e);
        }

        return builder.buildWidgetDefinition(widgetDefinition, this.context);
    }

    protected List buildEventListeners(Element widgetElement, String elementName, Class listenerClass)
    throws Exception {
        List result = null;
        Element listenersElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, elementName);
        if (listenersElement != null) {
            NodeList list = listenersElement.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element listenerElement = (Element)list.item(i);
                    WidgetListenerBuilder builder;
                    try {
                        builder = (WidgetListenerBuilder) widgetListenerBuilderSelector.select(listenerElement.getLocalName());
                    } catch (ServiceException e) {
                        throw new CascadingException("Unknown kind of eventlistener '" + listenerElement.getLocalName() +
                                                     "' at " + DomHelper.getLocation(listenerElement), e);
                    }
                    WidgetListener listener = builder.buildListener(listenerElement, listenerClass);
                    widgetListenerBuilderSelector.release(builder);
                    if (result == null) {
                        result = new ArrayList();
                    }
                    result.add(listener);
                }
            }
        }

        return result == null ? Collections.EMPTY_LIST : result;
    }

    protected void setDisplayData(Element widgetElement, AbstractWidgetDefinition widgetDefinition)
    throws Exception {
        final String[] names = {"label", "help", "hint"};
        Map displayData = new HashMap(names.length);
        for (int i = 0; i < names.length; i++) {
            XMLizable data = null;
            Element dataElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, names[i]);
            if (dataElement != null) {
                data = DomHelper.compileElementContent(dataElement);
            }

            // NOTE: We put also null values in the may in order to test their existence
            //       (see AbstractWidgetDefinition.generateDisplayData)
            displayData.put(names[i], data);
        }

        widgetDefinition.setDisplayData(displayData);
    }

    private void setValidators(Element widgetElement, AbstractWidgetDefinition widgetDefinition)
    throws Exception {
        Element validatorElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "validation");
        if (validatorElement != null) {
            NodeList list = validatorElement.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)list.item(i);
                    String name = element.getLocalName();
                    WidgetValidatorBuilder builder;
                    try {
                        builder = (WidgetValidatorBuilder)this.widgetValidatorBuilderSelector.select(name);
                    } catch(ServiceException e) {
                        throw new CascadingException("Unknown kind of validator '" + name + "' at " +
                                                     DomHelper.getLocation(element), e);
                    }

                    widgetDefinition.addValidator(builder.build(element, widgetDefinition));
                    widgetValidatorBuilderSelector.release(builder);
                }
            }
        }
    }

    private void setCreateListeners(Element widgetElement, AbstractWidgetDefinition widgetDefinition)
    throws Exception {
        Iterator i = buildEventListeners(widgetElement, "on-create", CreateListener.class).iterator();
        while (i.hasNext()) {
            widgetDefinition.addCreateListener((CreateListener)i.next());
        }
    }
}
