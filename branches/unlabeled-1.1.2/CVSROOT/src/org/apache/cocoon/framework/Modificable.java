/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.framework;

/**
 * The <code>Modificable</code> interface is implemented by those objects whose
 * behaviour changes over time.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:38 $
 * @since Cocoon 2.0
 */
public interface Modificable {
    /**
     * Check if a modification occourred since a specified date.
     *
     * @parame date The number of milliseconds since &quot;the epoch&quot;
     *              (January 1, 1970 00:00:00 GMT), to check.
     * @return If the a modification occourred since the specified date, this
     *         method returns true, false otherwise.
     */
    public boolean modifiedSince(long date);
}
