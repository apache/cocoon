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
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:14 $
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