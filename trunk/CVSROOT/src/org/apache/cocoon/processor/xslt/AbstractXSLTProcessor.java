/*-- $Id: AbstractXSLTProcessor.java,v 1.3 1999-11-09 02:30:50 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
import org.apache.cocoon.*;
import org.apache.cocoon.store.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

/**
 * This class abstracts the XSL processor interface.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.3 $ $Date: 1999-11-09 02:30:50 $
 */

public abstract class AbstractXSLTProcessor implements Actor, Processor, Status, Defaults {

    private Monitor monitor = new Monitor(10);
    
    protected Parser parser;
    protected Store store;
    
    public void init(Director director) {
        this.parser = (Parser) director.getActor("parser");
        this.store = (Store) director.getActor("store");
    }
    
    /**
     * Get the stylesheet associated with the given document, based 
     * on the environment and request parameters. This method
     * uses the object storage system to store preparsed stylesheets
     * in memory to be able to speed the transformation of those
     * files that changed the origin but left the stylesheet unchanged.
     */
    public Document getStylesheet(Document document, Dictionary parameters) throws ProcessorException {

        Object resource = null;
        Document sheet = null;
        
        HttpServletRequest request = (HttpServletRequest) parameters.get("request");

        try {
            Hashtable links = getStylesheetsForBrowsers(document, (String) parameters.get("path"));
            resource = links.get(parameters.get("browser"));

            if (resource == null) {
                resource = links.get(DEFAULT_BROWSER);
                if (resource == null) {
                    throw new PINotFoundException("No stylesheet is associated to the processed document.");
                }
            }

            if (this.hasChanged(request)) {
                sheet = getDocument(resource);
                this.store.hold(resource, sheet);
                this.monitor.watch(Utils.encode(request, true), resource);
            } else {
                Object o = this.store.get(resource);
                if (o != null) {
                    sheet = (Document) o;
                } else {
                    sheet  = getDocument(resource);
                    this.store.hold(resource, sheet);
                }
            }
            
            return sheet;
            
        } catch (MalformedURLException e) {
            throw new ProcessorException("Could not associate stylesheet to document: " 
                + resource + " is a malformed URL.");
        } catch (Exception e) {
            throw new ProcessorException("Could not associate stylesheet to document: " 
                + " error reading " + resource + ": " + e.getMessage());
        }
    }

    public boolean hasChanged(Object context) {
        return this.monitor.hasChanged(Utils.encode((HttpServletRequest) context, true));
    }

    private Document getDocument(Object resource) throws Exception {
        if (resource instanceof File) {
            return this.parser.parse(new FileReader((File) resource), null);
        } else if (resource instanceof URL) {
            return this.parser.parse(new InputStreamReader(((URL) resource).openStream()), null);
        } else {
            throw new ProcessorException("Could not handle resource: " + resource);
        }
    }
    
    private Hashtable getStylesheetsForBrowsers(Document document, String path) throws MalformedURLException {
        Hashtable links = new Hashtable();

        Enumeration pis = Utils.getAllPIs(document, STYLESHEET_PI).elements();
        while (pis.hasMoreElements()) {
            Hashtable attributes = Utils.getPIPseudoAttributes((ProcessingInstruction) pis.nextElement());
            
            String type = (String) attributes.get("type");
            if ((type != null) && (type.equals("text/xsl"))) {
                String url = (String) attributes.get("href");
                Object resource;
                if (url != null) {
                    if (url.indexOf("://") < 0) {
                        resource = new File(path + url);
                    } else {
                        resource = new URL(url);
                    }
                
                    String browser = (String) attributes.get("media");
                    if (browser != null) {
                        links.put(browser, resource);
                    } else {
                        links.put(DEFAULT_BROWSER, resource);
                    }
                }
            }
        }

        return links;
    }
}