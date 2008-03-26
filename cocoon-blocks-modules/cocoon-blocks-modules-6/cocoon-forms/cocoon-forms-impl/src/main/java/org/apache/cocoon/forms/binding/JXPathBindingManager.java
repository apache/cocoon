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
import java.util.Map;
import java.util.Stack;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

import org.apache.cocoon.core.xml.SAXParser;
import org.apache.cocoon.forms.CacheManager;
import org.apache.cocoon.forms.binding.library.Library;
import org.apache.cocoon.forms.binding.library.LibraryException;
import org.apache.cocoon.forms.binding.library.LibraryManager;
import org.apache.cocoon.forms.binding.library.LibraryManagerImpl;
import org.apache.cocoon.forms.datatype.DatatypeManager;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.location.LocationAttributes;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class JXPathBindingManager implements BindingManager {

    private static Log LOG = LogFactory.getLog( JXPathBindingManager.class );
    
    private static final String PREFIX = "CocoonFormBinding:";

    protected DatatypeManager datatypeManager;

    protected Map bindingBuilders;

    private CacheManager cacheManager;

    protected LibraryManagerImpl libraryManager;

    private SourceResolver sourceResolver;
    
    private SAXParser parser;

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

                Document doc = DomHelper.parse(is, parser);
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
        Source source = null;

        try {
            try {
                source = sourceResolver.resolveURI(bindingURI);
            } catch (Exception e) {
                throw new BindingException("Error resolving binding source: " + bindingURI);
            }
            return createBinding(source);
        } finally {
            if (source != null) {
                sourceResolver.release(source);
            }
        }
    }

    public Binding createBinding(Element bindingElement) throws BindingException {
        Binding binding = null;
        if (BindingManager.NAMESPACE.equals(bindingElement.getNamespaceURI())) {
            binding = getBuilderAssistant().getBindingForConfigurationElement(bindingElement);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creation of new binding finished. " + binding);
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Root Element of said binding file is in wrong namespace.");
            }
        }
        return binding;
    }
    
    public Assistant getBuilderAssistant() {
        return new Assistant();
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

        private JXPathBindingBuilder getBindingBuilder(String bindingType)
        throws BindingException {
            JXPathBindingBuilder builder = (JXPathBindingBuilder) bindingBuilders.get(bindingType);
            if (builder == null) {
                throw new BindingException("Cannot handle binding element '" + bindingType + "'.");
            }
            return builder;
        }

        /**
         * Creates a {@link Binding} following the specification in the
         * provided config element.
         */
        public JXPathBindingBase getBindingForConfigurationElement(Element configElm)
        throws BindingException {
            String bindingType = configElm.getLocalName();
            JXPathBindingBuilder bindingBuilder = getBindingBuilder(bindingType);

            boolean flag = false;
            if (context.getLocalLibrary() == null) {
                // FIXME Use newLibrary()?
                Library lib = new Library(libraryManager, getBuilderAssistant());
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

    public void setDatatypeManager( DatatypeManager datatypeManager )
    {
        this.datatypeManager = datatypeManager;
    }

    public void setCacheManager( CacheManager cacheManager )
    {
        this.cacheManager = cacheManager;
    }

    public void setBindingBuilders( Map bindingBuilders )
    {
        this.bindingBuilders = bindingBuilders;
    }

    public void setParser( SAXParser parser )
    {
        this.parser = parser;
    }

    public void setLibraryManager( LibraryManagerImpl libraryManager )
    {
        this.libraryManager = libraryManager;
    }

    public void setSourceResolver( SourceResolver sourceResolver )
    {
        this.sourceResolver = sourceResolver;
    }
}
