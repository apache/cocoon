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
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilderUtil;
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
 * @version $Id: AbstractWidgetDefinitionBuilder.java,v 1.2 2004/03/09 13:08:45 cziegeler Exp $
 */
public abstract class AbstractWidgetDefinitionBuilder implements WidgetDefinitionBuilder, Serviceable, Disposable {
    protected ServiceSelector widgetDefinitionBuilderSelector;
    protected ServiceSelector widgetValidatorBuilderSelector;
    protected DatatypeManager datatypeManager;
    protected ExpressionManager expressionManager;
    protected ServiceManager serviceManager;

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        this.widgetDefinitionBuilderSelector = (ServiceSelector)serviceManager.lookup( WidgetDefinitionBuilder.class.getName() + "Selector");
        this.datatypeManager = (DatatypeManager)serviceManager.lookup(DatatypeManager.ROLE);
        this.expressionManager = (ExpressionManager)serviceManager.lookup(ExpressionManager.ROLE);
        this.widgetValidatorBuilderSelector = (ServiceSelector)serviceManager.lookup(WidgetValidatorBuilder.ROLE + "Selector");
    }

    protected void setLocation(Element widgetElement, AbstractWidgetDefinition widgetDefinition) {
        widgetDefinition.setLocation(DomHelper.getLocation(widgetElement));
    }

    protected void setId(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
        String id = DomHelper.getAttribute(widgetElement, "id");
        if (id.length() < 1) {
            throw new Exception("Missing id attribute on element '" + widgetElement.getTagName() + "' at " +
                                DomHelper.getLocation(widgetElement));
        }
        widgetDefinition.setId(id);
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
        Element listenerElement = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, elementName);
        if (listenerElement != null) {
            NodeList list = listenerElement.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    WidgetListener listener = WidgetListenerBuilderUtil.getWidgetListener((Element)list.item(i), listenerClass);
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

    protected void setValidators(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
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
                }
            }
        }
    }

    public void dispose() {
        serviceManager.release(widgetDefinitionBuilderSelector);
        serviceManager.release(datatypeManager);
        serviceManager.release(expressionManager);
        serviceManager.release(widgetValidatorBuilderSelector);
    }
}
