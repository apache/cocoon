/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.servlet;

import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.apache.cocoon.Request;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.1 $ $Date: 2000-02-27 01:25:39 $
 */
public class CocoonServletRequest implements Request {
    
    /** This request <code>HttpServletRequest</code> */
    private HttpServletRequest request=null;
    /** This reques <code>String</code>uri */
    private String uri=null;
    
    /**
     * Create a new instance of this <code>CocoonServletRequest</code>
     */
    protected CocoonServletRequest(HttpServletRequest req, String uri) {
        super();
        if ((request==null)||(uri==null))
            throw new IllegalArgumentException("Null parameter specified");
        this.request=req;
        this.uri=uri;
    }

    /**
     * Returns the URI as it should be matched by the <code>Sitemap</code>
     */
    public String getUri() {
        return(this.uri);
    }

    /**
     * Returns the value of the specified parameter, or <code>null</code> if
     * the parameter does not exist.
     */
    public String getParameter(String name) {
        return(this.request.getParameter(name));
    }

    /**
     * Returns the values of the specified parameter for the request as an
     * array of strings, or <code>null</code> if the parameter does not exist.
     */
    public String[] getParameterValues(String name) {
        return(this.request.getParameterValues(name));
    }

    /**
     * Returns the parameter names for this request as an
     * <code>Enumeration</code> of <code>String</code> objects , or an empty
     * <code>Enumeration</code> if there are no parameters.
     */
    public Enumeration getParameterNames() {
        return(this.request.getParameterNames());
    }

    /**
     * Gets the value of the requested header field of this request.
     */
    public String getHeader(String name) {
        return(this.request.getHeader(name));
    }

    /**
     * Gets the value of the requested header field of this request as an
     * integer.
     */
    public int getIntHeader(String name) {
        return(this.request.getIntHeader(name));
    }

    /**
     * Gets the value of the requested header field of this request as a
     * date.
     */
    public long getDateHeader(String name) {
        return(this.request.getDateHeader(name));
    }

    /**
     * Returns the header names for this request as an
     * <code>Enumeration</code> of <code>String</code> objects , or an empty
     * <code>Enumeration</code> if there are no headers.
     */
    public Enumeration getHeaderNames() {
        return(this.request.getHeaderNames());
    }
}
