/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.utils.Parameters;
import org.apache.cocoon.xml.util.DocumentHandlerWrapper; 

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Version;

import org.xml.sax.DocumentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation &amp; Entwicklung)
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-08-04 21:12:01 $
 *
 */
public class FO2PDFSerializer extends DocumentHandlerWrapper 
        implements Serializer {

    /**
      * The FOP driver 
      */
    private Driver driver = null;

    /**
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        driver = new Driver();
        driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", Version.getVersion());
        driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
        driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
        this.setDocumentHandler (driver.getDocumentHandler());
        driver.setWriter(new PrintWriter(out));
    }
 
    /** 
     * Receive notification of the end of a document. 
     */ 
    public void endDocument()  
            throws SAXException { 
        super.endDocument();
        try { 
            driver.format(); 
            driver.render(); 
        } catch (IOException e) { 
            throw new SAXException (e); 
        } catch (FOPException e) { 
            throw new SAXException (e); 
        } 
    } 
}
