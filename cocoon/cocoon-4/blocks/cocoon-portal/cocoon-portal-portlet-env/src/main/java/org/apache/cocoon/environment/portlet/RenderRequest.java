/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.InputStream;

/**
 * Implements the {@link org.apache.cocoon.environment.Request} interface for
 * the JSR-168 (Portlet) environment.
 *
 * @version $Id$
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

	/* (non-Javadoc)
	 * @see org.apache.cocoon.environment.Request#getInputStream()
	 */
	public InputStream getInputStream() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
}
