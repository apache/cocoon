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

import org.apache.cocoon.woody.formmodel.*;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;
import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;

/**
 * Component implementing the {@link FormManager} role.
 */
public class DefaultFormManager extends AbstractLogEnabled implements FormManager, ThreadSafe, Composable, Disposable, Configurable {
    private ComponentManager componentManager;
    private Map widgetDefinitionBuilders = new HashMap();
    private FormDefinitionBuilder formDefinitionBuilder;
    private boolean initialized = false;
    private Store store;

    public void compose(ComponentManager componentManager) throws ComponentException {
        this.componentManager = componentManager;
        this.store = (Store)componentManager.lookup(Store.TRANSIENT_STORE);
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        // get available widgets from configuration
        Configuration[] widgetConfs = configuration.getChild("widgets").getChildren("widget");
        if (widgetConfs.length == 0)
            getLogger().warn("No Woody widgets found in FormManager configuration.");

        for (int i = 0; i < widgetConfs.length; i++) {
            String name = widgetConfs[i].getAttribute("name");
            String factoryClassName = widgetConfs[i].getAttribute("factory");
            Class clazz;
            try {
                clazz = Class.forName(factoryClassName);
            } catch (Exception e) {
                throw new ConfigurationException("Could not load class \"" + factoryClassName + "\" specified at " + widgetConfs[i].getLocation(), e);
            }
            WidgetDefinitionBuilder widgetDefinitionBuilder;
            try {
                widgetDefinitionBuilder = (WidgetDefinitionBuilder)clazz.newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Could not create WidgetDefinitionBuilder \"" + factoryClassName + "\"", e);
            }
            widgetDefinitionBuilders.put(name, widgetDefinitionBuilder);
        }
    }

    public void lazyInitialize() throws Exception {
        // Initialisation is only done after the FormManager has been fully created, because
        // the WidgetDefinitionBuilders that we create here need themselves access to
        // the FormManager (which they can only lookup after the FormManager itself has
        // passed all lifecycle stages).

        if (initialized)
            return;

        LifecycleHelper lifecycleHelper = new LifecycleHelper(null, null, componentManager, null, null, null);

        Iterator widgetDefinitionBuilderIt = widgetDefinitionBuilders.values().iterator();
        while (widgetDefinitionBuilderIt.hasNext()) {
            WidgetDefinitionBuilder widgetDefinitionBuilder = (WidgetDefinitionBuilder)widgetDefinitionBuilderIt.next();
            lifecycleHelper.setupComponent(widgetDefinitionBuilder);
        }

        // special case
        formDefinitionBuilder = new FormDefinitionBuilder();
        lifecycleHelper.setupComponent(formDefinitionBuilder);

        initialized = true;
    }

    public Form createForm(Source source) throws Exception {
        FormDefinition formDefinition = getFormDefinition(source);
        return (Form)formDefinition.createInstance();
    }

    public FormDefinition getFormDefinition(Source source) throws Exception {
        lazyInitialize();

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

            formDefinition = (FormDefinition)formDefinitionBuilder.buildWidgetDefinition(formElement);
            storeFormDefinition(formDefinition, source);
        }
        return formDefinition;
    }

    private FormDefinition getStoredFormDefinition(Source source) {
        String key = "WoodyForm:" + source.getURI();
        SourceValidity newValidity = source.getValidity();

        if (newValidity == null) {
            store.remove(key);
            return null;
        }

        Object[] formDefinitionAndValidity = (Object[])store.get(key);
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
            store.remove(key);
            return null;
        }

        return (FormDefinition)formDefinitionAndValidity[0];
    }

    private void storeFormDefinition(FormDefinition formDefinition, Source source) throws IOException {
        String key = "WoodyForm:" + source.getURI();
        SourceValidity validity = source.getValidity();
        if (validity != null) {
            Object[] formDefinitionAndValidity = {formDefinition,  validity};
            store.store(key, formDefinitionAndValidity);
        }
    }

    public WidgetDefinition buildWidgetDefinition(Element widgetDefinition) throws Exception {
        lazyInitialize();

        String widgetName = widgetDefinition.getLocalName();
        WidgetDefinitionBuilder builder = (WidgetDefinitionBuilder)widgetDefinitionBuilders.get(widgetName);
        if (builder == null)
            throw new Exception("Unkown kind of widget \"" + widgetName + "\" specified at " + DomHelper.getLocation(widgetDefinition));
        return builder.buildWidgetDefinition(widgetDefinition);
    }

    public void dispose() {
        componentManager.release(store);
    }
}
