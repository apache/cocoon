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

import org.apache.cocoon.Constants;
import org.apache.excalibur.pool.Poolable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.apache.cocoon.xml.xlink.ExtendedXLinkPipe;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-04-20 20:50:11 $
 */

public class LinkSerializer extends ExtendedXLinkPipe implements Serializer, Poolable {

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
        return Constants.LINK_CONTENT_TYPE;
    }

    public void simpleLink(String href, String role, String arcrole, String title, String show, String actuate, String uri, String name, String raw, Attributes attr)
    throws SAXException {
        if (traversable(href)) out.println(href);
    }

    public void startLocator(String href, String role, String title, String label, String uri, String name, String raw, Attributes attr)
    throws SAXException {
        if (traversable(href)) out.println(href);
    }

    private boolean traversable(String href) {
        if (href.charAt(0) == '#') return false;
        if (href.indexOf("://") != -1) return false;
        return true;
    }
}
