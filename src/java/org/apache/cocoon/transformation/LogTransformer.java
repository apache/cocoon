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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @cocoon.sitemap.component.documentation
 * The <code>LogTransformer</code> is a class that can be plugged into a pipeline
 * to print the SAX events which passes thru this transformer in a readable form
 * to a file.
 * 
 * @cocoon.sitemap.component.name  log
 * @cocoon.sitemap.component.logger sitemap.transformer.log
 * 
 * @cocoon.sitemap.component.pooling.min   2
 * @cocoon.sitemap.component.pooling.max  16
 * @cocoon.sitemap.component.pooling.grow  2
 * 
 *
 * The <code>LogTransformer</code> is a class that can be plugged into a pipeline
 * to print the SAX events which passes thru this transformer in a readable form
 * to a file.
 * <br>
 * The file will be specified in a parameter tag in the sitemap pipeline to the
 * transformer as follows:
 * <p>
 * <pre>
 * &lt;map:transform type="log"&gt;
 * &nbsp;&nbsp;&lt;map:parameter name="logfile" value="logfile.log"/&gt;
 * &nbsp;&nbsp;&lt;map:parameter name="append" value="no"/&gt;
 * &lt;/map:transform&gt;
 * </pre>
 * </p>
 *
 * Because the log file will be hardcoded into the sitemap this LOGTransformer will
 * not be thread save!!
 * <br>
 * This transformations main purpose is debugging.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation &amp; Entwicklung)
 * @version CVS $Id: LogTransformer.java,v 1.5 2004/06/17 14:55:24 cziegeler Exp $
 *
 */
public class LogTransformer
  extends AbstractTransformer {

    private static String lf = System.getProperty("line.separator", "\n");

    /** log file */
    private FileWriter logfile;

    /**
     * Setup
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        final boolean append = parameters.getParameterAsBoolean("append", false);
        final String  logfilename = parameters.getParameter("logfile", null);

        // Check for null, use System.out if logfile is not specified.
        this.logfile = null;
        if ( null != logfilename ) {
            Source source = null;
            try {
                source = resolver.resolveURI( logfilename );
                final String systemId = source.getURI();
                if ( systemId.startsWith("file:") ) {
                    this.logfile = new FileWriter(systemId.substring(5), append );
                } else {
                    throw new ProcessingException("The logfile parameter must point to a file: " + logfilename);
                }
            } catch (SourceException se) {
                throw SourceUtil.handle(se);
            } finally {
                resolver.release( source );
            }
        }

        Date date = new Date();
        StringBuffer logEntry = new StringBuffer();
        logEntry.append ( "---------------------------- [" );
        logEntry.append ( date.toString() );
        logEntry.append ( "] ----------------------------" );
        this.log("setup", logEntry.toString());
    }

    /**
     * Recycle
     */
    public void recycle() {
        super.recycle();
        try {
            if (this.logfile != null) logfile.close();
        } catch (Exception e) {
            this.getLogger().warn("LogTransformer.recycle()", e);
        }
        this.logfile = null;
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator(Locator locator) {
        this.log("setDocumentLocator", locator != null ? "systemid="+locator.getSystemId()+",publicid="+locator.getPublicId() : "(locator is null)");
        if (super.contentHandler!=null) {
            super.contentHandler.setDocumentLocator(locator);
        }
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        this.log("startDocument", "");
        if (super.contentHandler!=null) {
            super.contentHandler.startDocument();
        }
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        this.log ("endDocument", "");
        if (super.contentHandler!=null) {
            super.contentHandler.endDocument();
        }
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        this.log ("startPrefixMapping", "prefix="+prefix+",uri="+uri);
        if (super.contentHandler!=null) {
            super.contentHandler.startPrefixMapping(prefix,uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        this.log ("endPrefixMapping", "prefix="+prefix);
        if (super.contentHandler!=null) {
            super.contentHandler.endPrefixMapping(prefix);
        }
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        this.log ("startElement", "uri="+uri+",local="+loc+",raw="+raw);
        for (int i = 0; i < a.getLength(); i++) {
            this.log ("            ", new Integer(i+1).toString()
                 +". uri="+a.getURI(i)
                 +",local="+a.getLocalName(i)
                 +",qname="+a.getQName(i)
                 +",type="+a.getType(i)
                 +",value="+a.getValue(i));
        }
        if (super.contentHandler!=null) {
            super.contentHandler.startElement(uri,loc,raw,a);
        }
    }


    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        this.log ("endElement", "uri="+uri+",local="+loc+",raw="+raw);
        if (super.contentHandler!=null) {
            super.contentHandler.endElement(uri,loc,raw);
        }
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        this.log ("characters", new String(ch,start,len));
        if (super.contentHandler!=null) {
            super.contentHandler.characters(ch,start,len);
        }
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        this.log ("ignorableWhitespace", new String(ch,start,len));
        if (super.contentHandler!=null) {
            super.contentHandler.ignorableWhitespace(ch,start,len);
        }
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        log ("processingInstruction", "target="+target+",data="+data);
        if (super.contentHandler!=null) {
            super.contentHandler.processingInstruction(target,data);
        }
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name)
    throws SAXException {
        this.log ("skippedEntity", "name="+name);
        if (super.contentHandler!=null) {
            super.contentHandler.skippedEntity(name);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        this.log ("startDTD", "name="+name+",publicId="+publicId+",systemId="+systemId);
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.startDTD(name,publicId,systemId);
        }
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        this.log ("endDTD", "");
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.endDTD();
        }
    }

    /**
     * Report the beginning of an entity.
     */
    public void startEntity(String name)
    throws SAXException {
        this.log ("startEntity", "name="+name);
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.startEntity(name);
        }
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name)
    throws SAXException {
        this.log ("endEntity", "name="+name);
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.endEntity(name);
        }
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        this.log ("startCDATA", "");
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.startCDATA();
        }
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        this.log ("endCDATA", "");
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.endCDATA();
        }
    }

    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        this.log ("comment", new String(ch,start,len));
        if (super.lexicalHandler!=null) {
            super.lexicalHandler.comment(ch,start,len);
        }
    }

    /**
     * Report to logfile.
     */
    private void log (String location, String description) {
        final StringBuffer logEntry = new StringBuffer();
        logEntry.append ( "[" );
        logEntry.append ( location );
        logEntry.append ( "] " );
        logEntry.append ( description );
        logEntry.append ( lf );
        final String text = logEntry.toString();
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info( text );
        }
        try {
            if ( null != this.logfile ) {
                this.logfile.write( text, 0, text.length());
                this.logfile.flush();
            } else {
                System.out.println( text );
            }
        }
        catch(IOException ioe) {
            this.getLogger().debug("LogTransformer.log", ioe);
        }
    }

    /**
     *  Attempt to close the log file when the class is GC'd
     */
    public void destroy() {
        try {
            if (this.logfile != null) logfile.close();
        } catch (Exception e) {getLogger().debug("LogTransformer.destroy()", e);}
    }
}
