/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.excalibur.source.SourceUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer extends transforms html actions
 * into events.
 * The transformer listens for the element a and form. Links
 * that only contain an anchor are ignored.
 * Current we only support POSTing of forms.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: HTMLEventLinkTransformer.java,v 1.5 2004/03/26 09:36:30 cziegeler Exp $
 */
public class HTMLEventLinkTransformer 
extends AbstractCopletTransformer {
    
    /** The temporary attribute used to store the uri */
    protected String attributeName;
    
    /** The jxpath for the attribute */
    protected String jxPath;
    
    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String name, String raw, Attributes attr)
    throws SAXException {
        boolean processed = false;
        if ("a".equals(name) ) {
            final boolean isRemoteAnchor = this.isRemoteAnchor(name, attr);
            this.stack.push(Boolean.valueOf(isRemoteAnchor));
            if ( isRemoteAnchor ) {
                this.createAnchorEvent(uri, name, raw, attr);
                processed = true;
            }
        } else if ("form".equals(name) ) {
            this.createFormEvent(uri, name, raw, attr);
            processed = true;
        }
        if ( !processed ) {
            super.startElement(uri, name, raw, attr);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String name, String raw)
    throws SAXException {
        boolean processed = false;
        if ( "a".equals(name) ) {
            final Boolean isRemoteAnchor = (Boolean)this.stack.pop();
            if ( isRemoteAnchor.booleanValue() ) {
                this.xmlConsumer.endElement(CopletTransformer.NAMESPACE_URI,
                                            CopletTransformer.LINK_ELEM,
                                            "coplet:" + CopletTransformer.LINK_ELEM);
                this.xmlConsumer.endPrefixMapping("coplet");
                processed = true;
            }
        } else if ( "form".equals(name) ) {
            this.xmlConsumer.endElement(CopletTransformer.NAMESPACE_URI,
                    CopletTransformer.LINK_ELEM,
                    "coplet:" + CopletTransformer.LINK_ELEM);
            this.xmlConsumer.endPrefixMapping("coplet");
            processed = true;            
        }
        if ( !processed ) {
            super.endElement(uri, name, raw);
        }
    }

    protected void createAnchorEvent(String uri, String name, String raw, Attributes attributes) 
    throws SAXException {
        AttributesImpl newAttributes = new AttributesImpl();
        String link = attributes.getValue("href");
        
        CopletInstanceData cid = this.getCopletInstanceData();
        link = this.getLink((String)cid.getTemporaryAttribute(this.attributeName), link);

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

    protected void createFormEvent(String uri, String name, String raw, Attributes attributes) 
    throws SAXException {
        AttributesImpl newAttributes = new AttributesImpl();
        String link = attributes.getValue("action");
        
        CopletInstanceData cid = this.getCopletInstanceData();
        link = this.getLink((String)cid.getTemporaryAttribute(this.attributeName), link);
        
        newAttributes.addCDATAAttribute("path", this.jxPath);
        newAttributes.addCDATAAttribute("value", link);
        newAttributes.addCDATAAttribute("coplet", cid.getId());
        newAttributes.addCDATAAttribute("format", "html-form");
        newAttributes.addCDATAAttribute("method", "POST");
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
     * @param name the name of the element
     * @param attributes the attributes of the element
     * @return true if the href url is an anchor pointing to a remote source
     */
    protected boolean isRemoteAnchor(String name, Attributes attributes) {
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
    
}
