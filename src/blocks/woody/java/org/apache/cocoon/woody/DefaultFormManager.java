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
package org.apache.cocoon.woody;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.woody.formmodel.FormDefinition;
import org.apache.cocoon.woody.formmodel.FormDefinitionBuilder;
import org.apache.cocoon.woody.formmodel.WidgetDefinitionBuilder;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.util.SimpleServiceSelector;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Component implementing the {@link FormManager} role.
 * 
 * @version $Id: DefaultFormManager.java,v 1.16 2004/03/05 13:02:26 bdelacretaz Exp $
 */
public class DefaultFormManager 
  extends AbstractLogEnabled 
  implements FormManager, ThreadSafe, Serviceable, Disposable, Configurable, Component, Initializable {
      
    protected static final String PREFIX = "WoodyForm:";
    protected ServiceManager manager;
    protected Configuration configuration;
    protected SimpleServiceSelector widgetDefinitionBuilderSelector;
    protected CacheManager cacheManager;

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.cacheManager = (CacheManager)serviceManager.lookup(CacheManager.ROLE);
    }

    /**
     * Configurable
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public void initialize() throws Exception {
        widgetDefinitionBuilderSelector = new SimpleServiceSelector("widget", WidgetDefinitionBuilder.class);
        widgetDefinitionBuilderSelector.service(new ServiceManager() {
            final String WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE = WidgetDefinitionBuilder.class.getName() + "Selector";

            public Object lookup(String name) throws ServiceException {
                if (WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE.equals(name))
                    return widgetDefinitionBuilderSelector;
                else
                    return manager.lookup(name);
            }

            public boolean hasService(String name) {
                if (WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE.equals(name))
                    return true;
                else
                    return manager.hasService(name);
            }

            public void release(Object service) {
                if (service != widgetDefinitionBuilderSelector)
                    manager.release(service);
            }
        });
        widgetDefinitionBuilderSelector.configure(configuration.getChild("widgets"));
    }

    public Form createForm(Source source) throws Exception {
        FormDefinition formDefinition = getFormDefinition(source);
        return (Form)formDefinition.createInstance();
    }

    public FormDefinition getFormDefinition(Source source) throws Exception {
        FormDefinition formDefinition = (FormDefinition)this.cacheManager.get(source, PREFIX);
        if (formDefinition == null) {
            Document formDocument;
            try {
                InputSource inputSource = new InputSource(source.getInputStream());
                inputSource.setSystemId(source.getURI());
                formDocument = DomHelper.parse(inputSource);
            }
            catch (Exception exc) {
                throw new CascadingException("Could not parse form definition from " + source.getURI(), exc);
            }

            Element formElement = formDocument.getDocumentElement();

            // check that the root element is a wd:form element
            if (!(formElement.getLocalName().equals("form") || Constants.WD_NS.equals(formElement.getNamespaceURI())))
                throw new Exception("Expected a Woody form element at " + DomHelper.getLocation(formElement));

            FormDefinitionBuilder formDefinitionBuilder = (FormDefinitionBuilder)widgetDefinitionBuilderSelector.select("form");
            formDefinition = (FormDefinition)formDefinitionBuilder.buildWidgetDefinition(formElement);
            this.cacheManager.set(formDefinition, source, PREFIX);
        }
        return formDefinition;
    }

    /**
     * Disposable
     */
    public void dispose() {
        widgetDefinitionBuilderSelector.dispose();
        this.manager = null;
        this.cacheManager = null;
    }
}
