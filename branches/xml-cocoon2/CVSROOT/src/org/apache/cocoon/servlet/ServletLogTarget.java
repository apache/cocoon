/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.servlet;

import javax.servlet.ServletContext;
import org.apache.log.output.DefaultOutputLogTarget;

/**
 * A Servlet Log Target.  It will forward any LogEntry to the context.
 */
public class ServletLogTarget extends DefaultOutputLogTarget {
    private ServletContext context = null;

    public ServletLogTarget(ServletContext context) {
        this.context = context;
    }

    /**
     * Concrete implementation of output that writes out to underlying writer.
     *
     * @param data the data to output
     */
    protected void output( final String data ) {
        this.context.log( data );
    }
}