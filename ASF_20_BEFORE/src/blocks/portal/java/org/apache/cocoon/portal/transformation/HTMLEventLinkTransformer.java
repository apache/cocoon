/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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
 * @version CVS $Id: HTMLEventLinkTransformer.java,v 1.1 2004/02/12 09:32:37 cziegeler Exp $
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
