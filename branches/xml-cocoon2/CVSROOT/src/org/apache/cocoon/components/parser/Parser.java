/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.parser;

import java.io.IOException;
import org.apache.arch.Component;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:33:06 $
 */
public interface Parser extends Component, XMLProducer {
    
    public void parse(InputSource in)
    throws SAXException, IOException;
}
