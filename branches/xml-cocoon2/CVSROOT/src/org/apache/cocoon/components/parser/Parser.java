/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.parser;

import java.io.IOException;
import org.apache.avalon.Component;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.xml.dom.DOMFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-09-05 17:27:50 $
 */
public interface Parser extends Component, XMLProducer, DOMFactory {
    
    public void parse(InputSource in) throws SAXException, IOException;
    
}
