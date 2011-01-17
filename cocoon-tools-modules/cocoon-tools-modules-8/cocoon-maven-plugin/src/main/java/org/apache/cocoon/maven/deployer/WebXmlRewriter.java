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
package org.apache.cocoon.maven.deployer;

import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.maven.deployer.servlet.ShieldedClassLoaderManager;
import org.apache.cocoon.maven.deployer.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @version $Id$
 */
public class WebXmlRewriter {

    private String servletClass;
    private String listenerClass;
    private String filterClass;
    private boolean useShieldingRepositoryParameter;

    public WebXmlRewriter(String servletClass, String listenerClass, String filterClass, boolean useShieldingRepository) {
        this.servletClass = servletClass;
        this.listenerClass = listenerClass;
        this.filterClass = filterClass;
        this.useShieldingRepositoryParameter = useShieldingRepository;
    }

    public boolean rewrite(Document webAppDoc) {
        boolean rewritten = false;
        final Element rootElement = webAppDoc.getDocumentElement();
        
        // first rewrite servlets
        final List servlets = XMLUtils.getChildNodes(rootElement, "servlet");
        Iterator i = servlets.iterator();
        
        while ( i.hasNext() ) {
            final Element servletElement = (Element)i.next();
            final Element servletClassElement = XMLUtils.getChildNode(servletElement, "servlet-class");
            if ( servletClassElement != null ) {
                final String className = XMLUtils.getValue(servletClassElement);
                XMLUtils.setValue(servletClassElement, this.servletClass);
                // create init-param with real servlet class
                final Element initParamElem = webAppDoc.createElementNS(null, "init-param");
                final Element initParamNameElem = webAppDoc.createElementNS(null, "param-name");
                final Element initParamValueElem = webAppDoc.createElementNS(null, "param-value");
                
                initParamElem.appendChild(initParamNameElem);
                initParamElem.appendChild(initParamValueElem);
                
                XMLUtils.setValue(initParamNameElem, "servlet-class");
                XMLUtils.setValue(initParamValueElem, className);
                Element beforeElement = XMLUtils.getChildNode(servletElement, "load-on-startup");
                if ( beforeElement == null ) {
                    beforeElement = XMLUtils.getChildNode(servletElement, "run-as");
                    if ( beforeElement == null ) {
                        beforeElement = XMLUtils.getChildNode(servletElement, "security-role-ref");
                    }
                }
                if ( beforeElement == null ) {
                    servletElement.appendChild(initParamElem);
                } else {
                    servletElement.insertBefore(initParamElem, beforeElement);
                }
                
                rewritten = true;
            }
        }

        // now rewrite listeners
        final List listeners = XMLUtils.getChildNodes(rootElement, "listener");
        i = listeners.iterator();
        boolean hasListener = false;
        final StringBuffer rewrittenListeners = new StringBuffer();
        while ( i.hasNext() ) {
            final Element listenerElement = (Element)i.next();
            final Element listenerClassElement = XMLUtils.getChildNode(listenerElement, "listener-class");
            if ( listenerClassElement != null ) {
                final String className = XMLUtils.getValue(listenerClassElement);
                if ( rewrittenListeners.length() > 0 ) {
                    rewrittenListeners.append(',');
                }
                rewrittenListeners.append(className);
                if ( hasListener ) {
                    rootElement.removeChild(listenerElement);
                } else {
                    XMLUtils.setValue(listenerClassElement, this.listenerClass);
                    hasListener = true;
                }
                rewritten = true;
            }
        }
        // remove old parameter
        i = XMLUtils.getChildNodes(rootElement, "context-param").iterator();
        while ( i.hasNext() ) {
            final Element child = (Element)i.next();
            if ( this.listenerClass.equals(XMLUtils.getValue(XMLUtils.getChildNode(child, "param-name")))) {
                rootElement.removeChild(child);
            }
        }
        if ( hasListener ) {
            addContextParameter(rootElement, this.listenerClass, rewrittenListeners.toString());
        }

        // and now filters
        i = XMLUtils.getChildNodes(rootElement, "filter").iterator();
        while ( i.hasNext() ) {
            final Element filterElement = (Element)i.next();
            final Element filterClassElement = XMLUtils.getChildNode(filterElement, "filter-class");
            if ( filterClassElement != null ) {
                final String className = XMLUtils.getValue(filterClassElement);
                XMLUtils.setValue(filterClassElement, this.filterClass);
                // create init-param with real servlet class
                final Element initParamElem = webAppDoc.createElementNS(null, "init-param");
                final Element initParamNameElem = webAppDoc.createElementNS(null, "param-name");
                final Element initParamValueElem = webAppDoc.createElementNS(null, "param-value");
                initParamElem.appendChild(initParamNameElem);
                initParamElem.appendChild(initParamValueElem);
                XMLUtils.setValue(initParamNameElem, "filter-class");
                XMLUtils.setValue(initParamValueElem, className);
                filterElement.appendChild(initParamElem);
                rewritten = true;
            }
        }

        if ( !this.useShieldingRepositoryParameter ) {
            addContextParameter(rootElement,
                                ShieldedClassLoaderManager.SHIELDED_CLASSLOADER_USE_REPOSITORY,
                                "false");
            rewritten = true;
        } else {
            if ( removeContextParameter(rootElement, ShieldedClassLoaderManager.SHIELDED_CLASSLOADER_USE_REPOSITORY) ) {
                rewritten = true;
            }
        }

        return rewritten;
    }

    protected static boolean removeContextParameter(Element root, String name) {
        boolean removed = false;
        final Iterator i = XMLUtils.getChildNodes(root, "context-param").iterator();
        while ( !removed && i.hasNext() ) {
            final Element parameterElement = (Element)i.next();
            final String paramName = XMLUtils.getValue(XMLUtils.getChildNode(parameterElement, "param-name"));
            if ( name.equals(paramName) ) {
                parameterElement.getParentNode().removeChild(parameterElement);
                removed = true;
            }
        }
        return removed;
    }

    protected static void addContextParameter(Element root, String name, String value) {
        removeContextParameter(root, name);
        // search the element where we have to put the new context parameter before!
        // we know that we have listeners so this is the last element to search for
        Element searchElement = XMLUtils.getChildNode(root, "context-param");
        if ( searchElement == null ) {
            searchElement = XMLUtils.getChildNode(root, "filter");
            if ( searchElement == null ) {
                searchElement = XMLUtils.getChildNode(root, "filter-mapping");
                if ( searchElement == null ) {
                    searchElement = XMLUtils.getChildNode(root, "listener");
                }
            }
        }
        final Element contextParamElement = root.getOwnerDocument().createElementNS(null, "context-param");
        final Element contextParamNameElement = root.getOwnerDocument().createElementNS(null, "param-name");
        final Element contextParamValueElement = root.getOwnerDocument().createElementNS(null, "param-value");
        
        contextParamElement.appendChild(contextParamNameElement);
        contextParamElement.appendChild(contextParamValueElement);
        
        XMLUtils.setValue(contextParamNameElement, name);
        XMLUtils.setValue(contextParamValueElement, value);
        
        root.insertBefore(contextParamElement, searchElement);
    }
}