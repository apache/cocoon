/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.io.OutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.excalibur.pool.Poolable;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.21 $ $Date: 2001-04-25 17:08:30 $
 */

public class XMLSerializer extends AbstractTextSerializer implements Poolable {

    private TransformerHandler handler;

    public XMLSerializer() {
    }

    public void setOutputStream(OutputStream out) {
        try {
            super.setOutputStream(out);
            this.handler = getTransformerFactory().newTransformerHandler();
            format.put(OutputKeys.METHOD,"xml");
            handler.setResult(new StreamResult(out));
            handler.getTransformer().setOutputProperties(format);
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
        } catch (Exception e) {
            getLogger().error("XMLSerializer.setOutputStream()", e);
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Recycle the serializer. GC instance variables
     */
    public void recycle() {
        super.recycle();
        this.handler = null;
    }
}
