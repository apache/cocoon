/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.cocoon.Job;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:42 $
 */
public class ServletJob implements Job {
    private HttpServletRequest request=null;
    private HttpServletResponse response=null;
    private String uri=null;
    
    public ServletJob(HttpServletRequest req, HttpServletResponse res) {
        this.request=req;
        this.response=res;
        this.uri=this.request.getPathInfo();
    }

    public String getType() {
        return("HTTP");
    }

    public String getUri() {
        return(this.uri);
    }

    public String getParameter(String name) {
        return(this.request.getParameter(name));
    }

    public Enumeration getParameterNames() {
        return(this.request.getParameterNames());
    }

    public int getContentLength() {
        return(this.request.getContentLength());
    }

    public String getContentType() {
        return(this.request.getContentType());
    }

    public InputStream getContent()
    throws IOException {
        return(this.request.getInputStream());
    }

    public String getRequestHeader(String name) {
        return(this.request.getHeader(name));
    }

    public Enumeration getRequestHeaderNames() {
        return(this.request.getHeaderNames());
    }

    public void setHeader(String name, String value) {
        this.response.setHeader(name,value);
    }

    public void setContentType(String type) {
        this.response.setContentType(type);
    }
}
