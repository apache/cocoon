/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:34 $
 */
public interface Job {
    public String getType();

    public String getUri();
    
    public String getParameter(String name);
    public Enumeration getParameterNames();
    
    public int getContentLength();
    public String getContentType();
    public InputStream getContent()
    throws IOException;
    
    public String getRequestHeader(String name);
    public Enumeration getRequestHeaderNames();

    public void setHeader(String name, String value);
    public void setContentType(String type);
}
