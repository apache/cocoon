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
package org.apache.cocoon.woody.binding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.datatype.DatatypeManager;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * JXPathBindingManager provides an implementation of {@link BindingManager}
 * by usage of the <a href="http://jakarta.apache.org/commons/jxpath/index.html">
 * JXPath package</a>. 
 */
public class JXPathBindingManager implements BindingManager, LogEnabled, Serviceable, Disposable {

    //TODO caching of the Bindings. 

    private Logger logger;

    private DatatypeManager datatypeManager;
    private ServiceManager serviceManager;

    /**
     * Map of specific builders for the different elements in the 
     * Binding config.
     */
    private final Map bindingBuilders = new HashMap();
    {
        //TODO make this configurable

        this.bindingBuilders.put("field", new FieldJXPathBindingBuilder());
        this.bindingBuilders.put(
            "context",
            new ContextJXPathBindingBuilder());
        this.bindingBuilders.put(
            "repeater",
            new RepeaterJXPathBindingBuilder());
        this.bindingBuilders.put(
            "aggregate",
            new AggregateJXPathBindingBuilder());
        this.bindingBuilders.put(
            "set-attribute",
            new SetAttributeJXPathBindingBuilder());
        this.bindingBuilders.put(
            "insert-node",
            new InsertNodeJXPathBindingBuilder());
        this.bindingBuilders.put(
            "delete-node",
            new DeleteNodeJXPathBindingBuilder());
        this.bindingBuilders.put(
            "insert-bean",
            new InsertBeanJXPathBindingBuilder());
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
        this.datatypeManager = (DatatypeManager)serviceManager.lookup(DatatypeManager.ROLE);
    }

    public Binding createBinding(Source bindSrc)
        throws ProcessingException {
        try {
            Document doc =
                DomHelper.parse(new InputSource(bindSrc.getURI()));
            Element rootElm = doc.getDocumentElement();
            JXPathBindingBase newBinding = null;
            if (BindingManager.NAMESPACE.equals(rootElm.getNamespaceURI())) {
                newBinding = getBuilderAssistant().getBindingForConfigurationElement(rootElm);
                newBinding.enableLogging(getLogger());
                getLogger().debug("Creation of new Binding finished. " + newBinding);
            } else {
                getLogger().debug("Root Element of said binding file is in wrong namespace.");
            }
            return newBinding;
        } catch (Exception e) {
            throw new ProcessingException("Could not create binding.", e);
        }
    }

    public void enableLogging(Logger l) {
        this.logger = l;
        l.debug("Got logger, passing to the bindingBuilders");
        Iterator iter = this.bindingBuilders.values().iterator();
        while (iter.hasNext()) {
            Object builder = iter.next();
            if (builder instanceof LogEnabled) {
                ((LogEnabled) builder).enableLogging(l);
            }
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    private Assistant getBuilderAssistant() {
        return new Assistant();
    }

    public void dispose() {
        serviceManager.release(datatypeManager);
    }

    /**
     * Assistant Inner class discloses enough features to the created 
     * childBindings to recursively 
     * 
     * This patterns was chosen to prevent Inversion Of Control between
     * this factory and its builder classes (that could be provided by third
     * parties)  
     */
    public class Assistant {

        private JXpathBindingBuilderBase getBindingBuilder(String bindingType) {
            return (JXpathBindingBuilderBase) JXPathBindingManager
                .this
                .bindingBuilders
                .get(bindingType);
        }

        /**
         * Creates a {@link Binding} folowing the specification in the 
         * provided config element.
         */
        public JXPathBindingBase getBindingForConfigurationElement(Element configElm) throws BindingException {
            String bindingType = configElm.getLocalName();
            if (getLogger().isDebugEnabled())
                JXPathBindingManager.this.getLogger().debug("build binding for config elm " + bindingType);
            JXpathBindingBuilderBase bindingBuilder = getBindingBuilder(bindingType);
            JXPathBindingBase childBinding = bindingBuilder.buildBinding(configElm, this);
            return childBinding;
        }

        /**
         * Makes an array of childBindings for the child-elements of the 
         * provided configuration element.
         */
        public JXPathBindingBase[] makeChildBindings(Element parentElement) throws BindingException {
            if (parentElement == null) {
                return null;
            }

            Element[] childElements = DomHelper.getChildElements(parentElement, BindingManager.NAMESPACE);
            if (childElements.length > 0) {
                JXPathBindingBase[] childBindings = new JXPathBindingBase[childElements.length];
                for (int i = 0; i < childElements.length; i++) {
                    childBindings[i] = getBindingForConfigurationElement(childElements[i]);
                }
                return childBindings;
            } else {
                return null;
            }
        }

        public DatatypeManager getDatatypeManager() {
            return datatypeManager;
        }

    }
}
