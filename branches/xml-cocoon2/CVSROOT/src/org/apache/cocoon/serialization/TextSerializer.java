/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.serialization;

import java.io.OutputStream;

import org.apache.xml.serialize.SerializerFactory;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;

import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-08-23 22:43:37 $
 */

public class TextSerializer extends AbstractSerializer {

    private SerializerFactory factory;
    
    public TextSerializer() {
        this.factory = SerializerFactory.getSerializerFactory(Method.TEXT);
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
