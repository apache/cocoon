/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * This interfaces identifies classes that produce XML data, sending SAX
 * events to the configured <code>XMLConsumer</code> (or SAX 
 * <code>ContentHandler</code> and <code>LexicalHandler</code>).
 * <br>
 * It's beyond the scope of this interface to specify a way in wich the XML
 * data production is started.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:33:11 $
 */
public interface XMLProducer {

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer);

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     */
    public void setContentHandler(ContentHandler content);

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     */
    public void setLexicalHandler(LexicalHandler lexical);
}
