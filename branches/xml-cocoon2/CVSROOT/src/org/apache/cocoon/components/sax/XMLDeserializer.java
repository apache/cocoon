/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.sax;

import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.SAXException;

/**
 * This interfaces identifies classes that deserialize XML data, sending SAX
 * events to the configured <code>XMLConsumer</code> (or SAX
 * <code>ContentHandler</code> and <code>LexicalHandler</code>).
 * <br>
 * The production of the xml data is started by passing an
 * the xml information to the <code>deserialize</code>
 * method.
 * It is beyond the scope of this interface to specify the format of
 * the serialized data.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-25 17:06:32 $
 */
public interface XMLDeserializer
extends XMLProducer {

    /**
     * Deserialize the xml data and stream it.
     *
     * @param saxFragment The xml data.
    */
    public void deserialize(Object saxFragment)
    throws SAXException;
}
