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
package org.apache.cocoon.woody.binding;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.woody.CacheManager;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.util.SimpleServiceSelector;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * JXPathBindingManager provides an implementation of {@link BindingManager}by
 * usage of the <a href="http://jakarta.apache.org/commons/jxpath/index.html">
 * JXPath package </a>.
 * 
 * @version CVS $Id: JXPathBindingManager.java,v 1.22 2004/03/05 13:02:27 bdelacretaz Exp $
 */
public class JXPathBindingManager extends AbstractLogEnabled implements
        BindingManager, Serviceable, Disposable, Initializable, Configurable,
        ThreadSafe {

    private static final String PREFIX = "WoodyBinding:";

    private ServiceManager serviceManager;

    private DatatypeManager datatypeManager;

    private Configuration configuration;

    private SimpleServiceSelector bindingBuilderSelector;

    private CacheManager cacheManager;

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        this.datatypeManager = (DatatypeManager) serviceManager
                .lookup(DatatypeManager.ROLE);
        this.cacheManager = (CacheManager) serviceManager
                .lookup(CacheManager.ROLE);
    }

    public void configure(Configuration configuration)
            throws ConfigurationException {
        this.configuration = configuration;
    }

    public void initialize() throws Exception {
        bindingBuilderSelector = new SimpleServiceSelector("binding",
                JXPathBindingBuilderBase.class);
        bindingBuilderSelector.enableLogging(getLogger());
        bindingBuilderSelector.configure(configuration.getChild("bindings"));
    }

    public Binding createBinding(Source source) throws BindingException {
        Binding binding = (Binding) this.cacheManager.get(source, PREFIX);
        if (binding == null) {
            try {
                InputSource is = new InputSource(source.getInputStream());
                is.setSystemId(source.getURI());

                Document doc = DomHelper.parse(is);
                Element rootElm = doc.getDocumentElement();
                if (BindingManager.NAMESPACE.equals(rootElm.getNamespaceURI())) {
                    binding = getBuilderAssistant()
                            .getBindingForConfigurationElement(rootElm);
                    ((JXPathBindingBase) binding).enableLogging(getLogger());
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(
                                "Creation of new Binding finished. " + binding);
                    }
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger()
                                .debug(
                                        "Root Element of said binding file is in wrong namespace.");
                    }
                }
                this.cacheManager.set(binding, source, PREFIX);
            } catch (BindingException e) {
                throw e;
            } catch (Exception e) {
                throw new BindingException("Error creating binding from "
                        + source.getURI(), e);
            }
        }
        return binding;
    }

    private Assistant getBuilderAssistant() {
        return new Assistant();
    }

    public void dispose() {
        this.bindingBuilderSelector.dispose();
        this.bindingBuilderSelector = null;
        this.serviceManager.release(this.datatypeManager);
        this.datatypeManager = null;
        this.cacheManager = null;
    }

    /**
     * Assistant Inner class discloses enough features to the created
     * childBindings to recursively
     * 
     * This patterns was chosen to prevent Inversion Of Control between this
     * factory and its builder classes (that could be provided by third
     * parties.)
     */
    /*
     * NOTE: To get access to the logger in this inner class you must not call
     * getLogger() as with JDK 1.3 this gives a NoSuchMethod error. You need to
     * implement an explicit access method for the logger in the outer class.
     */
    public class Assistant {

        private JXPathBindingBuilderBase getBindingBuilder(String bindingType)
                throws BindingException {
            try {
                return (JXPathBindingBuilderBase) bindingBuilderSelector
                        .select(bindingType);
            } catch (ServiceException e) {
                throw new BindingException(
                        "Cannot handle binding element with " + "name \""
                                + bindingType + "\".", e);
            }
        }

        /**
         * Creates a {@link Binding}following the specification in the
         * provided config element.
         */
        public JXPathBindingBase getBindingForConfigurationElement(
                Element configElm) throws BindingException {
            String bindingType = configElm.getLocalName();
            JXPathBindingBuilderBase bindingBuilder = getBindingBuilder(bindingType);
            JXPathBindingBase childBinding = bindingBuilder.buildBinding(
                    configElm, this);
            return childBinding;
        }

        /**
         * Makes an array of childBindings for the child-elements of the
         * provided configuration element.
         */
        public JXPathBindingBase[] makeChildBindings(Element parentElement)
                throws BindingException {
            if (parentElement != null) {
                Element[] childElements = DomHelper.getChildElements(
                        parentElement, BindingManager.NAMESPACE);
                if (childElements.length > 0) {
                    JXPathBindingBase[] childBindings = new JXPathBindingBase[childElements.length];
                    for (int i = 0; i < childElements.length; i++) {
                        childBindings[i] = getBindingForConfigurationElement(childElements[i]);
                    }
                    return childBindings;
                }
            }
            return null;
        }

        public DatatypeManager getDatatypeManager() {
            return datatypeManager;
        }

        public ServiceManager getServiceManager() {
            return serviceManager;
        }
    }
}
