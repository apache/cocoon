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
package org.apache.cocoon.forms;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.FormDefinition;
import org.apache.cocoon.forms.formmodel.FormDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.library.LibraryManager;
import org.apache.cocoon.forms.formmodel.library.LibraryManagerImpl;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.SimpleServiceSelector;
import org.apache.cocoon.util.location.LocationImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Component implementing the {@link FormManager} role.
 *
 * @version $Id$
 */
public class DefaultFormManager extends AbstractLogEnabled
                                implements FormManager, Contextualizable, Serviceable, Configurable,
                                           Disposable, ThreadSafe, Component {
    // NOTE: Component is there to allow this block to run in the 2.1 branch

    private static final String PREFIX = "CocoonForms:";

    private Context avalonContext;
    protected ServiceManager manager;
    protected SimpleServiceSelector widgetDefinitionBuilderSelector;
    protected CacheManager cacheManager;

    protected LibraryManager libraryManager;

    //
    // Lifecycle
    //

    public DefaultFormManager() {
        widgetDefinitionBuilderSelector = new SimpleServiceSelector("widget", WidgetDefinitionBuilder.class);
        this.libraryManager = new LibraryManagerImpl();
    }

    /**
     * @see org.apache.avalon.framework.logger.AbstractLogEnabled#enableLogging(org.apache.avalon.framework.logger.Logger)
     */
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        widgetDefinitionBuilderSelector.enableLogging(getLogger());
        ContainerUtil.enableLogging(this.libraryManager, getLogger().getChildLogger("library"));
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.avalonContext = context;
        widgetDefinitionBuilderSelector.contextualize(avalonContext);
        ContainerUtil.contextualize(this.libraryManager, avalonContext);
    }

    /** Temporary internal method, don't rely on it's existence! Needed to access the context from flowscript. */
    // FIXME (SW). Extending the FOM is needed.
    public Context getAvalonContext() {
        return this.avalonContext;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.cacheManager = (CacheManager) manager.lookup(CacheManager.ROLE);
        widgetDefinitionBuilderSelector.service(new FormServiceManager());
        ContainerUtil.service(this.libraryManager, new FormServiceManager());
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        ContainerUtil.configure(this.libraryManager, configuration.getChild("libraries"));
        widgetDefinitionBuilderSelector.configure(configuration.getChild("widgets"));
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.widgetDefinitionBuilderSelector != null) {
            this.widgetDefinitionBuilderSelector.dispose();
            this.widgetDefinitionBuilderSelector = null;
        }
        if (this.libraryManager != null) {
            ContainerUtil.dispose(this.libraryManager);
            this.libraryManager = null;
        }
        if (this.cacheManager != null) {
            this.manager.release(this.cacheManager);
            this.cacheManager = null;
        }
        this.manager = null;
    }

    //
    // Business Methods
    //

    public Form createForm(String uri) throws Exception {
        SourceResolver resolver = null;
        Source source = null;
        try {
            try {
                resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                source = resolver.resolveURI(uri);
            } catch (Exception e) {
                throw new FormsException("Could not resolve form definition URI.",
                                         e, new LocationImpl("[FormManager]", uri));
            }
            return createForm(source);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
            if (resolver != null) {
                manager.release(resolver);
            }
        }
    }

    public Form createForm(Source source) throws Exception {
        FormDefinition formDefinition = createFormDefinition(source);
        Form form = (Form) formDefinition.createInstance();
        form.initialize();
        return form;
    }

    public Form createForm(Element formElement) throws Exception {
        Form form = (Form) createFormDefinition(formElement).createInstance();
        form.initialize();
        return form;
    }

    public FormDefinition createFormDefinition(String uri) throws Exception {
        SourceResolver sourceResolver = null;
        Source source = null;
        try {
            try {
                sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                source = sourceResolver.resolveURI(uri);
            } catch (Exception e) {
                throw new FormsException("Could not resolve form definition.",
                                         e, new LocationImpl("[FormManager]", uri));
            }
            return createFormDefinition(source);
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
            if (sourceResolver != null) {
                manager.release(sourceResolver);
            }
        }
    }

    public FormDefinition createFormDefinition(Source source) throws Exception {
        FormDefinition formDefinition = (FormDefinition) this.cacheManager.get(source, PREFIX);
        if (formDefinition != null && formDefinition.getLocalLibrary().dependenciesHaveChanged()) {
            formDefinition = null; // invalidate
        }

        if (formDefinition == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Building Form: " + source.getURI());
            }

            Document formDocument;
            try {
                InputSource inputSource = new InputSource(source.getInputStream());
                inputSource.setSystemId(source.getURI());
                formDocument = DomHelper.parse(inputSource, this.manager);
            } catch (Exception e) {
                throw new FormsException("Could not parse form definition.",
                                         e, new LocationImpl("[FormManager]", source.getURI()));
            }

            Element formElement = formDocument.getDocumentElement();
            formDefinition = createFormDefinition(formElement);
            this.cacheManager.set(formDefinition, source, PREFIX);
        }
        return formDefinition;
    }

    public FormDefinition createFormDefinition(Element formElement) throws Exception {
        // check that the root element is a fd:form element
        if (!FormsConstants.DEFINITION_NS.equals(formElement.getNamespaceURI()) || !formElement.getLocalName().equals("form")) {
            throw new FormsException("Expected forms definition <fd:form> element.",
                                     DomHelper.getLocationObject(formElement));
        }

        FormDefinitionBuilder builder = (FormDefinitionBuilder) widgetDefinitionBuilderSelector.select("form");
        return (FormDefinition) builder.buildWidgetDefinition(formElement);
    }


    private class FormServiceManager implements ServiceManager {
        final String WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE = WidgetDefinitionBuilder.class.getName() + "Selector";
        final String LIBRARY_MANAGER_ROLE = LibraryManager.ROLE;

        public Object lookup(String name) throws ServiceException {
            if (WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE.equals(name)) {
                return widgetDefinitionBuilderSelector;
            } else if(LIBRARY_MANAGER_ROLE.equals(name)) {
                return libraryManager;
            } else {
                return manager.lookup(name);
            }
        }

        public boolean hasService(String name) {
            if (WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE.equals(name)) {
                return true;
            } else if(LIBRARY_MANAGER_ROLE.equals(name)) {
                return true;
            } else {
                return manager.hasService(name);
            }
        }

        public void release(Object service) {
            if (service != widgetDefinitionBuilderSelector && service != libraryManager) {
                manager.release(service);
            }
        }
    }
}
