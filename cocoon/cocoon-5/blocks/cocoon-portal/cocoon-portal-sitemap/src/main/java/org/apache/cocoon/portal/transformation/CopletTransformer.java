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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.event.coplet.CopletJXPathEvent;
import org.apache.cocoon.portal.event.impl.JXPathEvent;
import org.apache.cocoon.portal.event.layout.LayoutJXPathEvent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer offers various functions for developing pipeline based coplets.
 * All elements that are processed by this transformer have the namespace
 * "http://apache.org/cocoon/portal/coplet/1.0". We assume in the following docs
 * that a prefix "coplet" has been defined for this namespace.
 *
 * Including information from a coplet
 * With the "coplet" element, information from a coplet can be included in the document.
 * The element must have an attribute "select" that contains a valid JXPath expression
 * applying to the coplet instance data, e.g.
 * &lt;coplet:coplet select="title"&gt;
 * The above statement only works if the pipeline is a pipeline for the current coplet.
 * If you want to include information from an arbitrary coplet, you can use the "id"
 * attribute to specify the identifier of the instance.
 *
 * Example:<br><br>
 *
 * <pre>&lt;title xmlns:coplet="http://apache.org/cocoon/portal/coplet/1.0"&gt;
 * 	&lt;coplet:coplet select="title"/&gt;
 * &lt;/title&gt;<br></pre>
 *
 * The transformer will insert the string value of the coplet's title.
 *
 * Please see also the documentation of superclass AbstractCopletTransformer for how
 * the coplet instance data are acquired.
 *
 * @version $Id$
 */
