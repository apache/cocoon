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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements the {@link org.apache.cocoon.environment.Request} interface for the
 * JSR-168 (Portlet) environment.
 *
 * @version CVS $Id: ActionRequest.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vadim.gritsenko@dc.gov">Vadim Gritsenko</a>
 */
public final class ActionRequest extends PortletRequest {

    /**
     * Creates a ActionRequest based on a real ActionRequest object
     */
    protected ActionRequest(String servletPath,
                            String pathInfo,
                            javax.portlet.ActionRequest request,
                            PortletEnvironment environment) {
        super(servletPath, pathInfo, request, environment);
    }

    // Request API methods

    public String getCharacterEncoding() {
        if (super.getCharacterEncoding() == null) {
            return getActionRequest().getCharacterEncoding();
        } else {
            return super.getCharacterEncoding();
        }
    }

    /**
     * Action request can be always recognized by POST method
     */
    public String getMethod() {
        return "POST";
    }


    // ActionRequest API methods

    /**
     * Type cast portletRequest to ActionRequest
     *
     * @return type casted portletRequest
     */
    public javax.portlet.ActionRequest getActionRequest() {
        return (javax.portlet.ActionRequest) getPortletRequest();
    }

    public InputStream getPortletInputStream() throws IOException {
        return getActionRequest().getPortletInputStream();
    }

    public BufferedReader getReader() throws IOException {
        return getActionRequest().getReader();
    }
}
