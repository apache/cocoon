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
package org.apache.cocoon.forms.binding;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
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

import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.CacheManager;
import org.apache.cocoon.forms.binding.library.Library;
import org.apache.cocoon.forms.binding.library.LibraryException;
import org.apache.cocoon.forms.binding.library.LibraryManager;
import org.apache.cocoon.forms.binding.library.LibraryManagerImpl;
import org.apache.cocoon.forms.datatype.DatatypeManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.SimpleServiceSelector;
import org.apache.cocoon.util.location.LocationAttributes;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * JXPathBindingManager provides an implementation of {@link BindingManager}by
 * usage of the <a href="http://jakarta.apache.org/commons/jxpath/index.html">
 * JXPath package </a>.
 *
 * @version $Id$
 */
public class JXPathBindingManager extends AbstractLogEnabled
                                  implements BindingManager, Contextualizable, Serviceable,
                                             Configurable, Initializable, Disposable, ThreadSafe {

    private static final String PREFIX = "CocoonFormBinding:";

    protected ServiceManager manager;

    protected DatatypeManager datatypeManager;

    private Configuration configuration;

    protected SimpleServiceSelector bindingBuilderSelector;

    private CacheManager cacheManager;

    private Context avalonContext;

    protected LibraryManagerImpl libraryManager;


    /**
     * Java 1.3 logger access method.
     * <br>
     * Access to {#getLogger} from inner class on Java 1.3 causes NoSuchMethod error.  
     */
    protected Logger getMyLogger() {
        return getLogger();
    }

    public void contextualize(Context context) throws ContextException {
        this.avalonContext = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.datatypeManager = (DatatypeManager) manager.lookup(DatatypeManager.ROLE);
        this.cacheManager = (CacheManager) manager.lookup(CacheManager.ROLE);
    }

    public void configure(Configuration configuration)
    throws ConfigurationException {
        this.configuration = configuration;
    }

    public void initialize() throws Exception {
        bindingBuilderSelector = new SimpleServiceSelector("binding", JXPathBindingBuilderBase.class);
        LifecycleHelper.setupComponent(bindingBuilderSelector,
                                       getLogger(),
                                       this.avalonContext,
                                       this.manager,
                                       configuration.getChild("bindings"));

        libraryManager = new LibraryManagerImpl();
        libraryManager.setBindingManager(this);
        LifecycleHelper.setupComponent(libraryManager,
                                       getLogger(),
                                       this.avalonContext,
                                       this.manager,
                                       configuration.getChild("library"));
    }

    public Binding createBinding(Source source) throws BindingException {

        Binding binding = (Binding) this.cacheManager.get(source, PREFIX);
        if (binding != null && !binding.isValid()) {
            binding = null; //invalidate
        }

        if (binding == null) {
            try {
                // Retrieve the input source of the binding file
                InputSource is = new InputSource(source.getInputStream());
                is.setSystemId(source.getURI());

                Document doc = DomHelper.parse(is, this.manager);
                binding = createBinding(doc.getDocumentElement());
                this.cacheManager.set(binding, source, PREFIX);
            } catch (BindingException e) {
                throw e;
            } catch (Exception e) {
                throw new BindingException("Error creating binding from " + source.getURI(), e);
            }
        }

        return binding;
    }

    public Binding createBinding(String bindingURI) throws BindingException {
        SourceResolver sourceResolver = null;
        Source source = null;

        try {
            try {
                sourceResolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
                source = sourceResolver.resolveURI(bindingURI);
            } catch (Exception e) {
                throw new BindingException("Error resolving binding source: " + bindingURI);
            }
            return createBinding(source);
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
            if (sourceResolver != null) {
                manager.release(sourceResolver);
            }
        }
    }

    public Binding createBinding(Element bindingElement) throws BindingException {
        Binding binding = null;
        if (BindingManager.NAMESPACE.equals(bindingElement.getNamespaceURI())) {
            binding = getBuilderAssistant().getBindingForConfigurationElement(bindingElement);
            ((JXPathBindingBase) binding).enableLogging(getLogger());
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Creation of new binding finished. " + binding);
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Root Element of said binding file is in wrong namespace.");
            }
        }
        return binding;
    }
    
    public Assistant getBuilderAssistant() {
        return new Assistant();
    }

    public void dispose() {
        if (this.bindingBuilderSelector != null) {
            this.bindingBuilderSelector.dispose();
            this.bindingBuilderSelector = null;
        }
        this.manager.release(this.datatypeManager);
        this.datatypeManager = null;
        this.manager.release(this.cacheManager);
        this.cacheManager = null;
        this.manager = null;
    }

    /**
     * Assistant Inner class discloses enough features to the created
     * childBindings to recursively
     *
     * This patterns was chosen to prevent Inversion Of Control between this
     * factory and its builder classes (that could be provided by third
     * parties.)
     */
    public class Assistant {

        private BindingBuilderContext context = new BindingBuilderContext();
        private Stack contextStack = new Stack();

        private JXPathBindingBuilderBase getBindingBuilder(String bindingType)
        throws BindingException {
            try {
                return (JXPathBindingBuilderBase) bindingBuilderSelector.select(bindingType);
            } catch (ServiceException e) {
                throw new BindingException("Cannot handle binding element '" + bindingType + "'.", e);
            }
        }

        /**
         * Creates a {@link Binding} following the specification in the
         * provided config element.
         */
        public JXPathBindingBase getBindingForConfigurationElement(Element configElm)
        throws BindingException {
            String bindingType = configElm.getLocalName();
            JXPathBindingBuilderBase bindingBuilder = getBindingBuilder(bindingType);

            boolean flag = false;
            if (context.getLocalLibrary() == null) {
                // FIXME Use newLibrary()?
                Library lib = new Library(libraryManager, getBuilderAssistant());
                lib.enableLogging(getMyLogger());
                context.setLocalLibrary(lib);
                lib.setSourceURI(LocationAttributes.getURI(configElm));
                flag = true;
            }

            if (context.getLocalLibrary() != null && configElm.hasAttribute("extends")) {
                try {
                    context.setSuperBinding(context.getLocalLibrary().getBinding(configElm.getAttribute("extends")));
                } catch (LibraryException e) {
                    // throw new RuntimeException("Error extending binding! (at "+DomHelper.getLocation(configElm)+")", e);
                    throw new NestableRuntimeException("Error extending binding! (at " + DomHelper.getLocation(configElm) + ")", e);
                }
            } else {
                context.setSuperBinding(null);
            }

            JXPathBindingBase childBinding = bindingBuilder.buildBinding(configElm, this);
            if (flag && childBinding != null) {
                childBinding.setEnclosingLibrary(context.getLocalLibrary());
            }

            // this might get called unnecessarily, but solves issues with the libraries
            if (childBinding != null) {
                childBinding.enableLogging(getMyLogger());
            }
            return childBinding;
        }

        private JXPathBindingBase[] mergeBindings(JXPathBindingBase[] existing, JXPathBindingBase[] extra) {

            if (existing == null || existing.length == 0) {
                return extra;
            }
            if (extra == null || extra.length == 0) {
                return existing;
            }
            // have to do it the stupid painter way..
            ArrayList list = new ArrayList(existing.length);
            for (int i = 0; i < existing.length; i++) {
                list.add(existing[i]);
            }
            for (int i = 0; i < extra.length; i++) {
                if (extra[i].getId() == null) {
                    list.add(extra[i]);
                } else {
                    // try to replace existing one
                    boolean match = false;
                    for(int j=0; j<list.size(); j++) {
                        if(extra[i].getId().equals(((JXPathBindingBase)list.get(j)).getId())) {
                            list.set(j,extra[i]);
                            match = true;
                            break; // stop searching
                        }
                    }
                    // if no match, just add
                    if (!match) {
                        list.add(extra[i]);
                    }
                }
            }
            return (JXPathBindingBase[])list.toArray(new JXPathBindingBase[list.size()]);
        }

        /**
         * proxy for compatibility
         *
         */
        public JXPathBindingBase[] makeChildBindings(Element parentElement) throws BindingException {
            return makeChildBindings(parentElement,new JXPathBindingBase[0]);
        }

        /**
         * Makes an array of childBindings for the child-elements of the
         * provided configuration element.
         */
        public JXPathBindingBase[] makeChildBindings(Element parentElement, JXPathBindingBase[] existingBindings)
        throws BindingException {
            if (existingBindings == null) {
                existingBindings = new JXPathBindingBase[0];
            }

            if (parentElement != null) {
                Element[] childElements = DomHelper.getChildElements(
                        parentElement, BindingManager.NAMESPACE);
                if (childElements.length > 0) {
                    JXPathBindingBase[] childBindings = new JXPathBindingBase[childElements.length];
                    for (int i = 0; i < childElements.length; i++) {
                        pushContext();
                        context.setSuperBinding(null);

                        String id = DomHelper.getAttribute(childElements[i], "id", null);
                        String path = DomHelper.getAttribute(childElements[i], "path", null);
                        if (context.getLocalLibrary() != null && childElements[i].getAttribute("extends") != null) {
                            try {
                                context.setSuperBinding(context.getLocalLibrary().getBinding(childElements[i].getAttribute("extends")));

                                if (context.getSuperBinding() == null) {
                                    // not found in library
                                    context.setSuperBinding(getBindingByIdOrPath(id, path, existingBindings));
                                }
                            } catch (LibraryException e) {
                                throw new BindingException("Error extending binding! (at "+DomHelper.getLocation(childElements[i])+")",e);
                            }
                        }
                        childBindings[i] = getBindingForConfigurationElement(childElements[i]);
                        popContext();
                    }
                    return mergeBindings(existingBindings,childBindings);
                }
            }
            return existingBindings;
        }

        private JXPathBindingBase getBindingByIdOrPath(String id, String path, JXPathBindingBase[] bindings) {
            String name = id;
            if (name == null) {
                name = "Context:"+path;
            }
            for (int i = 0; i < bindings.length; i++) {
                if (name.equals(bindings[i].getId())) {
                    return bindings[i];
                }
            }
            return null;
        }

        public DatatypeManager getDatatypeManager() {
            return datatypeManager;
        }

        public ServiceManager getServiceManager() {
            return manager;
        }

        public LibraryManager getLibraryManager() {
            return libraryManager;
        }

        public BindingBuilderContext getContext() {
            return this.context;
        }
        private void pushContext() {
            BindingBuilderContext c = new BindingBuilderContext(context);
            contextStack.push(context);
            context = c;
        }

        private void popContext() {
            if (!contextStack.empty()) {
                context = (BindingBuilderContext) contextStack.pop();
            } else {
                context = new BindingBuilderContext();
            }
        }
    }
}
