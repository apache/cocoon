/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.framework.AbstractComponent;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.framework.Configurations;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-02-12 00:31:40 $
 * @since Cocoon 2.0
 */
public abstract class AbstractSerializer extends AbstractComponent
implements Serializer {
    /** This <code>AbstractSerializer</code> configuration parameters. */
    protected Configurations configurations=null;

    /**
     * Configure this <code>XMLSerializer</code>.
     * <br>
     * By default this method only store configurations.
     */
    public void configure(Configurations conf)
    throws ConfigurationException {
        this.configurations=conf;
    }
    
    /**
     * Check wether this <code>XMLSerializer</code> was modified since a
     * specified date.
     * <br>
     * By default this method returns <code>false</code> meaning that this
     * serializer was not modified.
     */
    public boolean modifiedSince(long date) {
        return(false);
    }
}
