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
package org.apache.cocoon.woody;

import java.io.IOException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.woody.formmodel.*;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.util.SimpleServiceSelector;
import org.apache.commons.collections.FastHashMap;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Component implementing the {@link FormManager} role.
 */
public class DefaultFormManager 
  extends AbstractLogEnabled 
  implements FormManager, ThreadSafe, Serviceable, Disposable, Configurable, Component, Initializable {
      
    protected ServiceManager manager;
    protected Configuration configuration;
    protected SimpleServiceSelector widgetDefinitionBuilderSelector;
    protected FastHashMap formDefinitionCache = new FastHashMap();

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
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
        FormDefinition formDefinition = getStoredFormDefinition(source);
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
            storeFormDefinition(formDefinition, source);
        }
        return formDefinition;
    }

    protected FormDefinition getStoredFormDefinition(Source source) {
        String key = "WoodyForm:" + source.getURI();
        SourceValidity newValidity = source.getValidity();

        if (newValidity == null) {
            formDefinitionCache.remove(key);
            return null;
        }

        Object[] formDefinitionAndValidity = (Object[])formDefinitionCache.get(key);
        if (formDefinitionAndValidity == null)
            return null;

        SourceValidity storedValidity = (SourceValidity)formDefinitionAndValidity[1];
        int valid = storedValidity.isValid();
        boolean isValid;
        if (valid == 0) {
            valid = storedValidity.isValid(newValidity);
            isValid = (valid == 1);
        } else {
            isValid = (valid == 1);
        }

        if (!isValid) {
            formDefinitionCache.remove(key);
            return null;
        }

        return (FormDefinition)formDefinitionAndValidity[0];
    }

    protected void storeFormDefinition(FormDefinition formDefinition, Source source) throws IOException {
        String key = "WoodyForm:" + source.getURI();
        SourceValidity validity = source.getValidity();
        if (validity != null) {
            Object[] formDefinitionAndValidity = {formDefinition,  validity};
            formDefinitionCache.put(key, formDefinitionAndValidity);
        }
    }

    /**
     * Disposable
     */
    public void dispose() {
        widgetDefinitionBuilderSelector.dispose();
        this.manager = null;
        this.formDefinitionCache = null;
    }
}
