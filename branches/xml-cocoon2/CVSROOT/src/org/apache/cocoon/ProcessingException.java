/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import org.apache.avalon.CascadingException;

/**
 * This Exception is thrown every time there is a problem in processing
 * a request.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-02-05 16:23:03 $
 */
public class ProcessingException extends CascadingException {

    /**
     * Construct a new <code>ProcessingException</code> instance.
     */
    public ProcessingException(String message) {
        super(message, null);
    }

    /**
     * Construct a new <code>ProcessingException</code> that references
     * a parent Exception.
     */
    public ProcessingException(String message, Throwable t) {
        super(message, t);
    }
}