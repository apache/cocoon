/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
 * @version CVS $Id: LogTransformer.java,v 1.5 2003/12/06 21:22:07 cziegeler Exp $
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
