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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.excalibur.source.SourceUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer transforms html actions into events.
 * The transformer listens for the element a and form. Links
 * that only contain an anchor are ignored.
 * In addition if a link has the attribute "external" with the value
 * "true", the link is also ignored.
 *
 * TODO: Support target attribute
 *
 * @version $Id$
 */
public class HTMLEventLinkTransformer
extends AbstractCopletTransformer {

    /** The temporary attribute used to store the uri */
    protected String attributeName;

    /** The jxpath for the attribute */
    protected String jxPath;

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.attributeName = par.getParameter("attribute-name", "application-uri");
        this.jxPath = "temporaryAttributes/" + this.attributeName;
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String name, String raw, Attributes attr)
    throws SAXException {
        boolean processed = false;
        if ("a".equals(name) ) {
            final AttributesImpl a = this.getMutableAttributes(attr);
            attr = a;
            boolean convert = false;
            final boolean isRemoteAnchor = this.isRemoteAnchor(attr);
            if ( isRemoteAnchor ) {
                convert = !this.isExternalLink(a);
            }
            this.stack.push(convert ? Boolean.TRUE: Boolean.FALSE);
            if ( convert ) {
                this.createAnchorEvent(attr);
                processed = true;
            }
        } else if ("form".equals(name) ) {
            final AttributesImpl a = this.getMutableAttributes(attr);
            attr = a;
            boolean convert = !this.isExternalForm(a);
            this.stack.push(convert ? Boolean.TRUE: Boolean.FALSE);
            if ( convert ) {
                this.createFormEvent(attr);
                processed = true;
            }
        }
        if ( !processed ) {
            super.startElement(uri, name, raw, attr);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String name, String raw)
    throws SAXException {
        boolean processed = false;
        if ( "a".equals(name) ) {
            final Boolean converted = (Boolean)this.stack.pop();
            if ( converted.booleanValue() ) {
                this.xmlConsumer.endElement(CopletTransformer.NAMESPACE_URI,
                                            CopletTransformer.LINK_ELEM,
                                            "coplet:" + CopletTransformer.LINK_ELEM);
                this.xmlConsumer.endPrefixMapping("coplet");
                processed = true;
            }
        } else if ( "form".equals(name) ) {
            final Boolean converted = (Boolean)this.stack.pop();
            if ( converted.booleanValue() ) {
                this.xmlConsumer.endElement(CopletTransformer.NAMESPACE_URI,
                        CopletTransformer.LINK_ELEM,
                        "coplet:" + CopletTransformer.LINK_ELEM);
                this.xmlConsumer.endPrefixMapping("coplet");
                processed = true;
            }
        }
        if ( !processed ) {
            super.endElement(uri, name, raw);
        }
    }

    protected void createAnchorEvent(Attributes attributes)
    throws SAXException {
        final CopletInstance cid = this.getCopletInstanceData();
        final AttributesImpl newAttributes = new AttributesImpl(attributes);
        newAttributes.removeAttribute("href");
        newAttributes.removeAttribute("external");
        final String link = this.getLink((String)cid.getTemporaryAttribute(this.attributeName), attributes.getValue("href"));

        newAttributes.addCDATAAttribute("path", this.jxPath);
        newAttributes.addCDATAAttribute("value", link);
        newAttributes.addCDATAAttribute("coplet", cid.getId());
        newAttributes.addCDATAAttribute("format", "html-link");
        this.xmlConsumer.startPrefixMapping("coplet", CopletTransformer.NAMESPACE_URI);
        this.xmlConsumer.startElement(CopletTransformer.NAMESPACE_URI,
                                      CopletTransformer.LINK_ELEM,
                                      "coplet:" + CopletTransformer.LINK_ELEM,
                                      newAttributes);
    }

    protected void createFormEvent(Attributes attributes)
    throws SAXException {
        final CopletInstance cid = this.getCopletInstanceData();
        final AttributesImpl newAttributes = new AttributesImpl(attributes);
        newAttributes.removeAttribute("action");
        final String link = this.getLink((String)cid.getTemporaryAttribute(this.attributeName), attributes.getValue("action"));

        newAttributes.addCDATAAttribute("path", this.jxPath);
        newAttributes.addCDATAAttribute("value", link);
        newAttributes.addCDATAAttribute("coplet", cid.getId());
        newAttributes.addCDATAAttribute("format", "html-form");
        if ( newAttributes.getIndex("method") == -1 ) {
            newAttributes.addCDATAAttribute("method", "POST");
        }

        this.xmlConsumer.startPrefixMapping("coplet", CopletTransformer.NAMESPACE_URI);
        this.xmlConsumer.startElement(CopletTransformer.NAMESPACE_URI,
                CopletTransformer.LINK_ELEM,
                "coplet:" + CopletTransformer.LINK_ELEM,
                newAttributes);
    }

    protected String getLink(String base, String link) {
        final String v = SourceUtil.absolutize(base, link);
        return v;
    }

    /**
     * Determine if the element is an url and if the url points to some
     * remote source.
     *
     * @param attributes the attributes of the element
     * @return true if the href url is an anchor pointing to a remote source
     */
    protected boolean isRemoteAnchor(Attributes attributes) {
        String link = attributes.getValue("href");

        // no empty link to current document
        if (link != null && link.trim().length() > 0) {
            // check reference to document fragment
            if (!link.trim().startsWith("#")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is this link an external link?
     * A link in an external application is not transformed
     * if there is an attribute external="true" in the link-element
     * or if the link starts with "mailto:" or "javascript:".
     * 
     * @param attributes attributes of the node
     * @return true if the attribute 'external' is 'true'
     */
    private boolean isExternalLink (AttributesImpl attributes) {        
        final String external = attributes.getValue("external");
        // remote attribute
        if ( external != null ) {
            attributes.removeAttribute("external");
        }
        // links to external documents will be not transformed to portal links
        if (external != null && external.trim().length() > 0 
            && external.trim().toLowerCase().equals ("true") ) {            
            return true;
        }
        final String link = attributes.getValue("href");
        if ( link != null 
             && (link.startsWith("mailto:") || link.startsWith("javascript:") ) ) {
            return true;
        }
        return false;
    }

    /**
     * Does this form contain an external action?
     * A form is not transformed if there is an attribute
     * external="true" in the form action or if the action
     * starts with "javascript:".
     * 
     * @param attributes attributes of the node
     * @return True if the action is external.
     */
    private boolean isExternalForm(AttributesImpl attributes) {        
        final String external = attributes.getValue("external");
        // remote attribute
        if ( external != null ) {
            attributes.removeAttribute("external");
        }
        // links to external documents will be not transformed to portal links
        if (external != null && external.trim().length() > 0 
            && external.trim().toLowerCase().equals ("true") ) {            
            return true;
        }
        final String link = attributes.getValue("action");
        if ( link != null 
             && link.startsWith("javascript:") ) {
            return true;
        }
        return false;
    }
}
