/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sax;

import java.io.IOException;
import org.xml.sax.SAXException;

/**
 * The <code>XMLProducer</code> interface abstract the idea of XML data
 * production through SAX events.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-09 08:33:57 $
 * @since Cocoon 2.0
 */
public interface XMLProducer {
    /**
     * Generate XML data notifying the specified <code>XMLConsumer</code>.
     */
    public void produce(XMLConsumer cons)
    throws IOException, SAXException;
}
