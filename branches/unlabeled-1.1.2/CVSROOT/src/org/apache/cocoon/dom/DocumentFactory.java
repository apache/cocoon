/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.dom;

import org.apache.cocoon.framework.Configurable;
import org.w3c.dom.Document;

/**
 * The <code>DocumentFactory</code> object produces DOM Document objects.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:36 $
 */
public interface DocumentFactory extends Configurable {
    /** 
     * Create a new Document object.
     */
    public Document newDocument();

    /** 
     * Create a new Document object with a specified DOCTYPE.
     */
    public Document newDocument(String name);
}
