/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

/**
 * This Exception is thrown every time a component detects an exception
 * due to a connection reset by peer.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-05-09 18:33:18 $
 */
public class ConnectionResetException extends ProcessingException {

    /**
     * Construct a new <code>ConnectionResetException</code> instance.
     */
    public ConnectionResetException(String message) {
        super(message, null);
    }

    /**
     * Construct a new <code>ConnectionResetException</code> that references
     * a parent Exception.
     */
    public ConnectionResetException(String message, Throwable t) {
        super(message, t);
    }
}