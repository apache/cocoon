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

import org.apache.avalon.Poolable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-02-12 14:17:39 $
 */

public class HTMLSerializer extends AbstractTextSerializer implements Poolable {

    private SerializerFactory factory;

    public HTMLSerializer() {
        this.factory = SerializerFactory.getSerializerFactory(Method.HTML);
    }

    public void setOutputStream(OutputStream out) {
        try {
            super.setOutputStream(out);
            this.setContentHandler(this.factory.makeSerializer(out, this.format).asContentHandler());
        } catch (Exception e) {
            getLogger().error("HTMLSerializer.setOutputStream()", e);
            throw new RuntimeException(e.toString());
        }
    }
}
