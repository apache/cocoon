/*-- $Id: XSLTProcessor.java,v 1.23 2000-12-01 23:14:21 greenrd Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

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

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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

package org.apache.cocoon.processor.xslt;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.cocoon.store.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.logger.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.processor.xsp.XSPProcessor;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.transformer.*;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Utils;
import org.apache.cocoon.Defaults;

/**
 * This class implements an XSLT processor.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.23 $ $Date: 2000-12-01 23:14:21 $
 */

public class XSLTProcessor implements Actor, Processor, Status, Defaults, Cacheable {

    /* This could probably be more efficient, but it's the easiest way. */
    private Monitor requestMonitor = new Monitor(10), sheetMonitor = new Monitor(10);

    private Parser parser;
    private Store store;
    private Transformer transformer;
    private Logger logger;

    public void init(Director director) {
        this.parser = (Parser) director.getActor("parser");
        this.store = (Store) director.getActor("store");
        this.transformer = (Transformer) director.getActor("transformer");
        this.logger = (Logger) director.getActor("logger");
    }

    public Document process(Document document, Dictionary parameters) throws Exception {

        Document sheet = (Document) parameters.get("stylesheet");
        if (sheet != null) return sheet;

        HttpServletRequest request = (HttpServletRequest) parameters.get("request");
        HttpServletResponse response = (HttpServletResponse) parameters.get ("response");
        ServletContext context = (ServletContext) parameters.get("context");
        String path = (String) parameters.get("path");
        String browser = (String) parameters.get("browser");
        Hashtable params = this.filterParameters(request);
        params.put ("ENVIRONMENT", Cocoon.version());
        params.put ("XSP-VERSION", XSPProcessor.version());

        try {
            Object resource = getResource(context, request, response, document, path, browser);
            Document stylesheet = getStylesheet(resource, request);
            Document result = this.parser.createEmptyDocument();
            return transformer.transform(document, path, stylesheet, 
              (resource == null) ? null : resource.toString(), result, params);
        } catch (PINotFoundException e) {
            return document;
        }
    }

    private Hashtable filterParameters(HttpServletRequest request) {
        Hashtable params = new Hashtable();
        Enumeration parameters = request.getParameterNames();

        if (parameters != null) {
            while (parameters.hasMoreElements()) {
                String name = (String) parameters.nextElement();
                if (isValidName (name))
                    params.put (name, request.getParameter (name));
            }
        }

        Cookie[] cookies = request.getCookies ();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies [i];
                String name = cookie.getName ();
                if (isValidName (name))
                    params.put ("C_" + name, cookie.getValue ());
            }
        }
        
        Enumeration headers = request.getHeaderNames ();
        while (headers.hasMoreElements ()) {
            String name = (String) headers.nextElement ();
            if (isValidName (name))
                params.put ("R_" + name, request.getHeader (name));
        }

        return params;
    }

    private boolean isValidName (String name) {
				StringCharacterIterator iter = new StringCharacterIterator(name);
				boolean valid_name = true;
				char c = iter.first();
				
				if (!(Character.isLetter(c) || c == '_')) {
					valid_name = false;
				} else {
					c = iter.next();
				}
				
				while (valid_name && c != iter.DONE) {
					if (!(Character.isLetterOrDigit(c) ||
						c == '-' ||
						c == '_' ||
						c == '.')) {
						valid_name = false;
					} else {
						c = iter.next();
					}
				}

                                return valid_name;				
    }
    
    private Object getResource(ServletContext context, HttpServletRequest request, 
     HttpServletResponse response, Document document, String path, String browser)
     throws ProcessorException {

        Object resource = null;

        Enumeration pis = Utils.getAllPIs(document, STYLESHEET_PI).elements();
        while (pis.hasMoreElements()) {
            Hashtable attributes = Utils.getPIPseudoAttributes((ProcessingInstruction) pis.nextElement());

            String type = (String) attributes.get("type");
            if ((type != null) && (type.equals("text/xsl"))) {
                String url = (String) attributes.get("href");
                if (url != null) {
                    Object local = null;

                    try {
                        if (url.charAt(0) == '#') { // does not support useragent selection with fragments
                            return null;
                        } else if (url.charAt(0) == '/') {
                            local = new File(Utils.getRootpath(request, context) + url);
                        } else if (url.indexOf("://") < 0) {
                            local = new File(Utils.getBasepath(request, context) + url);
                        } else {
                            local = new URL(url);
                        }
                    } catch (MalformedURLException e) {
                        throw new ProcessorException("Could not associate stylesheet to document: "
                            + url + " is a malformed URL.");
                    }

                    String media = (String) attributes.get("media");

                    if (media == null) {
                        resource = local;
                        if (browser == null) break;
                    } else {
                        // set HTTP Vary header to notify proxies
                        if (!response.containsHeader ("Vary")) {
                           response.setHeader ("Vary", "User-Agent");
                        }

                        if (browser != null) {
                            if (media.equals(browser)) {
                               resource = local;
                               break;
                            }
                        }
                    }
                }
            }
        }

        if (resource == null) {
            throw new ProcessorException("Could not associate stylesheet to document: "
                + " no matching stylesheet for: " + browser);
        } else {
            return resource;
        }
    }

    private Document getStylesheet(Object resource, HttpServletRequest request) throws ProcessorException {

        if (resource == null) return null;

        try {
            Object o = this.store.get(resource);
            // need to use sheetMonitor instead of requestMonitor because stylesheet pi
            // might be dynamically generated (as in FAQ examples).
            if ((o != null) && (!sheetMonitor.hasChanged(resource))) {
                return (Document) o;
            } else {
                String encReq = Utils.encode (request);
                // resource URI might have changed so invalidate previous
                requestMonitor.invalidate(encReq);
                Document sheet = getDocument(resource);
                this.store.hold(resource, sheet);
                requestMonitor.watch(encReq, resource);
                sheetMonitor.watch(resource, resource);
                return sheet;
            }
        } catch (Exception e) {
            throw new ProcessorException("Could not associate stylesheet to document: "
                + " error reading " + resource + ": " + e);
        }
    }

    private Document getDocument(Object resource) throws Exception {
        return this.parser.parse(new InputSource(resource.toString()), false);  // do not validate stylesheets
    }

    public boolean hasChanged(Object context) {
        return requestMonitor.hasChanged(Utils.encode((HttpServletRequest) context));
    }

    public boolean isCacheable(HttpServletRequest request) {
        return true;
    }

    public String getStatus() {
        return "XSLT Processor";
    }
}
