/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.xml;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This interface must be implemented by classes willing
 * to provide an XML representation of their current state.
 * <br/>
 * This class replaces the Cocoon1 <code>XObject</code> class
 * by using the SAX2 <code>ContentHandler</code> and exists in both
 * Cocoon1 and Cocoon2 to ensure compatibility.
 *
 * @author <a href="mailto:sylvain.wallez@anyware-tech.com">Sylvain Wallez</a>
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a> for the original XObject class
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-01-03 15:38:20 $
 */

public interface XMLFragment {
    /**
     * Generates SAX events representing the object's state
     * for the given content handler.
     */
    public void toSAX(ContentHandler handler) throws SAXException;

    /**
     * Appends children representing the object's state to the given node.
     */
    public void toDOM(Node node) throws Exception;
}
