/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.jsp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A component for loading and running Servlets and JSPs.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: JSPEngine.java,v 1.4 2004/03/05 13:01:57 bdelacretaz Exp $
 */
public interface JSPEngine {

    public static final String ROLE = JSPEngine.class.getName();
    
    /**
     * Execute the Servlet/JSP and return the output.
     * Output of the JSPEngine <b>must</b> be in UTF8 encoding.
     * 
     * @exception IOException
     * @exception ServletException
     * @exception Exception
     */
    public byte[] executeJSP(String url,
                             HttpServletRequest request,
                             HttpServletResponse response,
                             ServletContext context)
        throws IOException, ServletException, Exception;
}
