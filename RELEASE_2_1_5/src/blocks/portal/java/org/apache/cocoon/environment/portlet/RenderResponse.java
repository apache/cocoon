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
 * @version CVS $Id: RenderResponse.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
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
