/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.cocoon.sitemap.Request;
import org.apache.cocoon.sitemap.Response;
import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.framework.Component;
import org.apache.cocoon.framework.Configurable;
import org.apache.cocoon.framework.Modificable;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.4.3 $ $Date: 2000-02-11 13:14:45 $
 */
public interface Serializer extends Component, Configurable, Modificable {
    public XMLConsumer getXMLConsumer(Request req, Response res, OutputStream out)
    throws IOException;
}
