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

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;

import org.apache.avalon.framework.CascadingRuntimeException;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implements the {@link Response} interface for the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletResponse.java,v 1.2 2003/12/03 13:20:29 vgritsenko Exp $
 */
public abstract class PortletResponse implements Response {

    /** The real PortletResponse object */
    private final javax.portlet.PortletResponse response;

    private final PortletPreferences preferences;

    /**
     * Stores property names set on the response.
     * Portlet response does not have containsProperty() method.
     */
    private Map properties = new HashMap(5);


    /**
     * Creates a PortletResponse based on a real PortletResponse object
     */
    protected PortletResponse(javax.portlet.PortletResponse response,
                              PortletPreferences preferences) {
        this.response = response;
        this.preferences = preferences;
    }


    public boolean containsHeader(String name) {
        return properties.containsKey(name);
    }

    public void setHeader(String name, String value) {
        properties.put(name, name);
        response.setProperty(name, value);
    }

    public void setIntHeader(String name, int value) {
        setHeader(name, "" + value);
    }

    public void setDateHeader(String name, long date) {
        setHeader(name, "" + date);
    }

    public void addHeader(String name, String value) {
        properties.put(name, name);
        response.addProperty(name, value);
    }

    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }

    public void addDateHeader(String name, long date) {
        addHeader(name, "" + date);
    }


    public String getCharacterEncoding() {
        return null;
    }

    public Cookie createCookie(String name, String value) {
        return new PortletCookie(name, value);
    }

    public void addCookie(Cookie cookie) {
        try {
            this.preferences.setValue(cookie.getName(), cookie.getValue());
// TODO: When is good time to persist changes?
            this.preferences.store();
        } catch (ReadOnlyException e) {
            throw new CascadingRuntimeException("Cannot set read-only preference '" + cookie.getName() + "'", e);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot set preference '" + cookie.getName() + "'", e);
        }
    }

    public void setLocale(Locale locale) {
    }

    public Locale getLocale() {
        return null;
    }

    public String encodeURL(String url) {
// TODO: Why this check?
        if (url != null && url.indexOf(";jsessionid=") != -1) {
            return url;
        }
        return this.response.encodeURL(url);
    }


    // Portlet API related methods

    /**
     * Provides access to the underlying response object
     * @return portlet API response object
     */
    public javax.portlet.PortletResponse getPortletResponse() {
        return response;
    }

    public void addProperty(String key, String value) {
        getPortletResponse().addProperty(key, value);
    }

    public void setProperty(String key, String value) {
        getPortletResponse().setProperty(key, value);
    }


    // Portlet Environment Methods

    OutputStream getOutputStream() throws IOException {
        throw new IllegalStateException("Operation 'getOutputStream' is not supported by '" + getClass().getName() + "'");
    }

    void setContentType(String type) {
        throw new IllegalStateException("Operation 'setContentType' is not supported by '" + getClass().getName() + "'");
    }

    void sendRedirect(String location) throws IOException {
        throw new IllegalStateException("Operation 'sendRedirect' is not supported by '" + getClass().getName() + "'");
    }

    boolean isCommitted() {
        throw new IllegalStateException("Operation 'isCommitted' is not supported by '" + getClass().getName() + "'");
    }

    void reset() {
        throw new IllegalStateException("Operation 'reset' is not supported by '" + getClass().getName() + "'");
    }
}
