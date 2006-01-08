/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cocoon.blocks.util.ServletContextWrapper;

/**
* @version $Id$
*/
public class BlockContext extends ServletContextWrapper {

    private Hashtable attributes;
    private BlockWiring wiring;
    private Blocks blocks;
    public BlockContext(ServletContext parentContext, BlockWiring wiring, Blocks blocks)
    throws ServletException, MalformedURLException {
    	super(parentContext);
        this.wiring = wiring;
        this.blocks = blocks;
    }

    /**
     * Returns the servlet container attribute with the given name, or null if there is no attribute by that name.
     * An attribute allows a servlet container to give the servlet additional information not already
     * provided by this interface. See your server documentation for information
     * about its attributes. A list of supported attributes can be retrieved using getAttributeNames.
     **/
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Map getAttributes() {
        return this.attributes;
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public Enumeration getAttributeNames() {
        return this.attributes.keys();
    }

    /**
     * Returns a URL to the resource that is mapped to a specified path. The path must begin
     * with a "/" and is interpreted as relative to the current context root.
     * <p>
     * This method allows the servlet container to make a resource available to servlets from any source.
     * Resources can be located on a local or remote file system, in a database, or in a .war file.
     * <p>
     * The servlet container must implement the URL handlers and URLConnection objects that are necessary to access the resource.
     * <p>
     * This method returns null if no resource is mapped to the pathname.
     *
     * Some containers may allow writing to the URL returned by this method using the methods of the URL class.
     *
     * The resource content is returned directly, so be aware that requesting a .jsp page returns the JSP source code. Use a
     * RequestDispatcher instead to include results of an execution.
     *
     * This method has a different purpose than java.lang.Class.getResource, which looks up resources based on a class loader. This
     * method does not use class loaders.
     **/
    public URL getResource(String path) throws MalformedURLException {
        // A path starting with '/' should be resolved relative to the context and
        // the '/' need to be removed to work with the URI resolver.
        while (path.length() >= 1 && path.charAt(0) == '/') {
            path = path.substring(1);
        }
        String location = this.wiring.getLocation();
        if (location.length() > 0 && location.charAt(0) != '/') {
        	location = "/" + location;
        }
        return super.servletContext.getResource(location +  path);
    }

    /**
     * Returns a String containing the real path for a given virtual path. For example, the virtual path "/index.html" has a real path of
     * whatever file on the server's filesystem would be served by a request for "/index.html".
     *
     * The real path returned will be in a form appropriate to the computer and operating system on which the servlet container is running,
     * including the proper path separators. This method returns null if the servlet container cannot translate the virtual path to a real path for
     * any reason (such as when the content is being made available from a .war archive).
     **/
    public String getRealPath(String path) {
        // We better don't assume that blocks are unpacked
        return null;
    }

    /**
     * Returns a String containing the value of the named context-wide initialization parameter, or null if the parameter does not exist.
     *
     * This method can make available configuration information useful to an entire "web application". For example, it can provide a
     * webmaster's email address or the name of a system that holds critical data.
     **/
    public String getInitParameter(String name) {
		String value = this.wiring.getProperty(name);
	    // Ask the super block for the property
		if (value == null) {
		    String superId = this.wiring.getBlockId(Block.SUPER);
		    // this.getLogger().debug("Try super property=" + name + " block=" + superId);
		    Block block = this.blocks.getBlock(superId);
		    if (block != null) {
		        value =  block.getProperty(name);
		    }
		}
		// Ask the parent context
		if (value == null) {
			super.getInitParameter(name);
		}
		return value;
    }

    /**
     * Returns the names of the context's initialization parameters as an Enumeration of String objects,
     * or an empty Enumeration if the context has no initialization parameters.
     **/
    public Enumeration getInitParameterNames() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Returns the resource located at the named path as an InputStream object.
     *
     * The data in the InputStream can be of any type or length. The path must be specified according to the rules given in getResource.
     * This method returns null if no resource exists at the specified path.

     * Meta-information such as content length and content type that is available via getResource method is lost when using this method.

     * The servlet container must implement the URL handlers and URLConnection objects necessary to access the resource.

     * This method is different from java.lang.Class.getResourceAsStream, which uses a class loader. This method allows servlet
     * containers to make a resource available to a servlet from any location, without using a class loader.
     **/
    public InputStream getResourceAsStream(String path) {
        try {
            return this.getResource(path).openStream();
        } catch (IOException e) {
            // FIXME Error handling
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a ServletContext object that corresponds to a specified URL on the server.
     * <p>
     * This method allows servlets to gain access to the context for various parts of the server,
     * and as needed obtain RequestDispatcher objects from the context. The given path must be
     * absolute (beginning with "/") and is interpreted based on the server's document root.
     * <p>
     * In a security conscious environment, the servlet container may return null for a given URL.
     **/
	public ServletContext getContext(String uripath) {
		return null;
	}

    /**
     * Returns the major version of the Java Servlet API that this servlet container supports.
     * All implementations that comply with Version 2.3 must have this method return the integer 2.
     **/
	public int getMajorVersion() {
		return 2;
	}

    /**
     * Returns the minor version of the Servlet API that this servlet container supports.
     * All implementations that comply with Version 2.3 must have this method return the integer 3.
     **/
	public int getMinorVersion() {
		return 3;
	}

    /**
     * Returns a directory-like listing of all the paths to resources within the web application
     * whose longest sub-path matches the supplied path argument. Paths indicating subdirectory paths end with a '/'.
     * The returned paths are all relative to the root of the web application and have a leading '/'.
     * For example, for a web application containing
     * <p>
     * /welcome.html<br />
     * /catalog/index.html<br /><br />
     * /catalog/products.html<br />
     * /catalog/offers/books.html<br />
     * /catalog/offers/music.html<br />
     * /customer/login.jsp<br />
     * /WEB-INF/web.xml<br />
     * /WEB-INF/classes/com.acme.OrderServlet.class,<br />
     * <br />
     * getResourcePaths("/") returns {"/welcome.html", "/catalog/", "/customer/", "/WEB-INF/"}<br />
     * getResourcePaths("/catalog/") returns {"/catalog/index.html", "/catalog/products.html", "/catalog/offers/"}.
     *
     * @param path partial path used to match the resources, which must start with a /
     * @return a Set containing the directory listing, or null if there are no resources
     *         in the web application whose path begins with the supplied path.
     * @since HttpUnit 1.3
     */
	public Set getResourcePaths(String arg0) {
		return null;
	}

    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the resource located at the given path. A RequestDispatcher
     * object can be used to forward a request to the resource or to include the resource in a response. The resource can be dynamic or static.

     * The pathname must begin with a "/" and is interpreted as relative to the current context root. Use getContext to obtain a
     * RequestDispatcher for resources in foreign contexts. This method returns null if the ServletContext cannot return a
     * RequestDispatcher.
     **/
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Returns a RequestDispatcher object that acts as a wrapper for the named servlet.
     *
     * Servlets (and JSP pages also) may be given names via server administration or via a web application deployment descriptor. A servlet
     * instance can determine its name using ServletConfig.getServletName().
     *
     * This method returns null if the ServletContext cannot return a RequestDispatcher for any reason.
     **/
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * @deprecated as of Servlet API 2.1
     **/
	public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

    /**
     * @deprecated as of Servlet API 2.0
     **/
	public Enumeration getServlets() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * @deprecated as of Servlet API 2.1
     **/
	public Enumeration getServletNames() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Writes the specified message to a servlet log file, usually an event log.
     * The name and type of the servlet log file is specific to the servlet container.
     **/
	public void log(String arg0) {
		// TODO Auto-generated method stub
		
	}

    /**
     * @deprecated as of Servlet API 2.1
     **/
	public void log(Exception arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

    /**
     * Writes an explanatory message and a stack trace for a given Throwable exception to the servlet log file.
     * The name and type of the servlet log file is specific to the servlet container, usually an event log.
     **/
	public void log(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		
	}

    /**
     * Returns the name and version of the servlet container on which the servlet is running.

     * The form of the returned string is servername/versionnumber. For example, the JavaServer Web Development Kit may return the
     * string JavaServer Web Dev Kit/1.0.

     * The servlet container may return other optional information after the primary string in parentheses, for example, JavaServer Web
     * Dev Kit/1.0 (JDK 1.1.6; Windows NT 4.0 x86).
     **/
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

    /**
     * Returns the name of this web application correponding to this ServletContext as specified
     * in the deployment descriptor for this web application by the display-name element.
     *
     * @return The name of the web application or null if no name has been declared in the deployment descriptor
     * @since HttpUnit 1.3
     */
	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

}
