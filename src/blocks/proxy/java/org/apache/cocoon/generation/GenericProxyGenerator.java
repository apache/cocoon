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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.RequestForwardingHttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.excalibur.xml.sax.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This is a generic HTTP proxy, designed to handle any possible HTTP method, 
 * but with a particular bias towards WebDAV. As of now it's pretty unstable, but
 * still it might be a good proof of concept towards a pure HTTP(++) proxy.
 * 
 * TODO: doesn't handle authentication properly
 * TODO: doesn't handle (and doubt it'll ever will) HTTP/1.1 keep-alive
 * 
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @version $Id: GenericProxyGenerator.java,v 1.5 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class GenericProxyGenerator extends ServiceableGenerator {
 
    /** The real URL to forward requests to */
    HttpURL destination;
    /** The current request */
    HttpServletRequest request;
    /** The current response */
    HttpServletResponse  response;
    /** The current request */
    String path;
    SAXParser parser;

    /**
     * Compose and get a SAX parser for further use.
     * 
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.parser = (SAXParser)manager.lookup(SAXParser.ROLE);
    }

    /**
     * Dispose
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.parser );
            this.parser = null;
        }
        super.dispose();
    }
    
    /**
     * Setup this component by getting the (required) "url" parameter and the 
     * (optional) "path" parameter.  If path is not specified, the request URI will
     * be used and forwarded.
     * 
     * TODO: handle query string
     * 
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(
        SourceResolver resolver,
        Map objectModel,
        String src,
        Parameters par)
        throws ProcessingException, SAXException, IOException {
            String url = par.getParameter("url", null);
            request = (HttpServletRequest)objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT);
            response = (HttpServletResponse)objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);

            if (url == null) {
                throw new ProcessingException("Missing the \"url\" parameter");
            }
            path =  par.getParameter("path", null);
            if (path == null)
                path =  request.getRequestURI();
            destination = new HttpURL(url);
            
      }
      
      
    /**
     * Get the request data, pass them on to the forwarder and return the result.
     * 
     * TODO: much better header handling
     * TODO: handle non XML and bodyless responses (probably needs a smarter Serializer, 
     *            since some XML has to go through the pipeline anyway.
     * 
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
        throws IOException, SAXException, ProcessingException {
            RequestForwardingHttpMethod method = 
                new RequestForwardingHttpMethod(request, destination);
             
            // Build the forwarded connection    
            HttpConnection conn = new HttpConnection(destination.getHost(), destination.getPort());            
            HttpState state = new HttpState();
            state.setCredentials(null, destination.getHost(), 
                new UsernamePasswordCredentials(destination.getUser(), destination.getPassword()));
            method.setPath(path);
            
            // Execute the method
            method.execute(state, conn);
            
            // Send the output to the client: set the status code...
            response.setStatus(method.getStatusCode());
  
            // ... retrieve the headers from the origin server and pass them on 
            Header[] methodHeaders = method.getResponseHeaders();
            for (int i = 0; i < methodHeaders.length; i++) {
                // there is more than one DAV header
                if (methodHeaders[i].getName().equals("DAV")) {
                    response.addHeader(methodHeaders[i].getName(), methodHeaders[i].getValue());
                } else if (methodHeaders[i].getName().equals("Content-Length")) {
                    // drop the original Content-Length header. Don't ask me why but there 
                    // it's always one byte off
                } else {
                    response.setHeader(methodHeaders[i].getName(), methodHeaders[i].getValue());
                }    
            }
            
            // no HTTP keepalives here...
            response.setHeader("Connection", "close");

            // Parse the XML, if any
            if (method.getResponseHeader("Content-Type").getValue().startsWith("text/xml")) {      
                InputStream stream = method.getResponseBodyAsStream();              
                parser.parse(new InputSource(stream), this.contentHandler, this.lexicalHandler);
            } else {
                // Just send a dummy XML
                this.contentHandler.startDocument();
                this.contentHandler.startElement("", "no-xml-content", "no-xml-content", new AttributesImpl());
                this.contentHandler.endElement("", "no-xml-content", "no-xml-content");
                this.contentHandler.endDocument();
            }
            
            // again, no keepalive here.
            conn.close();
   
    }

}
