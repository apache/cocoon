/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-07-22 20:41:29 $
 */
public interface Response {
    /**
     * Adds a field to the response header with the given name and value.
     */
    public void setHeader(String name, String value);

    /**
     * Adds a field to the response header with the given name and int value.
     */
    public void setIntHeader(String name, int value);

    /**
     * Adds a field to the response header with the given name and date-valued
     * field.
     */
    public void setDateHeader(java.lang.String name, long date);

    /**
     * Sets the content type for this response.
     */
    public void setContentType(String type);
}
