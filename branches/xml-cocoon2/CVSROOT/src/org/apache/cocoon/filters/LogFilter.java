/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import org.apache.cocoon.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.xml.AbstractXMLProducer;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * The <code>LogFilter</code> is a class that can be plugged into a pipeline
 * to print the SAX events which passes thru this filter in a readable form 
 * to a file. 
 * <br>
 * The file will be specified in a parameter tag to the filter as follows:
 * <p>
 * <code>
 * &lt;filter name="log"&gt;<br>
 * &nbsp;&nbsp;&lt;parameter name="logfile" value="logfile.log"/&gt;<br>
 * &nbsp;&nbsp;&lt;parameter name="append" value="no"/&gt;<br>
 * &lt;/filter&gt;<br>
 * </code>
 * </p>
 *
 * Because the log file will be hardcoded into the sitemap this LOGFilter will 
 * not be thread save!!
 * <br>
 * This filters main purpose is debugging.
 * 
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation &amp; Entwicklung)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-06-06 12:54:05 $
 *
 */
public class LogFilter extends AbstractFilter {

    /** Wether we are forwarding XML data or not. */
    private boolean canReset=true;

    private String lf = System.getProperty("line.separator", "\n");
    
    /** true if filename is valid and writeable */
    private boolean isValid = true;
    /** filename for log file	*/
    private String logfilename = null;
    /** log file	*/
    private FileWriter logfile = null;
    /** should we append content to the log file */
    private boolean append = false;

    /** BEGIN SitemapComponent methods **/

    public void setup(Request request, Response response, 
                      String source, Parameters parameters) 
            throws ProcessingException, SAXException, IOException {
        if (logfile == null) {
            String appends = parameters.getParameter("append", null);
            logfilename = parameters.getParameter("logfile", null);
            if (appends != null && appends.equals ("yes")) {
                append = true;
            } else {
                append = false;
            }
            try {
                logfile = new FileWriter(logfilename, append );
            } catch (IOException e) {
                isValid = false;
                throw e;
            }
        }
        Date date = new Date();
        StringBuffer logEntry = new StringBuffer();
        logEntry.append ( "---------------------------- [" );
        logEntry.append ( date.toString() ); 
        logEntry.append ( "] ----------------------------" );
        log("setup", logEntry.toString());
    }

    /** END SitemapComponent methods **/

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator(Locator locator) {
        log("setDocumentLocator","");
        if (super.contentHandler!=null)
            super.contentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        log("startDocument", "");
        if (super.contentHandler!=null)
            super.contentHandler.startDocument();
        this.canReset=false;
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        log ("endDocument", "");
        if (super.contentHandler!=null)
            super.contentHandler.endDocument();
        this.canReset=true;
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        log ("startPrefixMapping", "prefix="+prefix+",uri="+uri);
        if (super.contentHandler!=null)
            super.contentHandler.startPrefixMapping(prefix,uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        log ("endPrefixMapping", "prefix="+prefix);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        log ("startElement", "uri="+uri+",local="+loc+",raw="+raw);
        for (int i = 0; i < a.getLength(); i++) {
            log ("            ", new Integer(i+1).toString()
                 +". uri="+a.getURI(i)
                 +",local="+a.getLocalName(i)
                 +",qname="+a.getQName(i)
                 +",type="+a.getType(i)
                 +",value="+a.getValue(i));
        }
        if (super.contentHandler!=null)
            super.contentHandler.startElement(uri,loc,raw,a);
    }


    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String qname)
    throws SAXException {
        log ("endElement", "uri="+uri+",local="+loc+",qname="+qname);
        if (super.contentHandler!=null)
            super.contentHandler.endElement(uri,loc,qname);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        log ("characters", new String(ch).substring(start, len));
        if (super.contentHandler!=null)
            super.contentHandler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        log ("ignorableWhitespace", new String(ch).substring(start, len));
        if (super.contentHandler!=null)
            super.contentHandler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        log ("processingInstruction", "target="+target+",data="+data);
        if (super.contentHandler!=null)
            super.contentHandler.processingInstruction(target,data);
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name)
    throws SAXException {
        log ("skippedEntity", "name="+name);
        if (super.contentHandler!=null)
            super.contentHandler.skippedEntity(name);
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        log ("startDTD", "name="+name+",publicId="+publicId+",systemId="+systemId);
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startDTD(name,publicId,systemId);
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        log ("endDTD", "");
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endDTD();
    }

    /**
     * Report the beginning of an entity.
     */
    public void startEntity(String name)
    throws SAXException {
        log ("startEntity", "name="+name);
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startEntity(name);
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name)
    throws SAXException {
        log ("endEntity", "name="+name);
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endEntity(name);
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        log ("startCDATA", "");
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startCDATA();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        log ("endCDATA", "");
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endCDATA();
    }

    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        log ("comment", new String(ch).substring(start, len));
        if (super.lexicalHandler!=null)
            super.lexicalHandler.comment(ch,start,len);
    }

    /**
     * Report to logfile.
     */
    private void log (String location, String description) {
        if (isValid) {    
            StringBuffer logEntry = new StringBuffer();
            logEntry.append ( "[" );
            logEntry.append ( location ); 
            logEntry.append ( "] " );
            logEntry.append ( description );
            logEntry.append ( lf );
            synchronized (logfile) {
                try {
                    logfile.write( logEntry.toString(), 0, logEntry.length());
                    logfile.flush();
                }
                catch(IOException ioe) { }
            }
        }
    }
    
    /**
     *  Attempt to close the log file when the class is GC'd
     */
    public void destroy() {
        try {
            logfile.close();
        } catch (Exception e) {}
    }
}
