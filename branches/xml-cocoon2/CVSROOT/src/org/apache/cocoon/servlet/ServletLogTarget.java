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
import org.apache.log.LogKit;
import org.apache.log.Priority;
import org.apache.log.LogEntry;

/**
 * A Servlet Log Target.  It will forward any LogEntry to the context.
 */
public class ServletLogTarget extends DefaultOutputLogTarget {
    private ServletContext context = null;
    private Priority.Enum priority;

    public ServletLogTarget(ServletContext context, Priority.Enum minimum) {
        super();
        this.context = context;
        this.priority = minimum;
    }

    public ServletLogTarget(ServletContext context) {
        this(context, Priority.ERROR);
    }

    /**
     * Process a log entry, via formatting and outputting it.
     *
     * @param entry the log entry
     */
    public void processEntry( final LogEntry entry )
    {
        if (this.priority.isLowerOrEqual(entry.getPriority())) {
            super.processEntry(entry);
        }
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