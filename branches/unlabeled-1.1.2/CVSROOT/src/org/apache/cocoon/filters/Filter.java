/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.filters;

import org.apache.cocoon.sitemap.Request;
import org.apache.cocoon.sitemap.Response;
import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.framework.Component;
import org.apache.cocoon.framework.Configurable;
import org.apache.cocoon.framework.Modificable;

/**
 * The <code>Filter</code> interface is an abstraction for those compoments
 * whose task is to filter and modify SAX events into the Cocoon pipeline.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-02-11 13:14:42 $
 * @since Cocoon 2.0
 */
public interface Filter extends Component, Configurable, Modificable {
    /**
     * Get the <code>XMLConsumer</code> object that will listen to SAX events,
     * modify them, and then forward them to the specified 
     * <code>XMLConsumer</code>.
     *
     * @param req The cocoon <code>Request</code>.
     * @param res The cocoon <code>Response</code>.
     * @param cons The <code>XMLConsumer</code> listening to the modified
     *             SAX events.
     */
    public XMLConsumer getXMLConsumer(Request req, Response res, XMLConsumer cons);
}
