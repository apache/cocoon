/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.acting;

import org.apache.avalon.Disposable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.AbstractLoggable;

import org.apache.log.Logger;

/**
 * AbstractAction gives you the infrastructure for easily deploying more
 * Actions.  In order to get at the Logger, use getLogger().
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-04-13 16:02:17 $
 */
public abstract class AbstractAction extends AbstractLoggable
implements Action, Configurable, Disposable {

    /**
     * Configures the Action.  This implementation currently does nothing.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        // Purposely empty so that we don't need to implement it in every
        // class.
    }

    /**
     *  dispose
     */
    public void dispose() {
        // Purposely empty so that we don't need to implement it in every
        // class.
    }
}
