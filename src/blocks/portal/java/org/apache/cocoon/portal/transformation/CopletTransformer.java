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
package org.apache.cocoon.portal.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;
import org.apache.cocoon.portal.event.impl.CopletJXPathEvent;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.excalibur.xml.sax.XMLizable;
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
 * @version CVS $Id: CopletTransformer.java,v 1.19 2004/04/01 10:25:41 cziegeler Exp $
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

    /** Create a link containing several events */
    public static final String LINKS_ELEM = "links";
    
    /** The content for the links element */
    public static final String CONTENT_ELEM = "content";

    /** Are we inside a links element? */
    protected boolean insideLinks;
    
    /** The collected list of events */
    protected List collectedEvents = new ArrayList();
    
    /** The content of the links */
    protected XMLizable content;
    
    /**
     * Creates new CopletTransformer.
     */
    public CopletTransformer() {
        this.defaultNamespaceURI = NAMESPACE_URI;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#setupTransforming()
     */
    public void setupTransforming()
    throws IOException, ProcessingException, SAXException {
        super.setupTransforming();
        this.insideLinks = false;
        this.content = null;
        this.collectedEvents.clear();
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

            final LinkService linkService = this.getPortalService().getComponentManager().getLinkService();
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
                
                JXPathEvent event = null;
                if ( attr.getValue("layout") != null ) {
                    newAttrs.removeAttribute("layout");
                    final String layoutId = attr.getValue("layout");
                    Object layout = this.getPortalService().getComponentManager().getProfileManager().getPortalLayout(null, layoutId);
                    if ( layout != null ) {
                        event = new JXPathEvent(layout, path, value);
                    }
                } else {
                    String copletId = attr.getValue("coplet");
                    newAttrs.removeAttribute("coplet");
                    final CopletInstanceData cid = this.getCopletInstanceData(copletId);
                    if ( cid != null ) {
                        event = new CopletJXPathEvent(cid, path, value);
                    }
                }
                if ( this.insideLinks ) {
                    if ( event != null ) {
                        this.collectedEvents.add(event);
                    }
                } else {
                    final String href = linkService.getLinkURI(event);
                    this.output(href, format, newAttrs );
                }
            }
        } else if (name.equals(LINKS_ELEM) ) {
            this.insideLinks = true;
            String format = attr.getValue("format");
            if ( format == null ) {
                format = "html-link";
            }
            this.stack.push(format);
        } else if ( name.equals(CONTENT_ELEM) && this.insideLinks ) {
            this.startSAXRecording();
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
            if ( !this.insideLinks ) {
                String elem = (String)this.stack.pop();
                if ( elem.length() > 0 ) {
                    this.sendEndElementEvent(elem);
                }
            }
        } else if ( name.equals(LINKS_ELEM) ) {
            this.insideLinks = false;
            final String format = (String)this.stack.pop();
            final LinkService linkService = this.getPortalService().getComponentManager().getLinkService();
            
            final String href = linkService.getLinkURI(this.collectedEvents);
            final AttributesImpl newAttrs = new AttributesImpl();
            this.output(href, format, newAttrs );

            this.collectedEvents.clear();
            if ( this.content != null ) {
                this.content.toSAX(this.contentHandler);
                this.content = null;
            }
            String elem = (String)this.stack.pop();
            if ( elem.length() > 0 ) {
                this.sendEndElementEvent(elem);
            }
        } else if ( name.equals(CONTENT_ELEM) && this.insideLinks ) {
            this.content = this.endSAXRecording();
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
            boolean addParametersAsHiddenFields = false;
            String parameters = null;
            if ( newAttrs.getValue("enctype") != null )  {
                final int pos = uri.indexOf('?');
                if ( pos != -1 ) {
                    parameters = uri.substring(pos+1);
                    uri = uri.substring(0, pos);
                    addParametersAsHiddenFields = true;
                }
            }
            newAttrs.addCDATAAttribute("action", uri);
            this.sendStartElementEvent("form", newAttrs);
            this.stack.push("form");
            if ( addParametersAsHiddenFields ) {
                // create hidden input fields
                RequestParameters pars = new RequestParameters(parameters);
                Enumeration enum = pars.getParameterNames();
                while ( enum.hasMoreElements() ) {
                    String pName = (String)enum.nextElement();
                    String pValue = pars.getParameter(pName);
                    AttributesImpl hiddenAttrs = new AttributesImpl();
                    hiddenAttrs.addCDATAAttribute("type", "hidden");
                    hiddenAttrs.addCDATAAttribute("name", pName);
                    hiddenAttrs.addCDATAAttribute("value", pValue);
                    this.startElement("", "input", "input", hiddenAttrs);
                    this.endElement("", "input", "input");
                }
                
            }
        } else if ( "text".equals(format) ) {
            this.sendTextEvent(uri);
            this.stack.push("");
        } else if ( "parameters".equals(format) ) {
            final String value = uri.substring(uri.indexOf('?')+1);
            this.sendTextEvent(value);
            this.stack.push("");
        } else {
            // own format
            newAttrs.addCDATAAttribute("href", uri);
            this.sendStartElementEvent("link", newAttrs);
            this.stack.push("link");
        }
    }
}
