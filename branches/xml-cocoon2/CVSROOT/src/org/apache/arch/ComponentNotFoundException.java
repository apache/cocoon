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
 * <code>Component</code> cannot be found.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:33:02 $
 */
public class ComponentNotFoundException extends RuntimeException {

    /**
     * Construct a new <code>ComponentNotFoundException</code> instance.
     */
    public ComponentNotFoundException(String message) {
        super(message);
    }
}