/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.parser;

import java.io.IOException;
import org.apache.avalon.component.Component;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.cocoon.xml.dom.DOMFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2001-04-20 20:49:59 $
 */
public interface Parser extends Component, XMLProducer, DOMFactory {

    void parse(InputSource in) throws SAXException, IOException;
}
