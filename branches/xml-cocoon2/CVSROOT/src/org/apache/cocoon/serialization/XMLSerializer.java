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

import org.apache.avalon.util.pool.Pool;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.cocoon.PoolClient;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2001-02-19 15:58:10 $
 */

public class XMLSerializer extends AbstractTextSerializer implements PoolClient {

    private SerializerFactory factory;

    private Pool pool;

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public void returnToPool() {
        this.pool.put(this);
    }

    public XMLSerializer() {
        this.factory = SerializerFactory.getSerializerFactory(Method.XML);
    }

    public void setOutputStream(OutputStream out) {
        try {
            super.setOutputStream(out);
            this.setContentHandler(this.factory.makeSerializer(out, this.format).asContentHandler());
        } catch (Exception e) {
            getLogger().error("XMLSerializer.setOutputStream()", e);
            throw new RuntimeException(e.toString());
        }
    }
}
