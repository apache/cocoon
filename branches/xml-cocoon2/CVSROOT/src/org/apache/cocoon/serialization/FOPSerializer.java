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

import org.apache.avalon.Poolable;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Version;
import org.apache.fop.messaging.MessageListener;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.messaging.MessageEvent;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * FOP serializer.
 *
 * @author <a href="mailto:giacomo.pati@pwr.ch">Giacomo Pati</a>
 *         (PWR Organisation &amp; Entwicklung)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.13 $ $Date: 2001-02-22 17:10:42 $
 *
 */
public class FOPSerializer extends AbstractSerializer implements MessageListener, Poolable {

    /**
     * The FOP driver
     */
    private Driver driver = null;

    /**
     * Create the FOP driver
     * Set the <code>OutputStream</code> where the XML should be serialized.
     */
    public void setOutputStream(OutputStream out) {
        this.driver = new Driver();

        // the use of static resources sucks for servlet enviornments
        // since we could have multiple FOP that all logs in this pipe
        // It's a concurrency and security nightmare! (SM)
        MessageHandler.setOutputMethod(MessageHandler.EVENT);
        MessageHandler.addListener(this);

        this.driver.setRenderer("org.apache.fop.render.pdf.PDFRenderer", Version.getVersion());
        this.driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
        this.driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
        this.driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
        this.driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
        this.driver.setOutputStream(out);
        this.setContentHandler(this.driver.getContentHandler());
     }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument() throws SAXException {
        super.endDocument();
        try {
            driver.format();
            driver.render();
        } catch (IOException e) {
            getLogger().error("FOPSerializer.endDocument()", e);
            throw new SAXException (e);
        } catch (FOPException e) {
            getLogger().error("FOPSerializer.endDocument()", e);
            throw new SAXException (e);
        } finally {
            driver = null;
            MessageHandler.removeListener(this);
        }
    }

    /**
     * Return the MIME type.
     */
    public String getMimeType() {
        return "application/pdf";
    }

    /**
     * Receive FOP events.
     */
    public void processMessage (MessageEvent event) {
        // XXX (SM)
        // we should consume the events in some meaningful way
        // for example formatting them on the metapipeline
    }
}
