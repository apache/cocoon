/*-- $Id: XSLTProcessor.java,v 1.12 2000-04-27 17:57:17 stefano Exp $ --

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
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.store.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.logger.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.transformer.*;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.apache.cocoon.Utils;
import org.apache.cocoon.Defaults;

/**
 * This class implements an XSLT processor.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.12 $ $Date: 2000-04-27 17:57:17 $
 */

public class XSLTProcessor implements Actor, Processor, Status, Defaults {

    private Monitor monitor = new Monitor(10);

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
        String path = (String) parameters.get("path");
        String browser = (String) parameters.get("browser");

        Hashtable params = new Hashtable();
        Enumeration enum = request.getParameterNames();
        if (enum != null) {
            while (enum.hasMoreElements()) {
                String name = (String) enum.nextElement();
                params.put(name, request.getParameter(name));
            }
        }

        try {
            Object resource = getResource(document, path, browser);
            Document stylesheet = getStylesheet(resource, request);
            Document result = this.parser.createEmptyDocument();
            return transformer.transform(document, null, stylesheet, resource.toString(), result, params);
        } catch (PINotFoundException e) {
            return document;
        }
    }

    private Object getResource(Document document, String path, String browser) throws ProcessorException {

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
                        if (url.indexOf("://") < 0) {
                            local = new File(path + url);
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
                    } else if (browser != null) {
                        if (media.equals(browser)) {
                            resource = local;
                            break;
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

        try {
            Object o = this.store.get(resource);
            if ((o != null) && (!this.hasChanged(request))) {
                return (Document) o;
            } else {
                Document sheet = getDocument(resource);
                this.store.hold(resource, sheet);
                this.monitor.watch(Utils.encode(request), resource);
                return sheet;
            }
        } catch (Exception e) {
            this.monitor.invalidate(request);
            throw new ProcessorException("Could not associate stylesheet to document: "
                + " error reading " + resource + ": " + e);
        }
    }

    private Document getDocument(Object resource) throws Exception {
        InputSource input = new InputSource();
        input.setSystemId(resource.toString());
        return this.parser.parse(input, false);  // do not validate stylesheets
    }

    public boolean hasChanged(Object context) {
        return this.monitor.hasChanged(Utils.encode((HttpServletRequest) context));
    }

    public String getStatus() {
        return "XSLT Processor";
    }
}