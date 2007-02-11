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

/**
 * Implements the {@link org.apache.cocoon.environment.Request} interface for
 * the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RenderRequest.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public final class RenderRequest extends PortletRequest {

    /**
     * Creates a RenderRequest based on a real RenderRequest object
     */
    protected RenderRequest(String servletPath,
                            String pathInfo,
                            javax.portlet.RenderRequest request,
                            PortletEnvironment environment) {
        super(servletPath, pathInfo, request, environment);
    }

    /**
     * Type cast portletRequest to RenderRequest
     *
     * @return type casted portletRequest
     */
    public javax.portlet.RenderRequest getRenderRequest() {
        return (javax.portlet.RenderRequest) getPortletRequest();
    }

    /**
     * Render request can be always recognized by GET method
     */
    public String getMethod() {
        return "GET";
    }
}
