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
import org.apache.cocoon.forms.Constants;
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

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        this.widgetDefinitionBuilderSelector = (ServiceSelector) serviceManager.lookup(WidgetDefinitionBuilder.class.getName() + "Selector");
        this.datatypeManager = (DatatypeManager) serviceManager.lookup(DatatypeManager.ROLE);
        this.expressionManager = (ExpressionManager) serviceManager.lookup(ExpressionManager.ROLE);
        this.widgetValidatorBuilderSelector = (ServiceSelector) serviceManager.lookup(WidgetValidatorBuilder.ROLE + "Selector");
        this.widgetListenerBuilderSelector = (ServiceSelector) serviceManager.lookup(WidgetListenerBuilder.ROLE + "Selector");
    }
    
    protected void setupDefinition(Element widgetElement, AbstractWidgetDefinition definition) throws Exception {
        setCommonProperties(widgetElement, definition);
        setValidators(widgetElement, definition);
        setCreateListeners(widgetElement, definition);
    }

    private void setCommonProperties(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
        // location
        widgetDefinition.setLocation(DomHelper.getLocation(widgetElement));

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
            widgetDefinition.setId(id);
        }

        // state
        String stateValue = DomHelper.getAttribute(widgetElement, "state", null);
        if (stateValue != null) {
            WidgetState state = WidgetState.stateForName(stateValue);
            if (state == null) {
                throw new Exception ("Unknow value '" + stateValue +"' for state attribute at " +
                        DomHelper.getLocation(widgetElement));
            }
            widgetDefinition.setState(state);
        }

        // attributes
        Element attrContainer = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, "attributes", false);
        if (attrContainer != null) {
            // There's a <fd:attributes> element. Get its <fd:attribute> children
            Element[] attrs = DomHelper.getChildElements(attrContainer, Constants.DEFINITION_NS, "attribute");
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

    protected WidgetDefinition buildAnotherWidgetDefinition(Element widgetDefinition) throws Exception {
        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder = null;
        try {
            builder = (WidgetDefinitionBuilder)widgetDefinitionBuilderSelector.select(widgetName);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown kind of widget '" + widgetName + "' at " +
                                         DomHelper.getLocation(widgetDefinition), e);
        }
        return builder.buildWidgetDefinition(widgetDefinition);
    }

    protected List buildEventListeners(Element widgetElement, String elementName, Class listenerClass) throws Exception {
        List result = null;
        Element listenersElement = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, elementName);
        if (listenersElement != null) {
            NodeList list = listenersElement.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element listenerElement = (Element)list.item(i);
                    WidgetListenerBuilder listenerBuilder = null;
                    try {
                        listenerBuilder = (WidgetListenerBuilder)widgetListenerBuilderSelector.select(listenerElement.getLocalName());
                    } catch (ServiceException e) {
                        throw new CascadingException("Unknown kind of eventlistener '" + listenerElement.getLocalName()
                                + "' at " + DomHelper.getLocation(listenerElement), e);
                    }
                    WidgetListener listener = listenerBuilder.buildListener(listenerElement, listenerClass);
                    widgetListenerBuilderSelector.release(listenerBuilder);
                    if (result == null) result = new ArrayList();
                    result.add(listener);
                }
            }
        }

        return result == null ? Collections.EMPTY_LIST : result;
    }

    protected void setDisplayData(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
        final String[] names = {"label", "help", "hint"};
        Map displayData = new HashMap(names.length);
        for (int i = 0; i < names.length; i++) {
            XMLizable data = null;
            Element dataElement = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, names[i]);
            if (dataElement != null) {
                data = DomHelper.compileElementContent(dataElement);
            }

            // NOTE: We put also null values in the may in order to test their existence
            //       (see AbstractWidgetDefinition.generateDisplayData)
            displayData.put(names[i], data);
        }

        widgetDefinition.setDisplayData(displayData);
    }

    private void setValidators(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
        Element validatorElement = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, "validation");
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
                        throw new CascadingException("Unknow kind of validator '" + name + "' at " +
                                                     DomHelper.getLocation(element), e);
                    }

                    widgetDefinition.addValidator(builder.build(element, widgetDefinition));
                    widgetValidatorBuilderSelector.release(builder);
                }
            }
        }
    }

    private void setCreateListeners(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
        Iterator iter = buildEventListeners(widgetElement, "on-create", CreateListener.class).iterator();
        while (iter.hasNext()) {
            widgetDefinition.addCreateListener((CreateListener)iter.next());
        }
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
}
