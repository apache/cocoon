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
package org.apache.cocoon.webapps.session.context;

import java.io.IOException;
import java.util.Map;

import org.apache.excalibur.source.SourceParameters;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;
import org.apache.excalibur.source.SourceResolver;
import org.apache.cocoon.xml.dom.DOMUtil;

import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;


/**
 * A SessionContext which encapsulates the current Response object.
 *
 * The following XML for setXML and appendXML is allowed:
 * /header/<headername> - The content is the value of the header
 * /cookie              - The content is the cookie:
 *                <name>gsdgsdg</name>  - name of the cookie
 *                <value>gdgdgdgs</value> - cookie value
 *               optional:
 *                <domain/>
 *                <path/>
 *                <secure>true or false</secure>
 *                <comment/>
 *                <version/>
 *                <maxAge/>
 *
 * Using setXML uses setHeader() and appendXML uses addHeader. Despite this they
 * both have the same effect.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: ResponseSessionContext.java,v 1.2 2003/05/04 20:19:40 cziegeler Exp $
*/
public final class ResponseSessionContext
implements SessionContext {

    private String             name;
    transient private Response response;

    public void setup(String value, String loadResource, String saveResource) {
        this.name = value;
    }

    public void setup(Map objectModel) {
        this.response = ObjectModelHelper.getResponse(objectModel);
    }

    /**
     * Get the name of the context
     */
    public String getName() {
        return this.name;
    }
    /**
     * Get the request object
     */
    public Response getResponse() {
        return this.response;
    }

    /**
     * Build the cookie
     */
    private Cookie createCookie(SourceParameters par) {
        Cookie cookie = this.response.createCookie(par.getParameter("name"),
                                   par.getParameter("value"));
        String value;

        value = par.getParameter("comment");
        if (value != null) cookie.setComment(value);
        value = par.getParameter("domain");
        if (value != null) cookie.setDomain(value);
        value = par.getParameter("path");
        if (value != null) cookie.setPath(value);
        if (par.containsParameter("maxAge") == true) {
            cookie.setMaxAge(par.getParameterAsInteger("maxAge", 0));
        }
        if (par.containsParameter("version") == true) {
            cookie.setVersion(par.getParameterAsInteger("version", 0));
        }
        if (par.containsParameter("secure") == true) {
            cookie.setSecure(par.getParameterAsBoolean("secure", true));
        }
        return cookie;
    }

    /**
     * Get the XML from the response object
     */
    public DocumentFragment getXML(String path)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of xml not allowed.");
    }

    public void setXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        if (this.response == null) {
            throw new ProcessingException("Response Object missing");
        }
        if (path != null) {
            if (path.startsWith("/header/") == true) {
                String name = path.substring(8);
                this.response.setHeader(name, DOMUtil.createText(fragment));
            } else if (path.equals("/cookie") == true) {
                this.response.addCookie(this.createCookie(DOMUtil.createParameters(fragment, null)));
            } else {
                throw new ProcessingException("Invalid response path '"+path+"'");
            }
        }
    }

    /**
     * Append a document fragment at the given path. The implementation of this
     * method is context specific.
     */
    public void appendXML(String path, DocumentFragment fragment)
    throws ProcessingException {
        if (this.response == null) {
            throw new ProcessingException("Response Object missing");
        }
        if (path != null) {
            if (path.startsWith("/header/") == true) {
                String name = path.substring(8);
                this.response.addHeader(name, DOMUtil.createText(fragment));
            } else if (path.equals("/cookie") == true) {
                this.response.addCookie(this.createCookie(DOMUtil.createParameters(fragment, null)));
            } else {
                throw new ProcessingException("Invalid response path '"+path+"'");
            }
        }

    }

    public void removeXML(String path)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Removing of xml not allowed");
    }

    /**
     * Set a context attribute.
     */
    public void setAttribute(String key, Object value)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Setting of attributes not allowed");
    }

    /**
     * Get a context attribute.
     */
    public Object getAttribute(String key)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of attributes not allowed");
    }

    /**
     * Get a context attribute.
     */
    public Object getAttribute(String key, Object defaultObject)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of attributes not allowed");
    }

    /**
     * Get a copy the first node specified by the path.
     */
    public Node getSingleNode(String path)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of xml not allowed");
    }

    /**
     * Get a copy all the nodes specified by the path.
     */
    public NodeList getNodeList(String path)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of xml not allowed");
    }

    /**
     * Set the value of a node. The node is copied before insertion.
     */
    public void setNode(String path, Node node)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Setting of XML not allowed");
    }

    /**
     * Get the value of this node. This is similiar to the xsl:value-of
     * function. If the node does not exist, <code>null</code> is returned.
     */
    public String getValueOfNode(String path)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of xml not allowed");
    }

    /**
     * Set the value of this node.
     */
    public void setValueOfNode(String path, String value)
    throws ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Setting of xml not allowed");
    }

    /**
     * Stream the XML directly to the handler. This streams the contents of getXML()
     * to the given handler without creating a DocumentFragment containing a copy
     * of the data
     */
    public boolean streamXML(String path,
                             ContentHandler contentHandler,
                             LexicalHandler lexicalHandler)
    throws SAXException, ProcessingException {
        throw new ProcessingException("ResponseSessionContext: Getting of xml not allowed");
    }

    /**
     * Try to load XML into the context.
     * If the context does not provide the ability of loading,
     * an exception is thrown.
     */
    public void loadXML(String path,
                        SourceParameters parameters,
                        Map                objectModel,
                        SourceResolver     resolver,
                        ComponentManager   manager)
    throws SAXException, ProcessingException, IOException {
        throw new ProcessingException("The context " + this.name + " does not support loading.");
    }

    /**
     * Try to save XML from the context.
     * If the context does not provide the ability of saving,
     * an exception is thrown.
     */
    public void saveXML(String path,
                        SourceParameters parameters,
                        Map                objectModel,
                        SourceResolver     resolver,
                        ComponentManager   manager)
    throws SAXException, ProcessingException, IOException {
        throw new ProcessingException("The context " + this.name + " does not support saving.");
    }

}
