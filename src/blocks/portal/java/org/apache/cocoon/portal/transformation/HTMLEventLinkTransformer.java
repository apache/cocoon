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
import org.apache.avalon.framework.service.Serviceable;
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
 * The transformer listens for the element a and form.
 * Current we only support POSing of forms.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: HTMLEventLinkTransformer.java,v 1.2 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class HTMLEventLinkTransformer 
extends AbstractCopletTransformer 
implements Serviceable {
    
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
        if ("a".equals(name) ) {
            this.createAnchorEvent(uri, name, raw, attr);
        } else if ("form".equals(name) ) {
            this.createFormEvent(uri, name, raw, attr);
        } else {
            super.startElement(uri, name, raw, attr);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String name, String raw)
    throws SAXException {
        if ( "a".equals(name) || "form".equals("name") ) {
            this.xmlConsumer.endElement(CopletTransformer.NAMESPACE_URI,
                                        CopletTransformer.LINK_ELEM,
                                        "coplet:" + CopletTransformer.LINK_ELEM);
            this.xmlConsumer.endPrefixMapping("coplet");
        } else {
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
}
