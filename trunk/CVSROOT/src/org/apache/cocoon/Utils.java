/*-- $Id: Utils.java,v 1.4 2000-01-15 11:19:17 ricardo Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Utility methods for Cocoon and its classes.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.4 $ $Date: 2000-01-15 11:19:17 $
 */

public final class Utils {

    /**
     * This method returns a vector of PI nodes based on the PI target name.
     */
    public static final Vector getAllPIs(Document document, String name) {
        return getAllPIs(document, name, false);
    }
	
    /**
     * This method returns a vector of PI nodes based on the PI target name
     * and removes the found PIs from the document if the remove flag is
     * true.
     */
    public static final Vector getAllPIs(Document document, String name, boolean remove) {
        Vector pis = new Vector();
        
        NodeList nodelist = document.getChildNodes();
        int i = nodelist.getLength();
        for (int j = 0; j < i; j++) {
            Node node = nodelist.item(j);
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                if (((ProcessingInstruction) node).getTarget().equals(name)) {
                    pis.addElement(node);
                    if (remove) node.getParentNode().removeChild(node);
                }
            }
        }
        
        return pis;
    }
    
    /**
     * This method returns the first PI node based on the PI target name.
     */
    public static final ProcessingInstruction getFirstPI(Document document, String name) {
        return getFirstPI(document, name, false);
    }

    /**
     * This method returns the first PI node based on the PI target name and
     * removes it from the document if the remove flag is true.
     */
    public static final ProcessingInstruction getFirstPI(Document document, String name, boolean remove) {
        ProcessingInstruction pi = null;

        NodeList nodelist = document.getChildNodes();
        int i = nodelist.getLength();
        for (int j = 0; j < i; j++) {
            Node node = nodelist.item(j);
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                if (((ProcessingInstruction) node).getTarget().equals(name)) {
                    pi = (ProcessingInstruction) node;
                    if (remove) node.getParentNode().removeChild(node);
                    break;
                }
            }
        }

        return pi;
    }
    
    /**
     * This method returns an hashtable with all the pseudo attributes collected
     * in the document. If more PI have the same target, the attributes are
     * all put in the same hashtable. If there are collisions, the last attribute
     * is insered.
     */
    public static final Hashtable getPIPseudoAttributes(Document document, String name) {
        Hashtable attributes = new Hashtable();
        Enumeration nodes = getAllPIs(document, name).elements();
        
        while (nodes.hasMoreElements()) {
            String data = ((ProcessingInstruction) nodes.nextElement()).getData();
            for (StringTokenizer st = new StringTokenizer(data, " \t="); st.hasMoreTokens();) {
                String key = st.nextToken();
                String token = st.nextToken();
                token = token.substring(1, token.length() - 1);
                attributes.put(key, token);
            }
        }
        
        return attributes;
    }
    
    /**
     * This method returns an hashtable of pseudo attributes found in the first
     * occurrence of the PI with the given name in the given document.
     * No validation is performed on the PI pseudo syntax
     */
    public static final Hashtable getPIPseudoAttributes(ProcessingInstruction pi) {
        Hashtable attributes = new Hashtable();
        
        String data = pi.getData();
        for (StringTokenizer st = new StringTokenizer(data, " \t="); st.hasMoreTokens();) {
            String key = st.nextToken();
            String token = st.nextToken();
            token = token.substring(1, token.length() - 1);
            attributes.put(key, token);
        }
        
        return attributes;
    }

    /**
     * Encodes the given request into a string using the format
     *   protocol://serverName:serverPort/requestURI?query
     */
    public static final String encode(HttpServletRequest req) {
        return encode(req, true, true);
    }

    /**
     * Encodes the given request into a string using the format
     *   userAgent:protocol://serverName:serverPort/requestURI?query
     * with the agent flag controlling the presence of the userAgent
     * field.
     */
    public static final String encode(HttpServletRequest req, boolean agent) {
        return encode(req, agent, true);
    }
    
    /**
     * Encodes the given request into a string using the format
     *   userAgent:protocol://serverName:serverPort/requestURI?query
     * with the agent flag controlling the presence of the userAgent
     * field and the query flag controlling the query field.
     */
    public static final String encode(HttpServletRequest req, boolean agent, boolean query) {
        StringBuffer url = new StringBuffer();
        if (agent) {
            url.append(req.getHeader("user-Agent"));
            url.append(':');
        }
        url.append(req.getScheme());
        url.append("://");
        url.append(req.getServerName());
        url.append(':');
        url.append(req.getServerPort());
        url.append(req.getRequestURI());
        if (query) {
            url.append('?');
            url.append(req.getQueryString());
        }
        return url.toString();
    }

    /**
     * XXX: This is a dirty hack. The worst piece of code I ever wrote
     * and it clearly shows how Cocoon must change to support the Servlet API
     * 2.2 which has _much_ better mapping support thru the use of "getResource()"
     * but then, all the file system abstraction should be URL based.
     *
     * So, for now, leave the dirty code even if totally deprecated and work
     * out a better solution in the future.
     */
    public static String getBasename(HttpServletRequest request, Object context) {
        try {
            // detect if the engine supports at least Servlet API 2.2
            request.getContextPath();
            URL resource = ((ServletContext) context).getResource(request.getServletPath());
            if (resource.getProtocol().equals("file")) {
                return resource.getFile();
            } else {
                throw new RuntimeException("Cannot handle remote resources.");
            }
        } catch (NoSuchMethodError e) {
            // if there is no such method we must be in Servlet API 2.1
            if (request.getPathInfo() != null) {
                // this must be Apache JServ
                return request.getPathTranslated().replace('\\','/');
            } else {
                // otherwise use the deprecated method on all other servlet engines.
                return request.getRealPath(request.getRequestURI());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed request URL.");
        } catch (NullPointerException e) {
            // if there is no context set, we must be called from the command line
            return request.getPathTranslated().replace('\\','/');
        }
    }
}
