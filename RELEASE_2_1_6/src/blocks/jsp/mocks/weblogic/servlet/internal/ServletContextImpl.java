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
 * @version CVS $Id: ServletContextImpl.java,v 1.4 2004/03/05 13:01:57 bdelacretaz Exp $
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
