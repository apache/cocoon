/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.xml.dom;

import org.w3c.dom.Document;

/**
 * This interface identifies classes producing instances of DOM
 * <code>Document</code> objects.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-12-11 16:07:01 $
 */
public interface DOMFactory {
    /**
     * Create a new Document object.
     */
    Document newDocument();

    /**
     * Create a new Document object with a specified DOCTYPE.
     */
    Document newDocument(String name);

    /**
     * Create a new Document object with a specified DOCTYPE, public ID and
     * system ID.
     */
    Document newDocument(String name, String publicId, String systemId);
}
