/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.formmodel;

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
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.apache.cocoon.woody.event.WidgetListener;
import org.apache.cocoon.woody.event.WidgetListenerBuilderUtil;
import org.apache.cocoon.woody.expression.ExpressionManager;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.validation.WidgetValidatorBuilder;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract base class for WidgetDefinitionBuilders. Provides functionality
 * common to many implementations.
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
            throw new Exception("Missing id attribute on element \"" + widgetElement.getTagName() + "\" at " + DomHelper.getLocation(widgetElement));
        }
        widgetDefinition.setId(id);
    }

    protected WidgetDefinition buildAnotherWidgetDefinition(Element widgetDefinition) throws Exception {
        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder = null;
        try {
            builder = (WidgetDefinitionBuilder)widgetDefinitionBuilderSelector.select(widgetName);
        } catch (ServiceException e) {
            throw new CascadingException("Unknown kind of widget \"" + widgetName + "\" specified at " + DomHelper.getLocation(widgetDefinition), e);
        }
        return builder.buildWidgetDefinition(widgetDefinition);
    }

    protected List buildEventListeners(Element widgetElement, String elementName, Class listenerClass) throws Exception {
        List result = null;
        Element listenerElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, elementName);
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
            Element dataElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, names[i]);
            if (dataElement != null) {
                data = DomHelper.compileElementContent(dataElement);
            }
            
            // Note: we put also null values in the may in order to test their existence
            // (see AbstractWidgetDefinition.generateDisplayData)
            displayData.put(names[i], data);
        }
        
        widgetDefinition.setDisplayData(displayData);
    }
    
    protected void setValidators(Element widgetElement, AbstractWidgetDefinition widgetDefinition) throws Exception {
        List result = null;
        Element validatorElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "validation");
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
                        throw new CascadingException("Unknow kind of validator '" + name + "' at " + DomHelper.getLocation(element), e);
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
