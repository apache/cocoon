/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.serializers;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.framework.Configurations;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-11 13:15:30 $
 * @since Cocoon 2.0
 */
public abstract class AbstractSerializer implements Serializer {
    /** This <code>AbstractSerializer</code> configuration parameters. */
    protected Configurations configurations=null;
    /** This <code>AbstractSerializer</code> instance of <code>Cocoon</code>.*/
    protected Cocoon cocoon=null;

    /**
     * Configure this <code>XMLSerializer</code>.
     * <br>
     * By default this method only store configurations.
     */
    public void configure(Configurations conf) {
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

    /**
     * Set the <code>Cocoon</code> instance used by this
     * <code>XMLSerializer</code>.
     * <br>
     * By default this method only stores the <code>Cocoon</code> instance.
     */
    public void setCocoonInstance(Cocoon cocoon) {
        this.cocoon=cocoon;
    }
}
