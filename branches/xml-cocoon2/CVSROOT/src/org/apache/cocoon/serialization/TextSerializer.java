/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.io.OutputStream;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.OutputKeys;

import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.excalibur.pool.Poolable;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2001-04-20 20:50:12 $
 */

public class TextSerializer extends AbstractTextSerializer implements Poolable {

    private TransformerHandler handler;

    public TextSerializer() {
    }

    public void setOutputStream(OutputStream out) {
        try {
            super.setOutputStream(out);
            handler = factory.newTransformerHandler();
            format.put(OutputKeys.METHOD,"text");
            handler.setResult(new StreamResult(out));
            handler.getTransformer().setOutputProperties(format);
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
       } catch (Exception e) {
            getLogger().error("TextSerializer.setOutputStream()", e);
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Recyce the serializer. GC instance variables
     */
    public void recycle() {
        super.recycle();
        this.handler = null;
    }

}
