/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import org.apache.cocoon.Cocoon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.apache.cocoon.xml.xlink.XLinkPipe;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-09-27 16:15:49 $
 */

public class XLinkSerializer extends XLinkPipe implements Serializer {

    private PrintStream out;
    
    /**
     * Set the <code>OutputStream</code> where the requested resource should 
     * be serialized.
     */
    public void setOutputStream(OutputStream out) throws IOException {
        this.out = new PrintStream(out);
    }

    /**
     * Get the mime-type of the output of this <code>Component</code>.
     */
    public String getMimeType() {
        return Cocoon.LINK_CONTENT_TYPE;
    }
    
    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate, String uri, String name, String raw, Attributes attr) 
    throws SAXException {
        encode(href, role, out);
    }

    public void startLocator(String href, String role, String title, String label, String uri, String name, String raw, Attributes attr)
    throws SAXException {
        encode(href, role, out);
    }
    
    private void encode(String href, String role, PrintStream out) {
        if ((role == null) || role.equals(Cocoon.LINK_CRAWLING_ROLE)) {
            out.print('+');
        } else {
            out.print('-');
        }
        out.print(" ");
        out.println(href);
    }
}
