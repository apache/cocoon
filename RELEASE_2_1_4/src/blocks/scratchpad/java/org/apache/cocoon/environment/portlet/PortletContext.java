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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.environment.Context;

import org.apache.avalon.framework.CascadingRuntimeException;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

/**
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 * for the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletContext.java,v 1.2 2003/12/03 13:20:29 vgritsenko Exp $
 */
public final class PortletContext implements Context {

    /**
     * The PortletContext
     */
    private final javax.portlet.PortletContext context;

    /**
     * Constructs a PortletContext object from a PortletContext object
     */
    public PortletContext(javax.portlet.PortletContext context) {
        this.context = context;
    }

    public Object getAttribute(String name) {
        return context.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        context.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        context.removeAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return context.getAttributeNames();
    }

    public URL getResource(String path) throws MalformedURLException {
        return context.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return context.getResourceAsStream(path);
    }

    public String getRealPath(String path) {
        if (path.equals("/")) {
            String value = context.getRealPath(path);
            if (value == null) {
                // Try to figure out the path of the root from that of WEB-INF
                try {
                    value = this.context.getResource("/WEB-INF").toString();
                } catch (MalformedURLException mue) {
                    throw new CascadingRuntimeException("Cannot determine the base URL for " + path, mue);
                }
                value = value.substring(0, value.length() - "WEB-INF".length());
            }
            return value;
        }
        return context.getRealPath(path);
    }

    public String getMimeType(String file) {
        return context.getMimeType(file);
    }

    public String getInitParameter(String name) {
        return context.getInitParameter(name);
    }


    // PortletContext methods

    public Enumeration getInitParameterNames() {
        return context.getInitParameterNames();
    }

    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    public String getPortletContextName() {
        return context.getPortletContextName();
    }

    public String getServerInfo() {
        return context.getServerInfo();
    }
}
