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
package org.apache.cocoon.woody.datatype.typeimpl;

import org.apache.cocoon.woody.datatype.*;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;

/**
 * Abstract base class for datatype builders, most concrete datatype builders
 * will derive from this class.
 */
public abstract class AbstractDatatypeBuilder implements DatatypeBuilder, Composable {
    protected ComponentManager componentManager;

    public void compose(ComponentManager componentManager) throws ComponentException {
        this.componentManager = componentManager;
    }

    protected Source resolve(String src) throws ComponentException, IOException {
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver)componentManager.lookup(SourceResolver.ROLE);
            return resolver.resolveURI(src);
        } finally {
            if (resolver != null)
                componentManager.release(resolver);
        }
    }

    protected void buildSelectionList(Element datatypeElement, AbstractDatatype datatype) throws Exception {
        Element selectionListElement = DomHelper.getChildElement(datatypeElement, Constants.WD_NS, "selection-list");
        if (selectionListElement != null) {
            SelectionList selectionList;
            String src = selectionListElement.getAttribute("src");
            if (src.length() > 0) {
                boolean cache = DomHelper.getAttributeAsBoolean(selectionListElement, "cache", true);
                if (cache) {
                    selectionListElement = readSelectionList(src);
                    selectionList = SelectionListBuilder.build(selectionListElement, datatype);
                } else {
                    selectionList = new DynamicSelectionList(datatype, src, componentManager);
                }
            } else {
                // selection list is defined inline
                selectionList = SelectionListBuilder.build(selectionListElement, datatype);
            }
            datatype.setSelectionList(selectionList);
        }
    }

    private Element readSelectionList(String src) throws Exception {
        Source source = resolve(src);
        InputSource inputSource = new InputSource(source.getInputStream());
        inputSource.setSystemId(source.getURI());
        Document document = DomHelper.parse(inputSource);
        Element selectionListElement = document.getDocumentElement();
        if (!Constants.WD_NS.equals(selectionListElement.getNamespaceURI()) || !"selection-list".equals(selectionListElement.getLocalName()))
            throw new Exception("Excepted a wd:selection-list element at " + DomHelper.getLocation(selectionListElement));
        return selectionListElement;
    }

    protected void buildValidationRules(Element datatypeElement, AbstractDatatype datatype, DatatypeManager datatypeManager) throws Exception {
        Element validationElement = DomHelper.getChildElement(datatypeElement, Constants.WD_NS, "validation");
        if (validationElement != null) {
            Element[] validationElements = DomHelper.getChildElements(validationElement, Constants.WD_NS);
            for (int i = 0; i < validationElements.length; i++) {
                ValidationRule rule = datatypeManager.createValidationRule(validationElements[i]);
                if (!rule.supportsType(datatype.getTypeClass(), datatype.isArrayType())) {
                    throw new Exception("Validation rule \"" + validationElements[i].getLocalName() + "\" cannot be used with strings, error at " + DomHelper.getLocation(validationElements[i]));
                }
                datatype.addValidationRule(rule);
            }
        }
    }
}
