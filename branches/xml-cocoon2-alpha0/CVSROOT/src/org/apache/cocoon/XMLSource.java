/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import org.apache.cocoon.framework.Modificable;
import org.xml.sax.SAXException;

/**
 * The <code>XMLSource</code> interface extends <code>XMLProducer</code> adding
 * information about source modificability over time.
 * <br>
 * NOTE: (PF) This interface is required since a producer doesn't have any idea
 * of where a source XML file (or similar) resides until it's not requested to
 * produce XML data out of it, since this information resides in the actual
 * target uri to source uri translation done by the sitemap.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-09 01:51:26 $
 */
public interface XMLSource extends XMLProducer, Modificable {
}
