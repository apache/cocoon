/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.producers;

import org.apache.cocoon.sitemap.Request;
import org.apache.cocoon.sitemap.Response;
import org.apache.cocoon.sax.XMLSource;
import org.apache.cocoon.framework.Component;
import org.apache.cocoon.framework.Configurable;

/**
 * The <code>Producer</code> interface is an abstraction for those compoments
 * whose task is to generate SAX events into the Cocoon pipeline.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.4.4 $ $Date: 2000-02-11 13:14:44 $
 * @since Cocoon 2.0
 */
public interface Producer extends Component, Configurable {
    /**
     * Get the <code>XMLSource</code> object that will produce SAX events.
     *
     * @param req The cocoon <code>Request</code>.
     * @param res The cocoon <code>Response</code>.
     */
    public XMLSource getXMLSource(Request req, Response res);
}
