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

import javax.portlet.PortletPreferences;
import javax.portlet.PortletURL;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Implements the {@link org.apache.cocoon.environment.Response} interface for
 * the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RenderResponse.java,v 1.3 2004/02/19 22:13:28 joerg Exp $
 */
public final class RenderResponse extends PortletResponse {

    private String contentType = null;

    /**
     * Creates a RenderResponse based on a real RenderResponse object
     */
    protected RenderResponse(javax.portlet.RenderResponse response,
                             PortletPreferences preferences) {
        super(response, preferences);
    }


    public String getCharacterEncoding() {
        return getRenderResponse().getCharacterEncoding();
    }

    public Locale getLocale() {
        return getRenderResponse().getLocale();
    }

    /**
     *
     * @see PortletEnvironment#HEADER_PORTLET_TITLE
     */
    public void addHeader(String name, String value) {
        if (PortletEnvironment.HEADER_PORTLET_TITLE.equals(name)) {
            getRenderResponse().setTitle(value);
        } else {
            super.addHeader(name, value);
        }
    }

    /**
     *
     * @see PortletEnvironment#HEADER_PORTLET_TITLE
     */
    public void setHeader(String name, String value) {
        if (PortletEnvironment.HEADER_PORTLET_TITLE.equals(name)) {
            getRenderResponse().setTitle(value);
        } else {
            super.setHeader(name, value);
        }
    }


    // RenderResponse API related methods

    /**
     * Type cast portletResponse to RenderResponse
     *
     * @return type casted portletResponse
     */
    public javax.portlet.RenderResponse getRenderResponse() {
        return (javax.portlet.RenderResponse) getPortletResponse();
    }

    public PortletURL createActionURL() {
        return getRenderResponse().createActionURL();
    }

    public PortletURL createRenderURL() {
        return getRenderResponse().createRenderURL();
    }

    public void flushBuffer() throws IOException {
        getRenderResponse().flushBuffer();
    }

    public int getBufferSize() {
        return getRenderResponse().getBufferSize();
    }

    public String getContentType() {
        return getRenderResponse().getContentType();
    }

    public String getNamespace() {
        return getRenderResponse().getNamespace();
    }

    public OutputStream getPortletOutputStream() throws IOException {
        return getRenderResponse().getPortletOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        return getRenderResponse().getWriter();
    }

    public boolean isCommitted() {
        return getRenderResponse().isCommitted();
    }

    public void reset() {
        getRenderResponse().reset();
    }

    public void resetBuffer() {
        getRenderResponse().resetBuffer();
    }

    public void setBufferSize(int size) {
        getRenderResponse().setBufferSize(size);
    }

    public void setContentType(String type) {
        this.contentType = type;
        getRenderResponse().setContentType(type);
    }

    public void setTitle(String title) {
        getRenderResponse().setTitle(title);
    }


    // Portlet Environment related methods

    OutputStream getOutputStream() throws IOException {
        // TODO: Why this is needed? What's the purpose?
        if (this.contentType == null) {
            setContentType("text/html");
        }

        return getRenderResponse().getPortletOutputStream();
    }
}
