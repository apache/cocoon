/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.io.OutputStream;

import org.apache.cocoon.xml.XMLConsumer;

import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;

import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-08-21 17:36:26 $
 */

public class HTMLSerializer extends AbstractSerializer implements XMLConsumer {

    private SerializerFactory factory;
    
    public HTMLSerializer() {
        this.factory = SerializerFactory.getSerializerFactory(Method.HTML);
    }
    
    public void setOutputStream(OutputStream out) {
        try {
            super.setOutputStream(out);
            this.setBridgedContentHandler(this.factory.makeSerializer(out, this.format).asContentHandler());
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }    
}
