/*-- $Id: FO2PDFFormatter.java,v 1.15 2001-03-26 00:30:15 greenrd Exp $ -- 

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
package org.apache.cocoon.formatter;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.apache.fop.apps.Driver;
import org.apache.cocoon.framework.*;
import org.apache.cocoon.parser.Parser;
import org.apache.xerces.parsers.SAXParser;
import javax.servlet.http.HttpServletResponse;

/**
 * This class wraps around FOP to perform XSL:FO to PDF formatting.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:greenrd@hotmail.com">Robin Green</a>
 * @version $Revision: 1.15 $ $Date: 2001-03-26 00:30:15 $
 */

public class FO2PDFFormatter extends AbstractFormatter implements Actor {


    /* 
     * If FOP version >= 0.14 we may need to reparse the result to enforce the namespace
     * compliance that FOP now requires but Xalan does not provide. (This only affects
     * FO documents that have been styled by a stylesheet but for simplicitly we apply
     * it to all incoming documents if the FOP version is >= 0.14.
     */
    private static final String FOP_VERSION = org.apache.fop.apps.Version.getVersion ();
    private static final Hashtable NO_PARAMETERS = new Hashtable ();
    private static final double UNKNOWN_VERSION = -1.0;
    private static double FOP_VERSION_NO;
    private static boolean STREAM_MODE;
    static {
        try {
           // This is a real mess! Why couldn't they just do a getVersionNumber() method!?
           int i = FOP_VERSION.indexOf (' ', 4);
           if (i == -1) i = FOP_VERSION.length ();
           String vn = FOP_VERSION.substring (4, i).replace ('_', '.');
           // only interested in first dot, if any
           i = vn.indexOf ('.');
           if (i != -1) i = vn.indexOf ('.', i + 1);
           if (i != -1) vn = vn.substring (0, i);
           FOP_VERSION_NO = Double.valueOf (vn).doubleValue ();
        }
        catch (Exception ex) {
           FOP_VERSION_NO = UNKNOWN_VERSION;
        }
        STREAM_MODE = FOP_VERSION_NO > 0.15 || FOP_VERSION_NO == UNKNOWN_VERSION;
        System.err.println ("FOP_VERSION = " + FOP_VERSION);
        System.err.println ("FOP_VERSION_NO = " + FOP_VERSION_NO);
    }

    protected Director director;
    protected Formatter xmlFormatter;
    protected Parser parser;

    public FO2PDFFormatter() {
        super.MIMEtype = "application/pdf";
        super.statusMessage = FOP_VERSION + " formatter (note: version number is often wildly inaccurate)";
    }
        
    public void init(Configurations conf) {
        super.init(conf);
    }

    public void init(Director director) {
        // other components might not have been setup yet, so defer this
        this.director = director;
    }

    protected void deferredInit () throws Exception {
        if (xmlFormatter == null) { // don't bother initting multiple times
            FormatterFactory formatters = (FormatterFactory) director.getActor ("formatters");
            xmlFormatter = formatters.getFormatterForType ("text/xml");
            parser = (Parser) director.getActor ("parser");
        }
    }

    public void format(Document document, OutputStream outStream, Dictionary parameters)
    throws Exception {

	    Driver driver = new Driver();
	    driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", FOP_VERSION);
	    driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
	    driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");

            if (STREAM_MODE) {

                // We use reflection here to avoid compile-time errors
                // This translates at runtime to
                //driver.setOutputStream (outStream);
                Driver.class.getMethod ("setOutputStream", new Class [] {OutputStream.class})
                    .invoke (driver, new Object [] {outStream});
            }
            else {
                PrintWriter pw = new PrintWriter (new OutputStreamWriter (outStream));

                // We use reflection here to avoid compile-time errors
                // This translates at runtime to
                //driver.setWriter (pw);
                Driver.class.getMethod ("setWriter", new Class [] {PrintWriter.class})
                    .invoke (driver, new Object [] {pw});
            }

	    if (FOP_VERSION_NO > 0.13 || FOP_VERSION_NO == UNKNOWN_VERSION) {
  	        driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
	        driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");

                // To workaround the problem that Xalan 1.x does not output DOM2-compliant namespaces,
                // we use a major hack - output xml as a byte array and read it in again. 
                // With a DOM2-compliant parser such as Xerces, this should work fine.
                deferredInit ();
                ByteArrayOutputStream tempStream = new ByteArrayOutputStream ();
                xmlFormatter.format (document, tempStream, NO_PARAMETERS);
                byte[] tempBytes = tempStream.toByteArray ();

                // For now, we just use Xerces - it would be more complicated to support
                // other parsers here.
                SAXParser parser = new SAXParser ();
                parser.setFeature("http://xml.org/sax/features/namespaces", true);
                driver.buildFOTree(parser, new InputSource (new ByteArrayInputStream (tempBytes)));
            }
            else {
	        driver.buildFOTree(document);
            }
	    driver.format();
	    driver.render();
    }
}
