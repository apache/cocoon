/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.webapps.session.context;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.transformation.CIncludeTransformer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A SessionContext which encapsulates the current Request object.
 *
 * It is not allowed to change this context.
 * The following paths are valid:
 * /parameter                  - lists all parameters, parameter names build the
 *                               elements with the value of the first parameter with
 *                               this name as text node childs
 * /parameter/<parameter_name> - one text node containing the value of the first
 *                               parameter with this name
 * /querystring                - the querystring with a leading '?' or null (the querystring is only for GET)
 *
 * /parametervalues            - same as /parameter but values are listed as described
 *                               below and each value of a parameter is listed.
 *                               <cinclude:parameters>
 *                                      <cinclude:parameter>
 *                                              <cinclude:name>parameter name</cinclude:name>
 *                                              <cinclude:value>parameter value</cinclude:value>
 *                                      </cinclude:parameter>
 *                                       ...
 *                                      <cinclude:parameter>
 *                                              <cinclude:name>parameter name</cinclude:name>
 *                                              <cinclude:value>parameter value</cinclude:value>
 *                                      </session:parameter>
 *                               </cinclude:parameters>
 *                               If a parameter has more than one value for each value a
 *                               <cinclude:parameter/> block is generated.
 *                               This output has the namespace of the CIncludeTransformer
 *                               to use it as input for a <cinclude:includexml> command.
 * /attributes - lists all attributes, attribute names build the elements
 *               with the values as childs
 * /headers    - lists all headers, header names build the elements
 *               with the values as text node childs
 * /cookies ----- <cookie name="...">
 *                   <comment/>
 *                   <domain/>
 *                   <maxAge/>
 *                   <name/>
 *                   <path/>
 *                   <secure/>
 *                   <value/>
 *                   <version/>
 * /characterEncoding
 * /contentLength
 * /contentType
 * /protocol
 * /remoteAddress
 * /remoteHost
 * /scheme
 * /serverName
 * /serverPort
 * /method
 * /contextPath
 * /pathInfo
 * /pathTranslated
 * /remoteUser
 * /requestedSessionId
 * /requestURI
 * /servletPath
 * /isRequestedSessionIdFromCookie
 * /isRequestedSessionIdFromCookie
 * /isRequestedSessionIdValid
 *
 *  The following attributes of the servlet api 2.2 are missing:
 *  - getUserPrincipal()
 *  - getLocale()
 *  - getLocales()
 *  - getAuthType()
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: RequestSessionContext.java,v 1.7 2004/03/19 14:16:55 cziegeler Exp $
*/
public final class RequestSessionContext
implements SessionContext {

    private static final String PARAMETERS_ELEMENT = "cinclude:" + CIncludeTransformer.CINCLUDE_PARAMETERS_ELEMENT;
    private static final String PARAMETER_ELEMENT  = "cinclude:" + CIncludeTransformer.CINCLUDE_PARAMETER_ELEMENT;
    private static final String NAME_ELEMENT       = "cinclude:" + CIncludeTransformer.CINCLUDE_NAME_ELEMENT;
    private static final String VALUE_ELEMENT      = "cinclude:" + CIncludeTransformer.CINCLUDE_VALUE_ELEMENT;

    /** Name of this context */
    private String    name;

    /** The current {@link org.apache.cocoon.environment.Request} */
    transient private Request          request;

    /** The content of this context */
    private Document  contextData;

    /** The XPath Processor */
    private XPathProcessor xpathProcessor;

    /**
     * Setup this context
     */
    public void setup(String value, String loadResource, String saveResource) {
        this.name = value;
    }

    /**
     * Set the Request
     */
    public void setup(Map objectModel, ServiceManager manager, XPathProcessor processor)
    throws ProcessingException {
        this.xpathProcessor = processor;
        this.request = ObjectModelHelper.getRequest(objectModel);

        contextData = DOMUtil.createDocument();
        contextData.appendChild(contextData.createElementNS(null, "context"));

        Element root = contextData.getDocumentElement();

        SAXParser parser = null;
        try {
            parser = (SAXParser) manager.lookup( SAXParser.ROLE );
            this.buildParameterXML(root, parser);
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup parser.", ce);
        } finally {
            manager.release(parser );
        }
        this.buildAttributesXML(root);
        this.buildMiscXML(root);
        this.buildCookiesXML(root);
        this.buildHeadersXML(root);
    }

    /**
     * Get the name of the context
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the request object
     */
    public Request getRequest() {
        return this.request;
    }
    
    /**
     * Build path
     */
    private String createPath(String path) {
        if (path == null) path = "/";
        if (path.startsWith("/") == false) path = "/" + path;
        path = "/context" + path;
        if (path.endsWith("/") == true) path = path.substring(0, path.length() - 1);
        return path;
    }

    private Node createTextNode(Document doc, String value) {
        return doc.createTextNode(value != null ? value : "");
    }

    /**
     * Build attributes XML
     */
    private void buildMiscXML(Element root) {
        Document doc = root.getOwnerDocument();

        Element node;

        node = doc.createElementNS(null, "characterEncoding");
        node.appendChild(this.createTextNode(doc, this.request.getCharacterEncoding()));
        root.appendChild(node);
        node = doc.createElementNS(null, "contentLength");
        node.appendChild(this.createTextNode(doc, "" + this.request.getContentLength()));
        root.appendChild(node);
        node = doc.createElementNS(null, "contentType");
        node.appendChild(this.createTextNode(doc, this.request.getContentType()));
        root.appendChild(node);
        node = doc.createElementNS(null, "protocol");
        node.appendChild(this.createTextNode(doc, this.request.getProtocol()));
        root.appendChild(node);
        node = doc.createElementNS(null, "remoteAddress");
        node.appendChild(this.createTextNode(doc, this.request.getRemoteAddr()));
        root.appendChild(node);
        node = doc.createElementNS(null, "remoteHost");
        node.appendChild(this.createTextNode(doc, this.request.getRemoteHost()));
        root.appendChild(node);
        node = doc.createElementNS(null, "scheme");
        node.appendChild(this.createTextNode(doc, this.request.getScheme()));
        root.appendChild(node);
        node = doc.createElementNS(null, "serverName");
        node.appendChild(this.createTextNode(doc, this.request.getServerName()));
        root.appendChild(node);
        node = doc.createElementNS(null, "serverPort");
        node.appendChild(this.createTextNode(doc, ""+this.request.getServerPort()));
        root.appendChild(node);
        node = doc.createElementNS(null, "method");
        node.appendChild(this.createTextNode(doc, this.request.getMethod()));
        root.appendChild(node);
        node = doc.createElementNS(null, "contextPath");
        node.appendChild(this.createTextNode(doc, this.request.getContextPath()));
        root.appendChild(node);
        node = doc.createElementNS(null, "pathInfo");
        node.appendChild(this.createTextNode(doc, this.request.getPathInfo()));
        root.appendChild(node);
        node = doc.createElementNS(null, "pathTranslated");
        node.appendChild(this.createTextNode(doc, this.request.getPathTranslated()));
        root.appendChild(node);
        node = doc.createElementNS(null, "remoteUser");
        node.appendChild(this.createTextNode(doc, this.request.getRemoteUser()));
        root.appendChild(node);
        node = doc.createElementNS(null, "requestedSessionId");
        node.appendChild(this.createTextNode(doc, this.request.getRequestedSessionId()));
        root.appendChild(node);
        node = doc.createElementNS(null, "requestURI");
        node.appendChild(this.createTextNode(doc, this.request.getRequestURI()));
        root.appendChild(node);
        node = doc.createElementNS(null, "servletPath");
        node.appendChild(this.createTextNode(doc, this.request.getServletPath()));
        root.appendChild(node);
        node = doc.createElementNS(null, "isRequestedSessionIdFromCookie");
        node.appendChild(doc.createTextNode(this.request.isRequestedSessionIdFromCookie() ? "true" : "false"));
        root.appendChild(node);
        node = doc.createElementNS(null, "isRequestedSessionIdFromURL");
        node.appendChild(doc.createTextNode(this.request.isRequestedSessionIdFromURL() ? "true" : "false"));
        root.appendChild(node);
        node = doc.createElementNS(null, "isRequestedSessionIdValid");
        node.appendChild(doc.createTextNode(this.request.isRequestedSessionIdValid() ? "true" : "false"));
        root.appendChild(node);
    }

    /**
     * Build attributes XML
     */
    private void buildAttributesXML(Element root)
    throws ProcessingException {
        Document doc = root.getOwnerDocument();
        Element attrElement = doc.createElementNS(null, "attributes");
        String attrName;
        Element attr;

        root.appendChild(attrElement);
        Enumeration all = this.request.getAttributeNames();
        while (all.hasMoreElements() == true) {
            attrName = (String) all.nextElement();
            attr = doc.createElementNS(null, attrName);
            attrElement.appendChild(attr);
            DOMUtil.valueOf(attr, this.request.getAttribute(attrName));
        }
    }

    /**
     * Build cookies XML
     */
    private void buildCookiesXML(Element root) {
        Document doc = root.getOwnerDocument();

        Element cookiesElement = doc.createElementNS(null, "cookies");
        root.appendChild(cookiesElement);

        Cookie[] cookies = this.request.getCookies();
        if (cookies != null) {
            Cookie current;
            Element node;
            Element parent;
            for(int i = 0; i < cookies.length; i++) {
                current = cookies[i];
                parent = doc.createElementNS(null, "cookie");
                parent.setAttributeNS(null, "name", current.getName());
                cookiesElement.appendChild(parent);
                node = doc.createElementNS(null, "comment");
                node.appendChild(this.createTextNode(doc, current.getComment()));
                parent.appendChild(node);
                node = doc.createElementNS(null, "domain");
                node.appendChild(this.createTextNode(doc, current.getDomain()));
                parent.appendChild(node);
                node = doc.createElementNS(null, "maxAge");
                node.appendChild(this.createTextNode(doc, ""+current.getMaxAge()));
                parent.appendChild(node);
                node = doc.createElementNS(null, "name");
                node.appendChild(this.createTextNode(doc, current.getName()));
                parent.appendChild(node);
                node = doc.createElementNS(null, "path");
                node.appendChild(this.createTextNode(doc, current.getPath()));
                parent.appendChild(node);
                node = doc.createElementNS(null, "secure");
                node.appendChild(doc.createTextNode(current.getSecure() ? "true" : "false"));
                parent.appendChild(node);
                node = doc.createElementNS(null, "value");
                node.appendChild(this.createTextNode(doc, current.getValue()));
                parent.appendChild(node);
                node = doc.createElementNS(null, "version");
                node.appendChild(this.createTextNode(doc, ""+current.getVersion()));
                parent.appendChild(node);
            }
        }
    }

    /**
     * Build headers XML
     */
    private void buildHeadersXML(Element root) {
        Document doc = root.getOwnerDocument();
        Element headersElement = doc.createElementNS(null, "headers");
        String headerName;
        Element header;

        root.appendChild(headersElement);
        Enumeration all = this.request.getHeaderNames();
        while (all.hasMoreElements() == true) {
            headerName = (String) all.nextElement();
            try {
                header = doc.createElementNS(null, headerName);
                headersElement.appendChild(header);
                header.appendChild(this.createTextNode(doc, this.request.getHeader(headerName)));
            } catch (Exception ignore) {
                // if the header name is not a valid element name, we simply ignore it
            }
        }
    }

    /**
     * Build parameter XML
     */
    private void buildParameterXML(Element root, SAXParser parser) {
        Document doc = root.getOwnerDocument();
        // include all parameters
        // process "/parameter" and "/parametervalues" at the same time
        Element     parameterElement = doc.createElementNS(null, "parameter");
        Element     parameterValuesElement = doc.createElementNS(null, "parametervalues");
        root.appendChild(parameterElement);
        root.appendChild(parameterValuesElement);
        String      parameterName = null;
        Enumeration pars = this.request.getParameterNames();
        Element     parameter;
        Element     element;
        Node        valueNode;
        String[]    values;
        String      parValue;

        element = doc.createElementNS(CIncludeTransformer.CINCLUDE_NAMESPACE_URI, PARAMETERS_ELEMENT);
        element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cinclude", CIncludeTransformer.CINCLUDE_NAMESPACE_URI);
        parameterValuesElement.appendChild(element);
        parameterValuesElement = element;

        while (pars.hasMoreElements() == true) {
            parameterName = (String)pars.nextElement();
            values = this.request.getParameterValues(parameterName);

            for(int i = 0; i < values.length; i++) {

                // this is a fast test, if the parameter value contains xml!
                parValue = values[i].trim();
                if (parValue.length() > 0 && parValue.charAt(0) == '<') {
                    try {
                        valueNode = DOMUtil.getDocumentFragment(parser, new StringReader(parValue));
                        valueNode = doc.importNode(valueNode, true);
                    } catch (Exception noXMLException) {
                        valueNode = doc.createTextNode(parValue);
                    }
                } else {
                    valueNode = doc.createTextNode(parValue);
                }
                // create "/parameter" entry for first value
                if (i == 0) {
                    try {
                        parameter = doc.createElementNS(null, parameterName);
                        parameter.appendChild(valueNode);
                        parameterElement.appendChild(parameter);
                    } catch (Exception local) {
                        // the exception is ignored and only this parameters is ignored
                    }
                }

                try {
                    // create "/parametervalues" entry
                    element = doc.createElementNS(CIncludeTransformer.CINCLUDE_NAMESPACE_URI, PARAMETER_ELEMENT);
                    parameterValuesElement.appendChild(element);
                    parameter = element;
                    element = doc.createElementNS(CIncludeTransformer.CINCLUDE_NAMESPACE_URI, NAME_ELEMENT);
                    parameter.appendChild(element);
                    element.appendChild(doc.createTextNode(parameterName));
                    element = doc.createElementNS(CIncludeTransformer.CINCLUDE_NAMESPACE_URI, VALUE_ELEMENT);
                    parameter.appendChild(element);
                    element.appendChild(valueNode.cloneNode(true));
                } catch (Exception local) {
                    // the exception is ignored and only this parameters is ignored
                }
            }
        }
        // and now the query string
        element = doc.createElementNS(null, "querystring");
        root.appendChild(element);
        String value = request.getQueryString();
        if (value != null) {
            element.appendChild(doc.createTextNode('?' + value));
        }
    }

    /**
     * Get the XML from the request object
     */
    public DocumentFragment getXML(String path)
    throws ProcessingException {
        if (path == null || path.charAt(0) != '/') {
            throw new ProcessingException("Not a valid XPath: " + path);
        }
        path = this.createPath(path);
        DocumentFragment result = null;
        NodeList list;

        try {
            list = DOMUtil.selectNodeList(this.contextData, path, this.xpathProcessor);
        } catch (javax.xml.transform.TransformerException localException) {
            throw new ProcessingException("Exception: " + localException, localException);
        }
        if (list != null && list.getLength() > 0) {
            result = DOMUtil.getOwnerDocument(contextData).createDocumentFragment();
            for(int i = 0; i < list.getLength(); i++) {

                // the found node is either an attribute or an element
                if (list.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
                    // if it is an attribute simple create a new text node with the value of the attribute
                    result.appendChild(DOMUtil.getOwnerDocument(contextData).createTextNode(list.item(i).getNodeValue()));
                } else {
                    // now we have an element
                    // copy all children of this element in the resulting tree
                    NodeList childs = list.item(i).getChildNodes();
                    if (childs != null) {
                        for(int m = 0; m < childs.getLength(); m++) {
                            result.appendChild(DOMUtil.getOwnerDocument(contextData).importNode(childs.item(m), true));
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Setting of xml is not possible for the request context
     */
    public void setXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        throw new ProcessingException("RequestSessionContext: Setting of xml not allowed");
    }

    /**
     * Setting of xml is not possible for the request context
     */
    public void setValueOfNode(String path, String value)
    throws ProcessingException {
        throw new ProcessingException("RequestSessionContext: Setting of xml not allowed");
    }

    /**
     * Append a document fragment is not possible for the request context.
     */
    public void appendXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        throw new ProcessingException("RequestSessionContext: Appending of xml not allowed");
    }

    /**
     * Removing is not possible for the request context.
     */
    public void removeXML(String path)
    throws ProcessingException {
        throw new ProcessingException("RequestSessionContext: Removing of xml not allowed");
    }

    /**
     * Set a context attribute. If value is null the attribute is removed.
     */
    public void setAttribute(String key, Object value)
    throws ProcessingException {
        if (value == null) {
            this.request.removeAttribute(key);
        } else {
            this.request.setAttribute(key, value);
        }
    }

    /**
     * Get a context attribute. If the attribute is not available return null
     */
    public Object getAttribute(String key)
    throws ProcessingException {
        return this.request.getAttribute(key);
    }

    /**
     * Get a context attribute. If the attribute is not available the defaultObject is returned
     */
    public Object getAttribute(String key, Object defaultObject)
    throws ProcessingException {
        Object obj = this.getAttribute(key);
        return (obj != null ? obj : defaultObject);
    }

    /**
     * Get a copy the first node specified by the path.
     */
    public Node getSingleNode(String path)
    throws ProcessingException {
        path = this.createPath(path);
        Node node = null;

        try {
            node = DOMUtil.getSingleNode(this.contextData, path, this.xpathProcessor);
        } catch (javax.xml.transform.TransformerException localException) {
            throw new ProcessingException("Exception: " + localException, localException);
        }
        return node;
    }

    /**
     * Get a copy all the nodes specified by the path.
     */
    public NodeList getNodeList(String path)
    throws ProcessingException {
        path = this.createPath(path);
        NodeList list = null;

        try {
            list = DOMUtil.selectNodeList(this.contextData, path, this.xpathProcessor);
        } catch (javax.xml.transform.TransformerException localException) {
            throw new ProcessingException("Exception: " + localException, localException);
        }
        return list;
    }

    /**
     * Set the value of a node. The node is copied before insertion.
     */
    public void setNode(String path, Node node)
    throws ProcessingException {
        throw new ProcessingException("RequestSessionContext: Setting of XML not allowed");
    }

    /**
     * Get the value of this node. This is similiar to the xsl:value-of
     * function. If the node does not exist, <code>null</code> is returned.
     */
    public String getValueOfNode(String path)
    throws ProcessingException {
        String value = null;
        Node node = this.getSingleNode(path);
        if (node != null) {
            value = DOMUtil.getValueOfNode(node);
        }

        return value;
    }

    /**
     * Stream the XML directly to the handler. This streams the contents of getXML()
     * to the given handler without creating a DocumentFragment containing a copy
     * of the data
     */
    public boolean streamXML(String path,
                             ContentHandler contentHandler,
                             LexicalHandler lexicalHandler)
    throws SAXException, ProcessingException {
        boolean result = false;
        NodeList list;

        try {
            list = DOMUtil.selectNodeList(this.contextData, this.createPath(path), this.xpathProcessor);
        } catch (javax.xml.transform.TransformerException local) {
            throw new ProcessingException("TransformerException: " + local, local);
        }
        if (list != null && list.getLength() > 0) {
            result = true;
            for(int i = 0; i < list.getLength(); i++) {

                // the found node is either an attribute or an element
                if (list.item(i).getNodeType() == Node.ATTRIBUTE_NODE) {
                    // if it is an attribute simple create a new text node with the value of the attribute
                    String value = list.item(i).getNodeValue();
                    contentHandler.characters(value.toCharArray(), 0, value.length());
                } else {
                    // now we have an element
                    // stream all children of this element to the resulting tree
                    NodeList childs = list.item(i).getChildNodes();
                    if (childs != null) {
                        for(int m = 0; m < childs.getLength(); m++) {
                            IncludeXMLConsumer.includeNode(childs.item(m), contentHandler, lexicalHandler);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Get the request parameter as xml
     */
    public DocumentFragment getParameterAsXML(final String parameterName)
    throws ProcessingException {
        return this.getXML("/parameter/"+parameterName);
    }

    /**
     * Get the request parameter as a String
     */
    public String getParameter(final String parameterName) {
        return this.request.getParameter(parameterName);
    }

    /**
     * Try to load XML into the context.
     * If the context does not provide the ability of loading,
     * an exception is thrown.
     */
    public void loadXML(String path,
                        SourceParameters parameters)
    throws SAXException, ProcessingException, IOException {
        throw new ProcessingException("The context " + this.name + " does not support loading.");
    }

    /**
     * Try to save XML from the context.
     * If the context does not provide the ability of saving,
     * an exception is thrown.
     */
    public void saveXML(String path,
                        SourceParameters parameters)
    throws SAXException, ProcessingException, IOException {
        throw new ProcessingException("The context " + this.name + " does not support saving.");
    }

}
