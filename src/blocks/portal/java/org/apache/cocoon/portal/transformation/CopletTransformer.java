/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;
import org.apache.cocoon.portal.event.impl.CopletJXPathEvent;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer offers various functions for developing pipeline based coplets.
 * 
 * Includes coplet instance data by using JXPath expressions.
 * The transformer searches for tags &lt;coplet:coplet xmlns:coplet="http://apache.org/cocoon/portal/coplet/1.0"&gt;.
 * They must have an attribute "select" that contains a valid JXPath expression applying to the coplet instance data.<br><br>
 *
 * Example:<br><br>
 * 
 * <pre>&lt;maxpageable xmlns:coplet="http://apache.org/cocoon/portal/coplet/1.0"&gt;
 * 	&lt;coplet:coplet select="copletData.maxpageable"/&gt;
 * &lt;/maxpageable&gt;<br></pre>
 * 
 * The transformer will insert the boolean value specifying whether the coplet is 
 * maxpageable or not.<br> 
 * Please see also the documentation of superclass AbstractCopletTransformer for how
 * the coplet instance data are acquired.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @version CVS $Id: CopletTransformer.java,v 1.11 2003/12/11 15:36:04 cziegeler Exp $
 */
public class CopletTransformer 
extends AbstractCopletTransformer {

    /**
     * The namespace URI to listen for.
     */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/coplet/1.0";
    
    /**
     * The XML element name to listen for.
     */
    public static final String COPLET_ELEM = "coplet";

    /**
     * The attribute containing the JXPath expression.
     */
    public static final String SELECT_ATTR = "select";

        
    /**
     * The XML element name to listen for.
     */
    public static final String LINK_ELEM = "link";

    /**
     * Creates new CopletTransformer.
     */
    public CopletTransformer() {
        this.defaultNamespaceURI = NAMESPACE_URI;
    }
    
    /**
     * Overridden from superclass.
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attr) 
    throws ProcessingException, IOException, SAXException {
        if (name.equals(COPLET_ELEM)) {
            String expression = attr.getValue(SELECT_ATTR);
            if (expression == null) {
                throw new ProcessingException("Attribute "+SELECT_ATTR+" must be spcified.");
            }
                
            CopletInstanceData cid = this.getCopletInstanceData();
            
            JXPathContext jxpathContext = JXPathContext.newContext( cid );
            Object object = jxpathContext.getValue(expression);
                
            if (object == null) {
                throw new ProcessingException("Could not find value for expression "+expression);
            }
                
        } else if (name.equals(LINK_ELEM)) {
            PortalService portalService = null;
            try {
                portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

                final LinkService linkService = portalService.getComponentManager().getLinkService();
                final String format = attr.getValue("format");
                AttributesImpl newAttrs = new AttributesImpl();
                newAttrs.setAttributes(attr);
                newAttrs.removeAttribute("format");

                if ( attr.getValue("href") != null ) {
                    final CopletInstanceData cid = this.getCopletInstanceData();
                    ChangeCopletInstanceAspectDataEvent event = new ChangeCopletInstanceAspectDataEvent(cid, null, null);
                    
                    String value = linkService.getLinkURI(event);
                    if (value.indexOf('?') == -1) {
                        value = value + '?' + attr.getValue("href");
                    } else {
                        value = value + '&' + attr.getValue("href");
                    }
                    newAttrs.removeAttribute("href");
                    this.output(value, format, newAttrs );
                } else {
                    final String path = attr.getValue("path");
                    final String value = attr.getValue("value");
                    
                    newAttrs.removeAttribute("path");
                    newAttrs.removeAttribute("value");
                    
                    JXPathEvent event;
                    if ( attr.getValue("layout") != null ) {
                        newAttrs.removeAttribute("layout");
                        final String layoutId = attr.getValue("layout");
                        Object layout = portalService.getComponentManager().getProfileManager().getPortalLayout(null, layoutId);
                        event = new JXPathEvent(layout, path, value);
                    } else {
                        String copletId = attr.getValue("coplet");
                        newAttrs.removeAttribute("coplet");
                        final CopletInstanceData cid = this.getCopletInstanceData(copletId);
                        event = new CopletJXPathEvent(cid, path, value);
                    }
                    final String href = linkService.getLinkURI(event);
                    this.output(href, format, newAttrs );
                }
            } catch (ServiceException e) {
                throw new SAXException("Error getting portal service.", e);
            } finally {
                this.manager.release( portalService );
            }
            
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
    }

    /**
     * Overridden from superclass.
     */
    public void endTransformingElement(String uri, String name, String raw) 
    throws ProcessingException, IOException, SAXException {
        if ( name.equals(LINK_ELEM) ) {
            String elem = (String)this.stack.pop();
            if ( elem.length() > 0 ) {
                this.sendEndElementEvent(elem);
            }
        } else if (!name.equals(COPLET_ELEM)) {
            super.endTransformingElement(uri, name, raw);
        }  
    }
    
    /**
     * Output the link
     */
    protected void output(String uri, String format, AttributesImpl newAttrs) 
    throws SAXException {
        if ( format == null ) {
            // default
            format = "html-link";
        }
        
        if ( "html-link".equals(format) ) {
            newAttrs.addCDATAAttribute("href", uri);
            this.sendStartElementEvent("a", newAttrs);
            this.stack.push("a");
        
        } else if ( "html-form".equals(format) ) {
            newAttrs.addCDATAAttribute("action", uri);
            this.sendStartElementEvent("form", newAttrs);
            this.stack.push("form");
        } else if ( "text".equals(format) ) {
            this.sendTextEvent(uri);
        } else if ( "parameters".equals(format) ) {
            final String value = uri.substring(uri.indexOf('?')+1);
            this.sendTextEvent(value);
        } else {
            // own format
            newAttrs.addCDATAAttribute("href", uri);
            this.sendStartElementEvent("link", newAttrs);
            this.stack.push("link");
        }
    }
}
