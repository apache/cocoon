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
package org.apache.cocoon.forms.datatype;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.Deprecation;
import org.apache.cocoon.util.location.LocationAttributes;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * Builds {@link SelectionList}s from an XML description or an URL.
 *
 * <p>Note: the class {@link DynamicSelectionList} also interprets the same
 * <code>fd:selection-list</code> XML, so if anything changes here to how
 * that XML is interpreted, it also needs to change over there and vice
 * versa.</p>
 *
 * @version $Id$
 */
public class DefaultSelectionListBuilder implements SelectionListBuilder, Serviceable, Contextualizable {

    private ServiceManager serviceManager;
    private Context context;

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }

    public SelectionList build(Element selectionListElement, Datatype datatype) throws Exception {
        SelectionList selectionList;
        String src = selectionListElement.getAttribute("src");
        if (src.length() > 0) {
            // Principle of least surprise, use dynamic lists by default
            boolean dynamic = true;
            boolean usePerRequestCache = false;
            String cacheType = DomHelper.getAttribute(selectionListElement, "cache", null);

            // Read @cache 
            if ("request".equals(cacheType)) { // Dynamic SelectionList cached per request
                dynamic = true;
                usePerRequestCache = true;
            } else if ("none".equals(cacheType)){ // Dynamic SelectionList non cached
                dynamic = true;
            } else if ("static".equals(cacheType)) {
                // Static SelectionList
                dynamic = false;
            } else { // Checking for deprecated @dynamic
                if (DomHelper.getAttribute(selectionListElement, "dynamic", null) != null) {
                    Deprecation.logger.warn("'@dynamic' is deprecated in <fd:selection-list> and replaced by '@cache' at " + DomHelper.getLocation(selectionListElement));                    
                    dynamic = DomHelper.getAttributeAsBoolean(selectionListElement, "dynamic", false);
                }
            }
            // Create SelectionList
            if (dynamic) {
                selectionList = new DynamicSelectionList(datatype, src, usePerRequestCache, serviceManager, context);
            } else {
                selectionListElement = readSelectionList(src);
                selectionList = buildStaticList(selectionListElement, datatype);                
            }
        } else {
            // selection list is defined inline
            selectionList = buildStaticList(selectionListElement, datatype);
        }
        return selectionList;
    }

    private  SelectionList buildStaticList(Element selectionListElement, Datatype datatype) throws Exception {
        StaticSelectionList selectionList = new StaticSelectionList(datatype);
        Convertor convertor = null;
        Convertor.FormatCache formatCache = new DefaultFormatCache();

        // Remove location attributes from the selection list
        LocationAttributes.remove(selectionListElement, true);
        
        NodeList children = selectionListElement.getChildNodes();
        for (int i = 0; children.item(i) != null; i++) {
            Node node = children.item(i);
            if (convertor == null && node instanceof Element && FormsConstants.DEFINITION_NS.equals(node.getNamespaceURI()) && "convertor".equals(node.getLocalName())) {
                Element convertorConfigElement = (Element)node;
                try {
                    convertor = datatype.getBuilder().buildConvertor(convertorConfigElement);
                } catch (Exception e) {
                    throw new SAXException("Error building convertor from convertor configuration embedded in selection list XML.", e);
                }
            } else if (node instanceof Element && FormsConstants.DEFINITION_NS.equals(node.getNamespaceURI()) && "item".equals(node.getLocalName())) {
                if (convertor == null) {
                    convertor = datatype.getConvertor();
                }
                Element element = (Element)node;
                String stringValue = element.getAttribute("value");
                Object value;
                if ("".equals(stringValue)) {
                    // Empty value translates into the null object
                    value = null;
                } else {
                    ConversionResult conversionResult = convertor.convertFromString(stringValue, Locale.US, formatCache);
                    if (!conversionResult.isSuccessful()) {
                        throw new Exception("Could not convert the value \"" + stringValue +
                                            "\" to the type " + datatype.getDescriptiveName() +
                                            ", defined at " + DomHelper.getLocation(element));
                    }
                    value = conversionResult.getResult();
                }

                XMLizable label = null;
                Element labelEl = DomHelper.getChildElement(element, FormsConstants.DEFINITION_NS, "label");
                if (labelEl != null) {
                    label = DomHelper.compileElementContent(labelEl);
                }
                selectionList.addItem(value, label);
            }
        }

        return selectionList;
    }

    private Element readSelectionList(String src) throws Exception {
        SourceResolver resolver = null;
        Source source = null;
        try {
            resolver = (SourceResolver)serviceManager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(src);
            InputSource inputSource = new InputSource(source.getInputStream());
            inputSource.setSystemId(source.getURI());
            Document document = DomHelper.parse(inputSource, this.serviceManager);
            Element selectionListElement = document.getDocumentElement();
            if (!FormsConstants.DEFINITION_NS.equals(selectionListElement.getNamespaceURI()) ||
                    !"selection-list".equals(selectionListElement.getLocalName())) {
                throw new Exception("Expected a fd:selection-list element at " +
                                    DomHelper.getLocation(selectionListElement));
            }

            return selectionListElement;
        } finally {
            if (resolver != null) {
                if (source != null) {
                    resolver.release(source);
                }
                serviceManager.release(resolver);
            }
        }
    }
}