public class CopletTransformer
extends AbstractCopletTransformer {

    /** The namespace URI to listen for. */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/coplet/1.0";

    /** The XML element name for including coplet information: coplet. */
    public static final String COPLET_ELEM = "coplet";

    /** The XML element name for including layout information: layout. */
    public static final String LAYOUT_ELEM = "coplet";

    /** The attribute containing the JXPath expression: select. */
    public static final String SELECT_ATTR = "select";


    /** The XML element name to listen for: link. */
    public static final String LINK_ELEM = "link";

    /** Create a link containing several events. */
    public static final String LINKS_ELEM = "links";

    /** Create a link containing several events. */
    public static final String PARAMETER_ELEM = "parameter";

    /** The content for the links element. */
    public static final String CONTENT_ELEM = "content";

    /** Are we inside a links element? */
    protected boolean insideLinks;

    /** The collected list of events. */
    protected List collectedEvents = new ArrayList();

    /** The content of the links. */
    protected XMLizable content;

    /** use ajax? */
    protected boolean useAjax = false;

    /**
     * Creates new CopletTransformer.
     */
    public CopletTransformer() {
        this.defaultNamespaceURI = NAMESPACE_URI;
        this.removeOurNamespacePrefixes = true;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        super.configure(configuration);
        this.useAjax = this.portalService.getConfigurationAsBoolean(Constants.CONFIGURATION_USE_AJAX, Constants.DEFAULT_CONFIGURATION_USE_AJAX);
    }

    /**
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
                throw new SAXException("Attribute '"+SELECT_ATTR+"' must be specified on element " + COPLET_ELEM + ".");
            }
            final String copletId = attr.getValue("id");
            final CopletInstance cid = this.getCopletInstanceData(copletId);
            if ( cid == null ) {
                throw new SAXException("Unable to find coplet instance data with id '" + copletId + "'.");
            }
            final JXPathContext jxpathContext = JXPathContext.newContext( cid );
            final Object object = jxpathContext.getValue(expression);
            if (object != null) {
                XMLUtils.valueOf(contentHandler, object);
            }
        } else if (name.equals(LAYOUT_ELEM) ) {
            String expression = attr.getValue(SELECT_ATTR);
            if (expression == null) {
                throw new SAXException("Attribute '"+SELECT_ATTR+"' must be specified on element " + LAYOUT_ELEM + ".");
            }
            final String layoutId = attr.getValue("id");
            if ( layoutId == null ) {
                throw new SAXException("Attribute 'id' must be specified on element " + LAYOUT_ELEM + ".");
            }
            final Layout l = this.portalService.getProfileManager().getLayout(layoutId);
            if ( l == null ) {
                throw new SAXException("Unable to find layout with id '" + layoutId + "'.");
            }
            final JXPathContext jxpathContext = JXPathContext.newContext( l );
            final Object object = jxpathContext.getValue(expression);
            if (object != null) {
                XMLUtils.valueOf(contentHandler, object);
            }            
        } else if (name.equals(LINK_ELEM)) {

            final LinkService linkService = this.portalService.getLinkService();
            final String format = attr.getValue("format");
            AttributesImpl newAttrs = new AttributesImpl();
            newAttrs.setAttributes(attr);
            newAttrs.removeAttribute("format");

            if ( attr.getValue("href") != null ) {
                final CopletInstance cid = this.getCopletInstanceData();
                CopletJXPathEvent event = new CopletJXPathEvent(cid, null, null);

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
                    Layout layout = this.portalService.getProfileManager().getLayout(layoutId);
                    if ( layout != null ) {
                        event = new LayoutJXPathEvent(layout, path, value);
                    }
                } else {
                    String copletId = attr.getValue("coplet");
                    newAttrs.removeAttribute("coplet");
                    final CopletInstance cid = this.getCopletInstanceData(copletId);
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
        } else if (name.equals(PARAMETER_ELEM)) {
            if (this.insideLinks) {
                String href = attr.getValue("href");
                if ( href != null ) {
                    final int pos = href.indexOf('?');
                    if ( pos != -1 ) {
                        href = href.substring(pos+1);
                    }
                    this.collectedEvents.add(new LinkService.ParameterDescription(href));
                }
            }
        } else if (name.equals(LINKS_ELEM) ) {
            this.insideLinks = true;
            final AttributesImpl newAttrs = new AttributesImpl();
            newAttrs.setAttributes(attr);
            newAttrs.removeAttribute("format");
            this.stack.push(newAttrs);
            
            String format = attr.getValue("format");
            if ( format == null ) {
                format = "html-link";
            }
            this.stack.push(format);
        } else if ( name.equals(CONTENT_ELEM) && this.insideLinks ) {
            this.startSAXRecording();
        } else {
            throw new SAXException("Unknown element '"+name+"' in namespace '"+NAMESPACE_URI+"'.");
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
            final LinkService linkService = this.portalService.getLinkService();
            String href = linkService.getLinkURI(this.collectedEvents);

            AttributesImpl newAttrs = (AttributesImpl)this.stack.pop();
            // test for alternate base url
            final String baseURL = newAttrs.getValue("base-url");
            if ( baseURL != null ) {
                newAttrs.removeAttribute("base-url");
                int pos = href.indexOf('?') + 1;
                final char separator;
                if ( baseURL.indexOf('?') == -1 ) {
                    separator = '?';
                } else {
                    separator = '&';
                }
                href = baseURL + separator + href.substring(pos);
                
            }
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
            if ( this.useAjax ) {
                newAttrs.addCDATAAttribute("href", "javascript:cocoon.portal.process('" + uri + "');");
            } else {
                newAttrs.addCDATAAttribute("href", uri);                
            }
            this.sendStartElementEvent("a", newAttrs);
            this.stack.push("a");

        } else if ( "html-form".equals(format) ) {
            boolean addParametersAsHiddenFields = false;
            String reqParams = null;
            final String enctype = newAttrs.getValue("enctype");
            if ( enctype== null 
                || "application/x-www-form-urlencoded".equalsIgnoreCase(enctype)
                || "multipart/form-data".equalsIgnoreCase(enctype) )  {
                final int pos = uri.indexOf('?');
                if ( pos != -1 ) {
                    reqParams = uri.substring(pos+1);
                    uri = uri.substring(0, pos);
                    addParametersAsHiddenFields = true;
                }
            }
            newAttrs.addCDATAAttribute("action", uri);
            this.sendStartElementEvent("form", newAttrs);
            this.stack.push("form");
            if ( addParametersAsHiddenFields ) {
                // create hidden input fields
                RequestParameters pars = new RequestParameters(reqParams);
                Enumeration enumeration = pars.getParameterNames();
                while ( enumeration.hasMoreElements() ) {
                    final String pName = (String)enumeration.nextElement();
                    final String[] pValues = pars.getParameterValues(pName);
                    for(int k=0; k<pValues.length; k++) {
                        final String pValue = pValues[k];
                        AttributesImpl hiddenAttrs = new AttributesImpl();
                        hiddenAttrs.addCDATAAttribute("type", "hidden");
                        hiddenAttrs.addCDATAAttribute("name", pName);
                        hiddenAttrs.addCDATAAttribute("value", pValue);
                        this.startElement("", "input", "input", hiddenAttrs);
                        this.endElement("", "input", "input");
                    }
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
