/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.producers;

import org.apache.cocoon.framework.AbstractFactory;
import org.apache.cocoon.framework.Configurations;
import org.apache.cocoon.framework.ConfigurationException;

/**
 * The <code>DefaultProducerFactory</code> is a standard implementation of
 * a <code>Producer</code> factory.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-09 01:11:13 $
 * @since Cocoon 2.0
 */
public class DefaultProducerFactory extends AbstractFactory
                                    implements ProducerFactory {
    /**
     * Instantiate a new <code>Producer</code> whose name class is derived from
     * the configurations passed to the <code>AbstractFactory</code>.
     */
    public Producer getProducer()
    throws ConfigurationException {
        return((Producer)this.createInstance());
    }

    /**
     * Return the class name of the objects that need to be instantiated by
     * the <code>AbstractFactory</code>.
     *
     * @return Always <code>org.apache.cocoon.producers.Producer</code>
     */
    public String getAssignableClassName() {
        return("org.apache.cocoon.producers.Producer");
    }
}
