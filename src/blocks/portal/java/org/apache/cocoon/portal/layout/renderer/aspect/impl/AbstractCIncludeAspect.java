/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import org.apache.cocoon.transformation.CIncludeTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Base class for aspect renderers that generate cinclude statements. Provides a single
 * method for creating the cinclude tag.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AbstractCIncludeAspect.java,v 1.4 2004/04/25 20:09:34 haul Exp $
 */
public abstract class AbstractCIncludeAspect 
    extends AbstractAspect {

    protected static final String PREFIX = "cinclude";
    protected static final String NAMESPACE = CIncludeTransformer.CINCLUDE_NAMESPACE_URI;
    protected static final String ELEMENT = CIncludeTransformer.CINCLUDE_INCLUDE_ELEMENT;
    protected static final String QELEMENT= PREFIX + ":" + ELEMENT;
    protected static final String ATTRIBUTE = CIncludeTransformer.CINCLUDE_INCLUDE_ELEMENT_SRC_ATTRIBUTE; 

    /**
     * Create the cinclude statement.
     * 
     * @param source attribute value for the cinclude tag
     * @param handler SAX event handler
     */
    protected void createCInclude(String source, ContentHandler handler)
    throws SAXException {
        handler.startPrefixMapping(PREFIX, NAMESPACE);
        AttributesImpl attributes = new AttributesImpl();
        attributes.addCDATAAttribute("src", source);
        handler.startElement(NAMESPACE, ELEMENT, QELEMENT, attributes);
        handler.endElement(NAMESPACE, ELEMENT, QELEMENT);
        handler.endPrefixMapping(PREFIX);
    }

}
