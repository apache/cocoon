/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.reading;

import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.environment.Environment;

/**
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-28 16:20:30 $
 */
public abstract class AbstractReader implements Reader {
    /** The current <code>Environment</code>. */
    protected Environment environment=null;
    /** The current <code>Parameters</code>. */
    protected Parameters parameters=null;
    /** The source URI associated with the request or <b>null</b>. */
    protected String source=null;

    /**
     * Set the <code>Environment</code> and sitemap
     * <code>Parameters</code> used to process the request.
     */
    public void setup(Environment environment, String src, Parameters par) {
        this.environment=environment;
        this.source=src;
        this.parameters=par;
    }
}
