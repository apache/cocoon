/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package weblogic.servlet.internal;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

/**
 * **********************************************************************
 * *                            W A R N I N G                           *
 * **********************************************************************
 *
 *  This is a mock object of the class, not the actual class.
 *  It's used to compile the code in absence of the actual class.
 *
 *  This class is created by hand, not automatically.
 *
 * **********************************************************************
 * 
 * @version CVS $Id: ServletContextImpl.java,v 1.3 2004/03/01 03:50:59 antonio Exp $
 */
 
public class ServletContextImpl implements ServletContext{

    /** @deprecated The method ServletContextImpl.getServlets() overrides a
     *              deprecated method from ServletContext */
    public Enumeration getServlets(){
        return null;
    }

    public void log(String string){
    }

    public void setExpectedLog(String string){
    }

    public void setupGetResource(URL resource){
    }

    public URL getResource(String string){
        return null;
    }
    
    public void setupGetResourcePaths(Set resourcePaths){
    }

    public Set getResourcePaths(String string){
        return null;
    }

    public ServletContext getContext(String string){
        return null;
    }

    public int getMinorVersion(){
        return -1;
    }

    public void removeAttribute(String string){
    }
    
    public void log(String string, Throwable t){
    }

    public void setExpectedLogThrowable(Throwable throwable){
    }

    public void addRealPath(String realPath){
    }

    public String getRealPath(String string){
        return "";
    }

    /** @deprecated The method ServletContextImpl.getServletNames() overrides
     *              a deprecated method from ServletContext */
    public Enumeration getServletNames(){
        return null;
    }

    /** @deprecated The method ServletContextImpl.getServlet(String) overrides
     *              a deprecated method from ServletContext */
    public Servlet getServlet(String string){
        return null;
    }

    /** @deprecated The method ServletContextImpl.log(Exception, String)
     *              overrides a deprecated method from ServletContext */
    public void log(Exception exception, String string){
    }

    public String getServerInfo(){
        return null;
    }

    public void setExpectedRequestDispatcherURI(String uri){
    }

    public void setupGetRequestDispatcher(RequestDispatcher requestDispatcher){
    }

    public RequestDispatcher getRequestDispatcher(String uri){
        return null;
    }
    
    public int getMajorVersion(){
        return -1;
    }

    public Set getResourcePaths(){
        return null;
    }

    public void setAttribute(String string, Object object){
    }

    public String getMimeType(String string){
        return null;
    }

    public RequestDispatcher getNamedDispatcher(String string){
        return null;
    }

    public String getInitParameter(String paramName){
        return null;
    }

    public void setInitParameter(String paramName, String paramValue){
    }

    public Object getAttribute(String string){
        return null;
    }

    public Enumeration getAttributeNames(){
        return null;
    }

    public String getServletContextName() {
        return null;
    }
    
    public InputStream getResourceAsStream(String string){
        return null;
    }
    
    public Enumeration getInitParameterNames(){
        return null;
    }
}
