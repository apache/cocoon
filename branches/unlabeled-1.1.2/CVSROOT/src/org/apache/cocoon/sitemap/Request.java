/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-11 13:14:50 $
 */
public interface Request {
    /**
     * Get the requested URI in the target URI-space.
     */
    public String getPathInfo();

    /**
     * Get the requested URI in the source URI-space.
     */
    public String getPathTranslated();
    
    /**
     * Set the requested URI in the source URI-space.
     *
     * @exception IllegalStateException If this method was already called.
     * @exception NullPointerException If the specified uri was null.
     */
    public void setPathTranslated(String uri)
    throws IllegalStateException, NullPointerException;

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
     * Returns the header names for this request as an
     * <code>Enumeration</code> of <code>String</code> objects , or an empty
     * <code>Enumeration</code> if there are no headers.
     */
    public Enumeration getHeaderNames();
    
    /**
     * Returns the name of the user making this request, or null if not known.
     */
    public String getRemoteUser();

    /**
     * Returns the scheme of the URL used in this request, for example
     * &quot;http&quot;, &quot;https&quot;, or &quot;file&quot;.
     */
    public String getScheme();
}
