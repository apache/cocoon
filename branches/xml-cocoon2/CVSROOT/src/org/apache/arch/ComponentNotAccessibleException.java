/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.arch;

/**
 * This exception is thrown by the <code>ComponentManager</code> when a
 * <code>Component</code> cannot be accessed.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:18:58 $
 */
public class ComponentNotAccessibleException extends RuntimeException {

    /** The nested exception. */
    private Exception exception=null;

    /**
     * Construct a new <code>ComponentNotAccessibleException</code> instance.
     */
    public ComponentNotAccessibleException(String message, Exception e) {
        super(message);
        this.exception=e;
    }

    /**
     * Return the nested <code>Exception</code>
     */
    public Exception getException() {
        return(this.exception);
    }
}