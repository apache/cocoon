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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import org.apache.cocoon.portal.util.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Base class for aspect renderers that generate cinclude statements. Provides a single
 * method for creating the cinclude tag.
 *
 * @version $Id$
 */
public abstract class AbstractCIncludeAspect
    extends AbstractAspect {

    protected static final String PREFIX = "cinclude";
    protected static final String NAMESPACE = "http://apache.org/cocoon/include/1.0";
    protected static final String ELEMENT = "include";
    protected static final String QELEMENT= PREFIX + ":" + ELEMENT;
    protected static final String ATTRIBUTE = "src";

    /**
     * Create the cinclude statement.
     *
     * @param source attribute value for the cinclude tag
     * @param handler SAX event handler
     */
    protected void createCInclude(String source, ContentHandler handler)
    throws SAXException {
        handler.startPrefixMapping(PREFIX, NAMESPACE);
        final AttributesImpl attributes = new AttributesImpl();
        XMLUtils.addCDATAAttribute(attributes, ATTRIBUTE, source);
        handler.startElement(NAMESPACE, ELEMENT, QELEMENT, attributes);
        handler.endElement(NAMESPACE, ELEMENT, QELEMENT);
        handler.endPrefixMapping(PREFIX);
    }
}
