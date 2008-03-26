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
package org.apache.cocoon.ajax;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.RedundantNamespacesFilter;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The <code>BrowserUpdateTransformer</code> transformer filters the result of
 * the form template, so that only updated widgets are sent back to the browser
 *
 * @cocoon.sitemap.component.documentation
 * The <code>BrowserUpdateTransformer</code> transformer filters the result of
 * the form template, so that only updated widgets are sent back to the browser
 * @cocoon.sitemap.component.documentation.caching No
 *
 * @version $Id$
 */
public class BrowserUpdateTransformer extends AbstractTransformer {
    
    public static final String AJAXMODE_PARAM = "cocoon-ajax";
    public static final String BU_NSURI = "http://apache.org/cocoon/browser-update/1.0";
    
    private boolean ajaxRequest = false;
    
    private int replaceDepth = 0;
    
    private boolean inUpdateTag = false;
    private String updateTagId = null;
    
    Locator locator;


    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

        Request request = ObjectModelHelper.getRequest(objectModel);
        this.ajaxRequest = request.getParameter(AJAXMODE_PARAM) != null;
        this.replaceDepth = 0;
        this.inUpdateTag = false;
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    public void startDocument() throws SAXException {
        
        if (ajaxRequest) {
            // Add the namespace filter to our own output.
            // This is needed as flattening bu:* elements can lead to some weird reordering of
            // namespace declarations...
            RedundantNamespacesFilter nsPipe = new RedundantNamespacesFilter();
            if (this.xmlConsumer != null) {
                nsPipe.setConsumer(this.xmlConsumer);
            } else {
                nsPipe.setContentHandler(this.contentHandler);
            }
            setConsumer(nsPipe);
        }
        
        super.startDocument();
        if (ajaxRequest) {
            // Add a root element. The original one is very likely to be stripped
            super.startPrefixMapping("bu", BU_NSURI);
            super.startElement(BU_NSURI, "document", "bu:document", new AttributesImpl());
        }
    }
    
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // Passthrough if not in ajax mode or if in a bu:replace
        if (!this.ajaxRequest || this.replaceDepth > 0) {
            super.startPrefixMapping(prefix, uri);
        }
    }

    public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
        if (BU_NSURI.equals(uri) && "replace".equals(loc)) {
            // Keep the id attribute. It may be null, in which case the one of the
            // child element will be used.
            this.updateTagId = attrs.getValue("id");
            this.inUpdateTag = true;
            if (this.ajaxRequest && this.replaceDepth == 0) {
                // Pass this element through
                super.startElement(uri, loc, raw, attrs);
            }
            replaceDepth++;
        } else {
            // Passthrough if not in ajax mode or if in a bu:replace
            
            // Is the enclosing element a bu:replace?
            if (this.inUpdateTag) {
                this.inUpdateTag = false;
                // Is there already an id?
                String localId = attrs.getValue("id");
                if (localId != null) {
                    // Yes: check it's the same
                    if (this.updateTagId != null && !localId.equals(this.updateTagId)) {
                        throw new SAXParseException("Id on bu:replace (" + this.updateTagId + ") and " + raw + " (" +
                                localId + ") don't match.", this.locator);
                    }
                } else {
                    // No: add it
                    if (this.updateTagId == null) {
                        throw new SAXParseException("Neither bu:replace nor " + raw + " have an id attribute.", this.locator);
                    }
                    AttributesImpl newAttrs = new AttributesImpl(attrs);
                    newAttrs.addCDATAAttribute("id", this.updateTagId);
                    attrs = newAttrs;
                }
                this.updateTagId = null;
            }
            if (!this.ajaxRequest || this.replaceDepth > 0) {
                super.startElement(uri, loc, raw, attrs);
            }
        }
    }

    public void characters(char[] buffer, int offset, int len) throws SAXException {
        if (this.inUpdateTag) {
            // Check that it's only spaces
            for (int i = offset; i < len; i++) {
                if (!Character.isWhitespace(buffer[i])) {
                    throw new SAXParseException("bu:replace must include a single child element and no text.", this.locator);
                }
            }
        }
        if (!this.ajaxRequest || this.replaceDepth > 0) {
            super.characters(buffer, offset, len);
        }
    }
    
    public void endElement(String uri, String loc, String raw) throws SAXException {
        if (BU_NSURI.equals(uri) && "replace".equals(loc)) {
            replaceDepth--;
            if (this.ajaxRequest && this.replaceDepth == 0) {
                // Pass this element through
                super.endElement(uri, loc, raw);
            }
        } else {
            // Passthrough if not in ajax mode or if in a bu:replace
            if (!this.ajaxRequest || this.replaceDepth > 0) {
                super.endElement(uri, loc, raw);
            }
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // Passthrough if not in ajax mode or if in a bu:replace
        if (!this.ajaxRequest || this.replaceDepth > 0) {
            super.endPrefixMapping(prefix);
        }
    }

    public void endDocument() throws SAXException {
        if (ajaxRequest) {
            super.endElement(BU_NSURI, "document", "bu:document");
            super.endPrefixMapping("bu");
        }
        super.endDocument();
    }

    public void recycle() {
        this.locator = null;
        this.updateTagId = null;
        super.recycle();
    }
}
