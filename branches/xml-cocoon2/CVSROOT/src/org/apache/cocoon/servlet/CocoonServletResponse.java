/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.cocoon.Response;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.4.2 $ $Date: 2000-02-27 01:33:08 $
 */
public class CocoonServletResponse implements Response {

    /** This response <code>HttpServletResponse</code> */
    private HttpServletResponse response=null;
    
    /**
     * Create a new instance of this <code>CocoonServletResponse</code>
     */
    protected CocoonServletResponse(HttpServletResponse res) {
        super();
        this.response=res;
    }

    /**
     * Adds a field to the response header with the given name and value.
     */
    public void setHeader(String name, String value) {
        this.response.setHeader(name,value);
    }

    /**
     * Adds a field to the response header with the given name and
     * integer value.
     */
    public void setIntHeader(String name, int value) {
        this.response.setIntHeader(name,value);
    }

    /**
     * Adds a field to the response header with the given name and
     * date-valued field.
     */
    public void setDateHeader(String name, long date) {
        this.response.setDateHeader(name,date);
    }

    /**
     * Sets the content type for this response.
     */
    public void setContentType(String type) {
        this.response.setContentType(type);
    }
}
