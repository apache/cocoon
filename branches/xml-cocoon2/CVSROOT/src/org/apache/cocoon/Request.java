/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import java.util.Enumeration;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-02-27 01:25:35 $
 */
public interface Request {
    /**
     * Returns the URI as it should be matched by the <code>Sitemap</code>
     */
    public String getUri();

    /**
     * Returns the value of the specified parameter, or <code>null</code> if
     * the parameter does not exist.
     */
    public String getParameter(String name);

    /**
     * Returns the values of the specified parameter for the request as an
     * array of strings, or <code>null</code> if the parameter does not exist.
     */
    public String[] getParameterValues(String name);

    /**
     * Returns the parameter names for this request as an
     * <code>Enumeration</code> of <code>String</code> objects , or an empty
     * <code>Enumeration</code> if there are no parameters.
     */
    public Enumeration getParameterNames();

    /**
     * Gets the value of the requested header field of this request.
     */
    public String getHeader(String name);

    /**
     * Gets the value of the requested header field of this request as an
     * integer.
     */
    public int getIntHeader(String name);

    /**
     * Gets the value of the requested header field of this request as a
     * date.
     */
    public long getDateHeader(String name);

    /**
     * Returns the header names for this request as an
     * <code>Enumeration</code> of <code>String</code> objects , or an empty
     * <code>Enumeration</code> if there are no headers.
     */
    public Enumeration getHeaderNames();
}
