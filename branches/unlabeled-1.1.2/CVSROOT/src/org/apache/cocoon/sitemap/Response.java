/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-11 13:14:50 $
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
