/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import org.apache.avalon.Configurable;
import org.apache.avalon.Loggable;

import org.apache.log.Logger;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-01-22 21:56:33 $
 */
public abstract class AbstractAction implements Action, Configurable, Loggable {

    protected Logger log;

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

}
