/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.sax.XMLConsumerImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The <code>LinkHandler</code> handles elements and performs link translation
 * querying the configured <code>LinkResolver</code>.
 * <br>
 * This object will monitor ant then translate special attributes like
 * <code>cocoon:translate</code> and <code>cocoon:partition</code> to translate
 * links specified in the source space to valid links in the target space.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-10 13:46:56 $
 */
public class LinkHandler extends XMLConsumerImpl {
    /** The current links resolver */
    private LinkResolver resolver=null;

    public LinkHandler(LinkResolver resolver, XMLConsumer cons) {
        super(cons,cons);
        this.resolver=null;
    }

    /**
     * Receive notification of the beginning of an element.
     * <br>
     * This method will translate the value of the attribute wich name is
     * specified in the <code>cocoon:translate</code> attribute.
     * This will also monitor the <code>cocoon:partition</code> attribute used
     * to force the partition in wich link translation should be done.
     * <br>
     * NOTE: (PF) Link translation is yet all to implement.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        // Instead of calling super, we have to perform the translation.
        super.startElement(uri,loc,raw,a);
    }
}
