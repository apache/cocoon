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
package org.apache.cocoon.forms.datatype;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * Builds {@link SelectionList}s from an XML description or an URL.
 *
 * <p>Note: the class {@link DynamicSelectionList} also interprets the same wd:selection-list XML, so if
 * anything changes here to how that XML is interpreted, it also needs to change over there and vice versa.
 * 
 * @version CVS $Id: DefaultSelectionListBuilder.java,v 1.1 2004/03/09 10:34:01 reinhard Exp $
 */
public class DefaultSelectionListBuilder implements SelectionListBuilder, Serviceable {
    
    private ServiceManager serviceManager;

    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }
    
    public SelectionList build(Element selectionListElement, Datatype datatype) throws Exception {
        SelectionList selectionList;
        String src = selectionListElement.getAttribute("src");
        if (src.length() > 0) {
            boolean dynamic = DomHelper.getAttributeAsBoolean(selectionListElement, "dynamic", false);
            if (!dynamic) {
                selectionListElement = readSelectionList(src);
                selectionList = buildStaticList(selectionListElement, datatype);
            } else {
                selectionList = new DynamicSelectionList(datatype, src, serviceManager);
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

        NodeList children = selectionListElement.getChildNodes();
        for (int i = 0; children.item(i) != null; i++) {
            Node node = children.item(i);
            if (convertor == null && node instanceof Element && Constants.FD_NS.equals(node.getNamespaceURI()) && "convertor".equals(node.getLocalName())) {
                Element convertorConfigElement = (Element)node;
                try {
                    convertor = datatype.getBuilder().buildConvertor(convertorConfigElement);
                } catch (Exception e) {
                    throw new SAXException("Error building convertor from convertor configuration embedded in selection list XML.", e);
                }
            } else if (node instanceof Element && Constants.FD_NS.equals(node.getNamespaceURI()) && "item".equals(node.getLocalName())) {
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
                    value = convertor.convertFromString(stringValue, Locale.US, formatCache);
                    if (value == null) {
                        throw new Exception("Could not convert the value \"" + stringValue +
                                            "\" to the type " + datatype.getDescriptiveName() +
                                            ", defined at " + DomHelper.getLocation(element));
                    }
                }

                XMLizable label = null;
                Element labelEl = DomHelper.getChildElement(element, Constants.FD_NS, "label");
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
            Document document = DomHelper.parse(inputSource);
            Element selectionListElement = document.getDocumentElement();
            if (!Constants.FD_NS.equals(selectionListElement.getNamespaceURI()) || !"selection-list".equals(selectionListElement.getLocalName()))
                throw new Exception("Excepted a wd:selection-list element at " + DomHelper.getLocation(selectionListElement));
            return selectionListElement;
        } finally {
            if (source != null)
                resolver.release(source);
            if (resolver != null)
                serviceManager.release(resolver);
        }
    }
}
