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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.OptionsMethod;
import org.apache.webdav.lib.methods.SearchMethod;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This transformer performs DASL queries on DASL-enabled WebDAV servers.  
 * It expects a "query" element in the  "http://cocoon.apache.org/webdav/dasl/1.0"
 * namespace containing the DASL query to execute, with a "target" attribute specifiyng
 * the webdav:// or http:// URL for the target WebDAV server. It will then replace
 * it  with a "query-result" element containing the WebDAV results. 
 * 
 * Each result will be contained in a "result"  element with a "path" attribute pointing at it 
 * and all WebDAV properties presented as (namespaced) children elements. 
 * 
 * Sample invocation:
 * lt;dasl:query xmlns:dasl="http://cocoon.apache.org/webdav/dasl/1.0"
 *   target="webdav://localhost/repos/"gt;
 * lt;D:searchrequest xmlns:D="DAV:"gt;
 *   lt;D:basicsearchgt;
 *     lt;D:selectgt;
 *       lt;D:allprop/gt;
 *     lt;/D:selectgt;
 *     lt;D:fromgt;
 *       lt;D:scopegt;
 *         lt;D:hrefgt;/repos/lt;/D:hrefgt;
 *         lt;D:depthgt;infinitylt;/D:depthgt;
 *       lt;/D:scopegt;
 *     lt;/D:fromgt;
 *     lt;D:wheregt;
 *       lt;D:eqgt;
 *         lt;D:propgt;lt;D:getcontenttype/gt;lt;/D:propgt;
 *         lt;D:literalgt;text/htmllt;/D:literalgt;
 *       lt;/D:eqgt;
 *     lt;/D:wheregt;
 *     lt;D:orderbygt;
 *       lt;D:ordergt;
 *         lt;D:propgt;
 *           lt;D:getcontentlength/gt;
 *         lt;/D:propgt;
 *         lt;D:ascending/gt;
 *       lt;/D:ordergt;
 *       lt;D:ordergt;
 *         lt;D:propgt;
 *           lt;D:href/gt;
 *         lt;/D:propgt;
 *         lt;D:ascending/gt;
 *       lt;/D:ordergt;
 *     lt;/D:orderbygt;
 *   lt;/D:basicsearchgt;
 * lt;/D:searchrequestgt;
 * lt;/dasl:querygt;
 * 
 * Features
 * - Substitution of a value: with this feature it's possible to pass value from sitemap
 * that are substituted into a query. 
 * sitemap example:
 *        lt;map:transformer type="dasl"gt;
 *          lt;parameter name="repos" value="/repos/"gt;
 *        lt;/map:transformergt;
 * query example:
 *        ....
 *        lt;D:hrefgt;lt;substitute-value name="repos"/gt;lt;/D:hrefgt;
 *        ....
 * This feature is like substitute-value of SQLTransformer
 * 
 * TODO: the SWCL Search method doesn't preserve the result order, which makes
 * order-by clauses useless.
 *
 * TODO: *much* better error handling.
 *
 * @author <a href="mailto: gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:d.madama@pro-netics.com>Daniele Madama</a>
 */
public class DASLTransformer extends AbstractSAXTransformer {

    /** The prefix for tag */
    static final String PREFIX = "dasl";
    /** The tag name identifying the query */
    static final String QUERY_TAG = "query";
    /** The tag namespace */
    static final String DASL_QUERY_NS =
        "http://cocoon.apache.org/webdav/dasl/1.0";
    /** The URL namespace */
    static final String TARGET_URL = "target";
    /** The WebDAV scheme*/
    static final String WEBDAV_SCHEME = "webdav://";
    /** The tag name of root_tag for result */
    static final String RESULT_ROOT_TAG = "query-result";
    /** The tag name for substitution of query parameter */
    static final String SUBSTITUTE_TAG = "substitute-value";	
    /** The tag name for substitution of query parameter */
    static final String SUBSTITUTE_TAG_NAME_ATTRIBUTE = "name";

    protected static final String PATH_NODE_NAME = "path";
    protected static final String RESOURCE_NODE_NAME = "resource";

    /** The target HTTP URL */
    String targetUrl;

