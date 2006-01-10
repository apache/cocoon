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
package org.apache.cocoon.forms;

import org.apache.avalon.framework.CascadingException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.FormDefinition;
import org.apache.cocoon.forms.formmodel.FormDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.WidgetDefinitionBuilder;
import org.apache.cocoon.forms.formmodel.library.LibraryManager;
import org.apache.cocoon.forms.formmodel.library.LibraryManagerImpl;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.SimpleServiceSelector;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Component implementing the {@link FormManager} role.
 *
 * @version $Id$
 */
public class DefaultFormManager
  extends AbstractLogEnabled
  implements FormManager, Contextualizable, ThreadSafe, Serviceable, Disposable, Configurable, Component, Initializable {
// FIXME: Component is there to allow this block to also run in the 2.1 branch

    protected static final String PREFIX = "CocoonForm:";
    protected ServiceManager manager;
    protected Configuration configuration;
    protected SimpleServiceSelector widgetDefinitionBuilderSelector;
    protected CacheManager cacheManager;
    
    protected LibraryManagerImpl libraryManager;

    private Context avalonContext;
    public void contextualize(Context context) throws ContextException {
		this.avalonContext = context;
	}

    /** Temporary internal method, don't rely on it's existence! Needed to access the context from flowscript. */
    // FIXME (SW). Extending the FOM is needed.
    public Context getAvalonContext() {
        return this.avalonContext;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.cacheManager = (CacheManager)manager.lookup(CacheManager.ROLE);
    }

    /**
     * Configurable
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    public void initialize() throws Exception {

        libraryManager = new LibraryManagerImpl();
        libraryManager.enableLogging(getLogger().getChildLogger("library"));
        libraryManager.service(new FormServiceManager());
        libraryManager.configure(configuration.getChild("libraries"));
        
        widgetDefinitionBuilderSelector = new SimpleServiceSelector("widget", WidgetDefinitionBuilder.class);
        widgetDefinitionBuilderSelector.contextualize(avalonContext);
        widgetDefinitionBuilderSelector.service(new FormServiceManager());
        widgetDefinitionBuilderSelector.configure(configuration.getChild("widgets"));
        
        libraryManager.initialize();
    }
    
    public ServiceSelector getWidgetDefinitionBuilderSelector() {
    	return this.widgetDefinitionBuilderSelector;
    }

    public Form createForm(Source source) throws Exception {
        FormDefinition formDefinition = getFormDefinition(source);
        Form form = (Form)formDefinition.createInstance();
        form.initialize();
        return form;
    }

    public Form createForm(String uri) throws Exception {
        SourceResolver sourceResolver = null;
        Source source = null;

        try {
            sourceResolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);

            source = sourceResolver.resolveURI(uri);
            Form form = createForm(source);
            return form;
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
            if (sourceResolver != null) {
                manager.release(sourceResolver);
            }
        }
    }

    public Form createForm(Element formElement) throws Exception {
        Form form = (Form)getFormDefinition(formElement).createInstance();
        form.initialize();
        return form;
    }

    public FormDefinition createFormDefinition(Element formElement) throws Exception {
        return getFormDefinition(formElement);
    }

    public FormDefinition getFormDefinition(Source source) throws Exception {
        FormDefinition formDefinition = (FormDefinition)this.cacheManager.get(source, PREFIX);
        
        if(formDefinition != null && formDefinition.getLocalLibrary().dependenciesHaveChanged())
        	formDefinition = null; // invalidate
        
        if (formDefinition == null) {
        	
        	if(getLogger().isDebugEnabled())
        		getLogger().debug("Building Form: "+source.getURI());
        	
            Document formDocument;
            try {
                InputSource inputSource = new InputSource(source.getInputStream());
                inputSource.setSystemId(source.getURI());
                formDocument = DomHelper.parse(inputSource, this.manager);
            } catch (Exception e) {
                throw new CascadingException("Could not parse form definition from " +
                                             source.getURI(), e);
            }

            Element formElement = formDocument.getDocumentElement();
            formDefinition = getFormDefinition(formElement);
            this.cacheManager.set(formDefinition, source, PREFIX);
        }
        return formDefinition;
    }

    public FormDefinition getFormDefinition(Element formElement) throws Exception {
        // check that the root element is a fd:form element
        if (!(formElement.getLocalName().equals("form") || FormsConstants.DEFINITION_NS.equals(formElement.getNamespaceURI())))
            throw new Exception("Expected a Cocoon Forms form element at " + DomHelper.getLocation(formElement));

        FormDefinitionBuilder formDefinitionBuilder = (FormDefinitionBuilder)widgetDefinitionBuilderSelector.select("form");
        return (FormDefinition)formDefinitionBuilder.buildWidgetDefinition(formElement);
    }

    public FormDefinition createFormDefinition(String uri) throws Exception {
        SourceResolver sourceResolver = null;
        Source source = null;
        Document formDocument = null;

        try {
            sourceResolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
            source = sourceResolver.resolveURI(uri);

            try {
                InputSource inputSource = new InputSource(source.getInputStream());
                inputSource.setSystemId(source.getURI());
                formDocument = DomHelper.parse(inputSource, this.manager);
            } catch (Exception e) {
                throw new CascadingException("Could not parse form definition from " +
                                             source.getURI(), e);
            }

        } finally {
            if (source != null)
                sourceResolver.release(source);
            if (sourceResolver != null)
                manager.release(sourceResolver);
        }

        Element formElement = formDocument.getDocumentElement();
        return getFormDefinition(formElement);
    }

    /**
     * Disposable
     */
    public void dispose() {
        if (this.widgetDefinitionBuilderSelector != null) {
            this.widgetDefinitionBuilderSelector.dispose();
            this.widgetDefinitionBuilderSelector = null;
        }
        if(this.libraryManager != null) {
        	this.libraryManager.dispose();
        	this.libraryManager = null;
        }
        this.manager.release(this.cacheManager);
        this.cacheManager = null;
        this.manager = null;
    }
    
    
    public class FormServiceManager implements ServiceManager {
        final String WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE = WidgetDefinitionBuilder.class.getName() + "Selector";
        final String LIBRARY_MANAGER_ROLE = LibraryManager.ROLE;

        public Object lookup(String name) throws ServiceException {
            if (WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE.equals(name))
                return widgetDefinitionBuilderSelector;
            else if(LIBRARY_MANAGER_ROLE.equals(name))
            	return libraryManager;
            else
                return manager.lookup(name);
        }

        public boolean hasService(String name) {
            if (WIDGET_DEFINITION_BUILDER_SELECTOR_ROLE.equals(name))
                return true;
            else if(LIBRARY_MANAGER_ROLE.equals(name))
            	return true;
            else
                return manager.hasService(name);
        }

        public void release(Object service) {
            if (service != widgetDefinitionBuilderSelector
            		&& service != libraryManager)
                manager.release(service);
        }
    }
}
