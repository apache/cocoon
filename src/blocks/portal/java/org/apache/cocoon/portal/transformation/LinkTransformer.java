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
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This Transformer deals with tags containing links to external applications that need to be converted so
 * that not the external application will be called directly but the request gets routed via the cocoon portal 
 * (either via proxy transformer or proxy reader).
 * The link transformer therefore cooperates with the event link transformer.
 * Tags that include a link for which a link event needs to be generated will be converted to
 * &lt;eventlink&gt; elements.
 * Examples:<br><br>
 * 
 * <pre>
 * &lt;img src="images/logo.jpg"&gt; will be converted to use the proxy reader:
 * &lt;img src="proxy-images/logo.jpg&cocoon-portal-copletid=xxx&cocoon-portal-portalname=yyy
 * <br>
 * &lt;form action="/submitted.jsp"&gt; will be converted to be processed by the event link transformer
 * &lt;eventlink action="/submitted.jsp" attribute="action" element="form"&gt;
 * </pre>
 * 
 * @author <a href="mailto:gernot.koller@rizit.at">Gernot Koller</a>
 * @author <a href="mailto:friedrich.klenner@rzb.at">Friedrich Klenner</a> 
 * 
 * @version CVS $Id: LinkTransformer.java,v 1.5 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class LinkTransformer
    extends AbstractTransformer
    implements Serviceable {

    /**
     * Namespace prefix usef vor NewEventLinkTransformer-Namespace
     */
    public static final String NAMESPACE_PREFIX = "ev";

    /**
     * Used for appending a request parameter containing the coplet id
     */
    protected String copletIdParamString = null;

    /**
     * Used for appending a request parameter containing the portal name
     */
    protected String portalNameParamString = null;

    /**
     * The coplet instance data
     */
    protected CopletInstanceData copletInstanceData = null;

    /**
     * The html document base uri
     */
    protected String documentBase = null;

    /**
     * Used to store elements' name between startTransformingElement and endTransformingElement. 
     */
    protected Stack elementStack = new Stack();

    /**
     * The avalon service manager
     */
    protected ServiceManager manager;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * @see AbstractTransformer#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(
        SourceResolver resolver,
        Map objectModel,
        String src,
        Parameters par)
        throws ProcessingException, SAXException, IOException {
        copletInstanceData =
            ProxyTransformer.getInstanceData(
                this.manager,
                objectModel,
                par);
        copletIdParamString =
            ProxyTransformer.COPLETID + "=" + copletInstanceData.getId();

        Map context = (Map) objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
        portalNameParamString =
            ProxyTransformer.PORTALNAME
                + "="
                + (String) context.get(Constants.PORTAL_NAME_KEY);
    }

    /**
     * Recycle this component.
     * All instance variables are set to <code>null</code>.
     */
    public void recycle() {
        copletInstanceData = null;
        elementStack.clear();
        copletIdParamString = null;
        portalNameParamString = null;
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        super.startDocument();
        documentBase =
        (String)copletInstanceData.getAttribute(ProxyTransformer.DOCUMENT_BASE);
        super.startPrefixMapping(NAMESPACE_PREFIX,
                                 NewEventLinkTransformer.NAMESPACE_URI);
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        super.endPrefixMapping(NAMESPACE_PREFIX);
        super.endDocument();
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String name, String raw,
                             Attributes attributes)
        throws SAXException {

        if ("form".equalsIgnoreCase(name)) {
            handleTag(
                "action",
                uri,
                name,
                raw,
                attributes,
                true,
                (attributes.getIndex("target") > -1));
        }
        else if ("script".equalsIgnoreCase(name)) {
            handleTag("src", uri, name, raw, attributes, false, false);
        }
        else if ("img".equalsIgnoreCase(name)) {
            handleTag("src", uri, name, raw, attributes, false, false);
        }
        else if ("link".equalsIgnoreCase(name)) {
            handleTag("href", uri, name, raw, attributes, false, false);
        }
        else if ("a".equalsIgnoreCase(name)) {
            handleTag(
                "href",
                uri,
                name,
                raw,
                attributes,
                true,
                (attributes.getIndex("target") > -1));
        }
        else if ("menu-item".equalsIgnoreCase(name)) {
            handleTag("href", uri, name, raw, attributes, true, false);
        }
        else if ("input".equalsIgnoreCase(name)) {
            handleTag("src", uri, name, raw, attributes, false, false);
        }
        else if ("applet".equalsIgnoreCase(name)) {
            if (attributes.getIndex("codebase") > -1) {
                handleTag("codebase", uri, name, raw, attributes, false, true);
            }
        }
        else {
            super.startElement(uri, name, raw, attributes);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String name, String raw)
        throws SAXException {
        String elementName = null;

        if (!elementStack.empty()) {
            elementName = (String) elementStack.peek();
        }

        if (elementName != null && elementName.equals(name)) {
            super.endElement(
                NewEventLinkTransformer.NAMESPACE_URI,
                NewEventLinkTransformer.EVENT_ELEM,
                NAMESPACE_PREFIX + ":" + NewEventLinkTransformer.EVENT_ELEM);
        }
        else {
            super.endElement(uri, name, raw);
        }
    }

    /**
     * The handleTag method is responsible for preparing tags so that they can either be conveted to
     * link events by the event link transformer or that the proxy reader is called directly.
     * Tags with absolute links (starting with "http://", "ftp://", etc.) will not be touched.
     * Tags which contain a target attribute will be modified to call the uri directly 
     * (no proxy reader or proxy transformer involved).
     * Tags (like &lt;a href="uri"&ht; or &lt;form action="uri"&gt, etc.) that require a link event will be converted to 
     * &lt;eventlink&gt; elements, so that the  event link transformer can create the necessary link event 
     * and the proxy transformer will be used.
     * Tags (like &lt;img src="uri"&gt;) that shoud call the proxy reader will be converted to do so.
     *
     * Examples:<br><br>
     * 
     * <pre>
     * &lt;a href="http://www.somewhere.com"&gt; will not be converted because of absolute url
     * <br>
     * &lt;img src="images/logo.jpg"&gt; will be converted to use the proxy reader:
     * &lt;img src="proxy-images/logo.jpg&cocoon-portal-copletid=xxx&cocoon-portal-portalname=yyy
     * <br>
     * &lt;form action="/submitted.jsp"&gt; will be converted to use proxy transformer:
     * &lt;eventlink action="/submitted.jsp" attribute="action" element="form"&gt;
     * </pre>
     * 
     * @param attributeName Name oft the attribute containing the link to be converted
     * @param uri Namespace URI
     * @param elementName Name of the element (tag)
     * @param raw Raw name of the element (including namespace prefix)
     * @param attributes Attributes of the element
     * @param eventLink True signals that the tag sould be converted to an  event link tag.
     * @param direct True signals that the uri should point directly to the external resource (no proxys)
     * @throws SAXException if an invalid URL was detected.
     */
    public void handleTag(
        String attributeName,
        String uri,
        String elementName,
        String raw,
        Attributes attributes,
        boolean eventLink,
        boolean direct)
        throws SAXException {
        String remoteURI = attributes.getValue(attributeName);

        if ((remoteURI == null)
            || remoteURI.startsWith("http://")
            || remoteURI.startsWith("https://")
            || remoteURI.startsWith("#")
            || remoteURI.startsWith("ftp://")
            || remoteURI.startsWith("javascript:")
            || remoteURI.startsWith("mailto:")) {
            super.startElement(uri, elementName, raw, attributes);
        }
        else {
            if (attributes.getIndex("target") > -1 || direct) {
                try {
                    remoteURI =
                        ProxyTransformer.resolveURI(remoteURI, documentBase);
                    eventLink = false;
                }
                catch (MalformedURLException ex) {
                    throw new SAXException(
                        "Invalid URL encountered: " + remoteURI,
                        ex);
                }
            }
            else {
                remoteURI = this.buildUrlString(remoteURI, !eventLink);
            }

            Attributes newAttributes =
                modifyLinkAttribute(attributeName, remoteURI, attributes);

            if (eventLink) {
                this.startEventLinkElement(
                    elementName,
                    attributeName,
                    newAttributes);
            }
            else {
                super.startElement(uri, elementName, raw, newAttributes);
            }
        }
    }

    /**
     * Replaces the link of given attribute whith the new uri.
     * @param attribute Name of the attribute containing the link
     * @param remoteURI The new uri
     * @param attributes List of attributes
     * @return The modified List of attributes
     */
    protected Attributes modifyLinkAttribute(
        String attribute,
        String remoteURI,
        Attributes attributes) {
        AttributesImpl newAttributes = new AttributesImpl(attributes);

        int index = newAttributes.getIndex(attribute);
        newAttributes.setValue(index, remoteURI);
        return newAttributes;
    }

    /**
     * Replaces the given element with an eventlink element adding the attribute and element attribute within 
     * the SAX stream.
     * @param element Original name of the element
     * @param attribute Name of the attribute containing the link
     * @param attributes Original list of attributes
     * @throws SAXException
     */
    protected void startEventLinkElement(
        String element,
        String attribute,
        Attributes attributes)
        throws SAXException {
        elementStack.push(element);
        AttributesImpl eventAttributes = null;
        if (attributes instanceof AttributesImpl) {
            eventAttributes = (AttributesImpl) attributes;
        }
        else {
            eventAttributes = new AttributesImpl(attributes);
        }

        eventAttributes.addAttribute(
            "",
            NewEventLinkTransformer.ATTRIBUTE_ATTR,
            NewEventLinkTransformer.ATTRIBUTE_ATTR,
            "CDATA",
            attribute);
        eventAttributes.addAttribute(
            "",
            NewEventLinkTransformer.ELEMENT_ATTR,
            NewEventLinkTransformer.ELEMENT_ATTR,
            "CDATA",
            element);
        super.startElement(
            NewEventLinkTransformer.NAMESPACE_URI,
            NewEventLinkTransformer.EVENT_ELEM,
            NAMESPACE_PREFIX + ":" + NewEventLinkTransformer.EVENT_ELEM,
            eventAttributes);
    }

    /**
     * Retrieves and stores any session token, appends proxy reader prefix and parameters if necessary
     * @param uri the url to be converted
     * @param applyPrefixAndPortalParams true signals that the url should be converted to call the proxy-reader
     * @return the converted uri
     * FIXME: anchors (#) should be treated right!
     */
    protected String buildUrlString(
        String uri,
        boolean applyPrefixAndPortalParams) {
        StringBuffer uriBuffer = new StringBuffer(uri.length());

        int index_semikolon = uri.indexOf(";");
        int index_question = uri.indexOf("?");

        if ((index_semikolon > -1)) {
            String sessionToken =
                uri.substring(
                    index_semikolon + 1,
                    (index_question == -1 ? uri.length() : index_question));
            copletInstanceData.getPersistentAspectData().put(
                ProxyTransformer.SESSIONTOKEN,
                sessionToken);
        }

        if (applyPrefixAndPortalParams) {
            uriBuffer.append(ProxyTransformer.PROXY_PREFIX);
        }

        uriBuffer.append(uri);

        if (applyPrefixAndPortalParams) {
            uriBuffer.append((index_question == -1 ? '?' : '&'));
            uriBuffer.append(this.copletIdParamString);
            uriBuffer.append('&');
            uriBuffer.append(this.portalNameParamString);
        }

        return uriBuffer.toString();
    }
}