    /**
     *  Intercept the <dasl:query> start tag.
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(
        String uri,
        String name,
        String raw,
        Attributes attr)
        throws SAXException {
        if (name.equals(QUERY_TAG) && uri.equals(DASL_QUERY_NS)) {
            this.startRecording();
            if ((targetUrl = attr.getValue(TARGET_URL)) == null) {
                throw new IllegalStateException("The query element must contain a \"target\" attribute");
            }
            //Sanityze target
            if (targetUrl.startsWith(WEBDAV_SCHEME))
                targetUrl =
                    "http://" + targetUrl.substring(WEBDAV_SCHEME.length());
            if (!targetUrl.startsWith("http"))
                throw new SAXException("Illegal value for target, must be an http:// or webdav:// URL");
        } else if (name.equals(SUBSTITUTE_TAG) && uri.equals(DASL_QUERY_NS)) {
            String parName = attr.getValue( DASL_QUERY_NS, SUBSTITUTE_TAG_NAME_ATTRIBUTE );
            if ( parName == null ) {
                throw new IllegalStateException( "Substitute value elements must have a " +
                                           SUBSTITUTE_TAG_NAME_ATTRIBUTE + " attribute" );
            }
            String substitute = this.parameters.getParameter( parName, null );
            if (getLogger().isDebugEnabled()) {
                getLogger().debug( "SUBSTITUTE VALUE " + substitute );
            }
            super.characters(substitute.toCharArray(), 0, substitute.length());
        } else {
            super.startElement(uri, name, raw, attr);
        }
    }

    /**
     * Intercept the <dasl:query> end tag, convert buffered input to a String, build and execute the 
     * DASL query.
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String name, String raw)
        throws SAXException {
        String query;
        if (name.equals(QUERY_TAG) && uri.equals(DASL_QUERY_NS)) {
            DocumentFragment frag = this.endRecording();
            try {
                Properties props = XMLUtils.createPropertiesForXML(false);
                props.put(OutputKeys.ENCODING, "ISO-8859-1");
                query = XMLUtils.serializeNode(frag, props);
                // Perform the DASL query
                this.performSearchMethod(query);
            } catch (ProcessingException e) {
                throw new SAXException("Unable to fetch the query data:", e);
            }
        } else if (name.equals(SUBSTITUTE_TAG) && uri.equals(DASL_QUERY_NS)) {
            //Do nothing!!!!
        } else {
            super.endElement(uri, name, raw);
        }
    }

    protected void performSearchMethod(String query) throws SAXException {
        try {
            DOMStreamer propertyStreamer = new DOMStreamer(this.xmlConsumer);
            OptionsMethod optionsMethod = new OptionsMethod(this.targetUrl);
            SearchMethod searchMethod = new SearchMethod(this.targetUrl, query);
            HttpURL url = new HttpURL(this.targetUrl);
            HttpState state = new HttpState();
            state.setCredentials(null, new UsernamePasswordCredentials(
                    url.getUser(),
                    url.getPassword()));                       
            HttpConnection conn = new HttpConnection(url.getHost(), url.getPort());
            WebdavResource resource = new WebdavResource(new HttpURL(this.targetUrl));
            if(!resource.exists()) {
                throw new SAXException("The WebDAV resource don't exist");
            }
            optionsMethod.execute(state, conn);
            if(!optionsMethod.isAllowed("SEARCH")) {
                throw new SAXException("The server don't support the SEARCH method");
            }
            searchMethod.execute(state, conn);
            Enumeration enum = searchMethod.getAllResponseURLs();
            this.contentHandler.startElement(
                DASL_QUERY_NS,
                RESULT_ROOT_TAG,
                PREFIX + ":" + RESULT_ROOT_TAG,
                new AttributesImpl());
            while (enum.hasMoreElements()) {
                String path = (String) enum.nextElement();
                Enumeration properties = searchMethod.getResponseProperties(path);
                AttributesImpl attr = new AttributesImpl();
                attr.addAttribute(DASL_QUERY_NS, PATH_NODE_NAME, PREFIX + ":" + PATH_NODE_NAME, "CDATA",path);

                this.contentHandler.startElement(DASL_QUERY_NS,
                    RESOURCE_NODE_NAME,
                    PREFIX + ":" + RESOURCE_NODE_NAME,
                    attr);
                while(properties.hasMoreElements()) {
                    BaseProperty metadata = (BaseProperty) properties.nextElement();
                    Element propertyElement = metadata.getElement();
                    propertyStreamer.stream(propertyElement);
                }
                    
                this.contentHandler.endElement(DASL_QUERY_NS,
                    RESOURCE_NODE_NAME,
                    PREFIX + ":" + RESOURCE_NODE_NAME);
            }
            this.contentHandler.endElement(
                DASL_QUERY_NS,
                RESULT_ROOT_TAG,
                PREFIX + ":" + RESULT_ROOT_TAG);
        } catch (SAXException e) {
            throw new SAXException("Unable to fetch the query data:", e);
        } catch (HttpException e1) {
            this.getLogger().error("Unable to contact Webdav server", e1);
            throw new SAXException("Unable to connect with server: ", e1);
        } catch (IOException e2) {
            throw new SAXException("Unable to connect with server: ", e2);
        } catch (NullPointerException e) {
            throw new SAXException("Unable to fetch the query data:", e);
        } catch (Exception e) {
            throw new SAXException("Generic Error:", e);
    		}
    }

}

