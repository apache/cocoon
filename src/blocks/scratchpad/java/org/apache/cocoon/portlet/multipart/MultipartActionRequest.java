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
package org.apache.cocoon.portlet.multipart;

import org.apache.cocoon.servlet.multipart.PartOnDisk;

import javax.portlet.ActionRequest;
import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

/**
 * Portlet action request wrapper for multipart parser.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: MultipartActionRequest.java,v 1.1 2003/12/03 13:09:34 vgritsenko Exp $
 */
public class MultipartActionRequest implements ActionRequest {

    /** The wrapped request */
    private ActionRequest request = null;

    /** The submitted parts */
    private Hashtable values = null;

    /**
     * Create this wrapper around the given request and including the given
     * parts.
     */
    public MultipartActionRequest(ActionRequest request, Hashtable values) {
        this.request = request;
        this.values = values;
    }

    /**
     * Cleanup eventually uploaded parts that were saved on disk
     */
    public void cleanup() throws IOException {
        Enumeration e = getParameterNames();
        while (e.hasMoreElements()) {
            Object o = get((String) e.nextElement());
            if (o instanceof PartOnDisk) {
                File file = ((PartOnDisk) o).getFile();
                file.delete();
            }
        }
    }

    /**
     * Method get
     *
     * @param name
     */
    public Object get(String name) {
        Object result = null;

        if (values != null) {
            result = values.get(name);

            if (result instanceof Vector) {
                if (((Vector) result).size() == 1) {
                    return ((Vector) result).elementAt(0);
                } else {
                    return result;
                }
            }
        }

        // TODO: Test multipart form with parameter in action="" attribute
        if (result == null) {
            String[] array = request.getParameterValues(name);
            Vector vec = new Vector();

            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    vec.addElement(array[i]);
                }

                if (vec.size() == 1) {
                    result = vec.elementAt(0);
                } else {
                    result = vec;
                }
            }
        }

        return result;
    }

    /**
     * Method getParameterNames
     */
    public Enumeration getParameterNames() {
        if (values != null) {
            return values.keys();
        } else {
            return request.getParameterNames();
        }
    }

    /**
     * Method getParameter
     *
     * @param name
     */
    public String getParameter(String name) {
        Object value = get(name);
        String result = null;

        if (value != null) {
            if (value instanceof Vector) {
                value = ((Vector) value).elementAt(0);
            }

            result = value.toString();
        }

        return result;
    }

    /**
     * Method getParameterValues
     *
     * @param name
     */
    public String[] getParameterValues(String name) {
        if (values != null) {
            Object value = get(name);

            if (value != null) {
                if (value instanceof Vector) {
                    String[] results = new String[((Vector) value).size()];
                    for (int i = 0; i < ((Vector) value).size(); i++) {
                        results[i] = ((Vector) value).elementAt(i).toString();
                    }
                    return results;

                } else {
                    return new String[]{value.toString()};
                }
            }

            return null;
        } else {
            return request.getParameterValues(name);
        }
    }

    /**
     * Method getAttribute
     *
     * @param name
     */
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }

    /**
     * Method getAttributeNames
     */
    public Enumeration getAttributeNames() {
        return request.getAttributeNames();
    }

    /**
     * Method getCharacterEncoding
     */
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    /**
     * Method getContentLength
     */
    public int getContentLength() {
        return request.getContentLength();
    }

    /**
     * Method getContentType
     */
    public String getContentType() {
        return request.getContentType();
    }

    /**
     * Method getInputStream
     *
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        return request.getPortletInputStream();
    }

    /**
     * Method getScheme
     */
    public String getScheme() {
        return request.getScheme();
    }

    /**
     * Method getServerName
     */
    public String getServerName() {
        return request.getServerName();
    }

    /**
     * Method getServerPort
     */
    public int getServerPort() {
        return request.getServerPort();
    }

    /**
     * Method getReader
     *
     * @throws IOException
     */
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    /**
     * Method setAttribute
     *
     * @param name
     * @param o
     */
    public void setAttribute(String name, Object o) {
        request.setAttribute(name, o);
    }

    /**
     * Method removeAttribute
     *
     * @param name
     */
    public void removeAttribute(String name) {
        request.removeAttribute(name);
    }

    /**
     * Method getLocale
     */
    public Locale getLocale() {
        return request.getLocale();
    }

    /**
     * Method getLocales
     */
    public Enumeration getLocales() {
        return request.getLocales();
    }

    /**
     * Method isSecure
     */
    public boolean isSecure() {
        return request.isSecure();
    }

    /**
     * Method getAuthType
     */
    public String getAuthType() {
        return request.getAuthType();
    }

    /**
     * Method getContextPath
     */
    public String getContextPath() {
        return request.getContextPath();
    }

    /**
     * Method getRemoteUser
     *
     */
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    /**
     * Method isUserInRole
     *
     * @param role
     */
    public boolean isUserInRole(String role) {
        return request.isUserInRole(role);
    }

    /**
     * Method getUserPrincipal
     */
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    /**
     * Method getRequestedSessionId
     */
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    /**
     * Method getSession
     *
     * @param create
     */
    public PortletSession getPortletSession(boolean create) {
        return request.getPortletSession(create);
    }

    /**
     * Method getSession
     */
    public PortletSession getPortletSession() {
        return request.getPortletSession();
    }

    /**
     * Method isRequestedSessionIdValid
     */
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }


    /* (non-Javadoc)
     * @see javax.portlet.ActionRequest#getPortletInputStream()
     */
    public InputStream getPortletInputStream() throws IOException {
        return request.getPortletInputStream();
    }

    /* (non-Javadoc)
     * @see javax.portlet.ActionRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        request.setCharacterEncoding(enc);
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return request.getParameterMap();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPortalContext()
     */
    public PortalContext getPortalContext() {
        return request.getPortalContext();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPortletMode()
     */
    public PortletMode getPortletMode() {
        return request.getPortletMode();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPreferences()
     */
    public PortletPreferences getPreferences() {
        return request.getPreferences();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getProperties(java.lang.String)
     */
    public Enumeration getProperties(String name) {
        return request.getProperties(name);
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        return request.getProperty(name);
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getPropertyNames()
     */
    public Enumeration getPropertyNames() {
        return request.getPropertyNames();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getResponseContentType()
     */
    public String getResponseContentType() {
        return request.getResponseContentType();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getResponseContentTypes()
     */
    public Enumeration getResponseContentTypes() {
        return request.getResponseContentTypes();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#getWindowState()
     */
    public WindowState getWindowState() {
        return request.getWindowState();
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#isPortletModeAllowed(javax.portlet.PortletMode)
     */
    public boolean isPortletModeAllowed(PortletMode mode) {
        return request.isPortletModeAllowed(mode);
    }

    /* (non-Javadoc)
     * @see javax.portlet.PortletRequest#isWindowStateAllowed(javax.portlet.WindowState)
     */
    public boolean isWindowStateAllowed(WindowState state) {
        return request.isWindowStateAllowed(state);
    }
}
