/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:41 $
 */
public class XercesSerializerFactory implements SerializerFactory {
    private Configurations conf=null;

    public Serializer getSerializer() {
        return(new XercesSerializer(conf));
    }

    public void configure(Configurations conf) {
        this.conf=conf;
    }
}
