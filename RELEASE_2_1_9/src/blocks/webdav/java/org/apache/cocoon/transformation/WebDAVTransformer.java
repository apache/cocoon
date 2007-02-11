/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.OutputKeys;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.cocoon.components.webdav.WebDAVEventFactory;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.xmlizer.XMLizer;
import org.apache.webdav.lib.methods.HttpRequestBodyMethodBase;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A general purpose, low level webdav transformer. Sends http requests defined in xml
 * directly to the server and returns the response to the processing stream.
 * 
 * For a more high level approach, use WebDAVSource (GET/PUT/PROPPATCH) and DASLTransformer (SEARCH).
 * 
 * 
 * 
 */
public class WebDAVTransformer extends AbstractSAXTransformer 
implements Disposable, CacheableProcessingComponent {
	
    // ---------------------------------------------------- Constants

    private static final String WEBDAV_SCHEME = "webdav://";
    private static final String HTTP_SCHEME= "http://";
    
    private static final String NS_URI = "http://cocoon.apache.org/webdav/1.0";
    private static final String NS_PREFIX = "webdav:";
    
    private static final String REQUEST_TAG = "request";
    private static final String METHOD_ATTR = "method";
    private static final String TARGET_ATTR = "target";
    
    private static final String HEADER_TAG = "header";
    private static final String NAME_ATTR = "name";
    private static final String VALUE_ATTR = "value";
    
    private static final String BODY_TAG = "body";
    
    private static final String RESPONSE_TAG = "response";
    private static final String STATUS_TAG = "status";
    private static final String CODE_ATTR = "code";
    private static final String MSG_ATTR = "msg";

    private static HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());

    // ---------------------------------------------------- Member variables

    private HttpState m_state = null;

    private String m_method = null;
	private String m_target = null;
    private Map m_headers = null;

    private WebDAVEventFactory m_eventfactory = null;

    private DocumentFragment m_requestdocument = null;

    private AggregatedValidity m_validity = null;

    // ---------------------------------------------------- Lifecycle

    public WebDAVTransformer() {
        super.defaultNamespaceURI = "DAV:";
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) 
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        m_state = new HttpState();
        
        if(null != par.getParameter("username", null)) {
        	m_state.setCredentials(null, null, new UsernamePasswordCredentials(
        			par.getParameter("username", ""),
        			par.getParameter("password", ""))
        			);
        }

        if(m_eventfactory == null) {
        	try {
        		m_eventfactory = (WebDAVEventFactory)manager.lookup(WebDAVEventFactory.ROLE);
        	} catch (ServiceException e) {
				// ignore, no eventcaching configured
        		m_eventfactory = null;
			}
        }
    }
    
    /**
     * Helper method to do event caching
     * 
     * @param methodurl The url to create the EventValidity for
     * @return an EventValidity object or null
     */
    private SourceValidity makeWebdavEventValidity(HttpURL methodurl) {
    	
    	if (m_eventfactory == null) {
    		return null;
    	}
    	
    	SourceValidity evalidity = null;
    	try {
    		
    		evalidity = new EventValidity(m_eventfactory.createEvent(methodurl));
    		
    		if(getLogger().isDebugEnabled())
    			getLogger().debug("Created eventValidity for webdav request: "+evalidity);
    	
    	} catch (Exception e) {
    		if(getLogger().isErrorEnabled())
    			getLogger().error("could not create EventValidity!",e);
    	}
    	return evalidity;
    }
    
    public void recycle() {
        super.recycle();

        m_method = null;
        m_target = null;
        m_validity = null;
        m_requestdocument = null;
    }
    
    public void dispose() {
        recycle();

        manager = null;
    }

    // ---------------------------------------------------- Transformer

    public void startElement(String uri, String name, String raw, Attributes atts) 
    throws SAXException {
        if (name.equals(REQUEST_TAG) && uri.equals(NS_URI)) {
            m_headers = new HashMap();
            if ((m_method = atts.getValue(METHOD_ATTR)) == null) {
                final String msg = "The <request> element must contain a \"method\" attribute";
                throw new IllegalStateException(msg);
            }
            if ((m_target = atts.getValue(TARGET_ATTR)) == null) {
                throw new IllegalStateException("The <request> element must contain a \"target\" attribute");
            }
            if (m_target.startsWith(WEBDAV_SCHEME)) {
                m_target = HTTP_SCHEME + m_target.substring(WEBDAV_SCHEME.length());
            }
            else {
                throw new SAXException("Illegal value for target, must be an http:// or webdav:// URL");
            }
        }
        else if (name.equals(HEADER_TAG) && uri.equals(NS_URI)) {
            final String hname = atts.getValue(NAME_ATTR);
            if (hname == null) {
                throw new SAXException("The <header> element requires a \"name\" attribute");
            }
            final String value = atts.getValue(VALUE_ATTR);
            if (value == null) {
                throw new SAXException("The <header> element requires a \"value\" attribute");
            }
            m_headers.put(hname, value);
        }
        else if (name.equals(BODY_TAG) && uri.equals(NS_URI)) {
            startRecording();
        }
        else {
            super.startElement(uri, name, raw, atts);
        }
	}
    
    public void endElement(String uri, String name, String raw) 
    throws SAXException {
        if (name.equals(REQUEST_TAG) && uri.equals(NS_URI)) {
            
            try {
	            HttpURL url = new HttpURL(m_target);
	            if(url.getUser() != null && !"".equals(url.getUser())) {
		            m_state.setCredentials(null, new UsernamePasswordCredentials(
		                    url.getUser(),
		                    url.getPassword()));
	            }
	            m_target = url.getURI();
	            
	            if (m_validity != null) {
	                m_validity.add(makeWebdavEventValidity(url));
	            }
	            
            } catch (Exception e) {
				//ignore
			}
            
            // create method
            WebDAVRequestMethod method = new WebDAVRequestMethod(m_target, m_method);
            
            try {
                // add request headers
                Iterator headers = m_headers.entrySet().iterator();
                while (headers.hasNext()) {
                    Map.Entry header = (Map.Entry) headers.next();
                    method.addRequestHeader((String) header.getKey(), (String) header.getValue());
                }

                Properties props = XMLUtils.createPropertiesForXML(false);
                props.put(OutputKeys.ENCODING, "ISO-8859-1");
                String body = XMLUtils.serializeNode(m_requestdocument, props);
                // set request body
                method.setRequestBody(body.getBytes("ISO-8859-1"));
                
                // execute the request
                executeRequest(method);
            } catch (ProcessingException e) {
				if(getLogger().isErrorEnabled()) {
					getLogger().debug("Couldn't read request from sax stream",e);
				}
				throw new SAXException("Couldn't read request from sax stream",e);
			} catch (UnsupportedEncodingException e) {
				if(getLogger().isErrorEnabled()) {
					getLogger().debug("ISO-8859-1 encoding not present",e);
				}
				throw new SAXException("ISO-8859-1 encoding not present",e);
			}
            finally {
                method.releaseConnection();
                m_headers = null;
            }
        }
        else if (name.equals(HEADER_TAG) && uri.equals(NS_URI)) {
            // dont do anything
        }
        else if (name.equals(BODY_TAG) && uri.equals(NS_URI)) {
        	m_requestdocument = super.endRecording();
        }
        else {
            super.endElement(uri, name, raw);
        }
    }
    
    private void executeRequest(WebDAVRequestMethod method) throws SAXException {
        try {
            client.executeMethod(method.getHostConfiguration(), method, m_state);
            
            super.contentHandler.startPrefixMapping("webdav", NS_URI);
            
            // start <response>
            AttributesImpl atts = new AttributesImpl();
            atts.addCDATAAttribute(TARGET_ATTR, m_target);
            atts.addCDATAAttribute(METHOD_ATTR, m_method);
            super.contentHandler.startElement(NS_URI, RESPONSE_TAG, NS_PREFIX + RESPONSE_TAG, atts);
            atts.clear();
            
            // <status>
            atts.addCDATAAttribute(CODE_ATTR, String.valueOf(method.getStatusCode()));
            atts.addCDATAAttribute(MSG_ATTR, method.getStatusText());
            super.contentHandler.startElement(NS_URI, STATUS_TAG, NS_PREFIX + STATUS_TAG, atts);
            atts.clear();
            super.contentHandler.endElement(NS_URI, STATUS_TAG, NS_PREFIX + STATUS_TAG);
            
            // <header>s
            Header[] headers = method.getResponseHeaders();
            for (int i = 0; i < headers.length; i++) {
                atts.addCDATAAttribute(NAME_ATTR, headers[i].getName());
                atts.addCDATAAttribute(VALUE_ATTR, headers[i].getValue());
                super.contentHandler.startElement(NS_URI, HEADER_TAG, NS_PREFIX + HEADER_TAG, atts);
                atts.clear();
                super.contentHandler.endElement(NS_URI, HEADER_TAG, NS_PREFIX + HEADER_TAG);
            }
            
            // response <body>
            final InputStream in = method.getResponseBodyAsStream();
            if (in != null) {
                String mimeType = null;
                Header header = method.getResponseHeader("Content-Type");
                if (header != null) {
                    mimeType = header.getValue();
                    int pos = mimeType.indexOf(';');
                    if (pos != -1) {
                        mimeType = mimeType.substring(0, pos);
                    }
                }
                if (mimeType != null && mimeType.equals("text/xml")) {
                    super.contentHandler.startElement(NS_URI, BODY_TAG, NS_PREFIX + BODY_TAG, atts);
                    IncludeXMLConsumer consumer = new IncludeXMLConsumer(super.contentHandler);
                    XMLizer xmlizer = null;
                    try {
                        xmlizer = (XMLizer) manager.lookup(XMLizer.ROLE);
                        xmlizer.toSAX(in, mimeType, m_target, consumer);
                    } catch (ServiceException ce) {
                        throw new SAXException("Missing service dependency: " + XMLizer.ROLE, ce);
                    } finally {
                        manager.release(xmlizer);
                    }
                    super.contentHandler.endElement(NS_URI, BODY_TAG, NS_PREFIX + BODY_TAG);
                }
            }
                
            // end <response>
            super.contentHandler.endElement(NS_URI, RESPONSE_TAG, NS_PREFIX + RESPONSE_TAG);
            
            super.contentHandler.endPrefixMapping(NS_URI);
        }
        catch (HttpException e) {
            throw new SAXException("Error executing WebDAV request." + " Server responded " 
                + e.getReasonCode() + " (" + e.getReason() + ") - " + e.getMessage(), e);
        }
        catch (IOException e) {
            throw new SAXException("Error executing WebDAV request", e);
        }
    }

    // ---------------------------------------------------- CacheableProcessingComponent

    public Serializable getKey() {
        if (m_state == null) {
            return "WebDAVTransformer";
        }
        final StringBuffer key = new StringBuffer();
        // get the credentials
        final Credentials credentials = m_state.getCredentials(null, null);
        if (credentials != null) {
            if (credentials instanceof UsernamePasswordCredentials) {
                key.append(((UsernamePasswordCredentials) credentials).getUserName());
            }
            else {
                key.append(credentials.toString());
            }
        }
        return key.toString();
    }

    public SourceValidity getValidity() {
    	
        // dont do any caching when no event caching is set up
    	if (m_eventfactory == null) {
    		return null;
    	}
    	
        if (m_validity == null) {
            m_validity = new AggregatedValidity();
        }
        return m_validity;
    }

    // ---------------------------------------------------- Implementation

    private static class WebDAVRequestMethod extends HttpRequestBodyMethodBase {

        private String m_name;

        private WebDAVRequestMethod(String uri, String name) {
            super(uri);
            m_name = name;
        }

		public String getName() {
			return m_name;
		}

    }
    
}
