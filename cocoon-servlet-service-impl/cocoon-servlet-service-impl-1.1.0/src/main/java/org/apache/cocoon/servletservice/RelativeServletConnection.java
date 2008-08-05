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
package org.apache.cocoon.servletservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.cocoon.callstack.environment.CallFrameHelper;
import org.apache.cocoon.servletservice.util.ServletServiceRequest;
import org.apache.cocoon.servletservice.util.ServletServiceResponse;

/**
 * Implementation of a {@link ServletConnection} that gets its content by
 * invoking the servlet service. It works based on the context of the current
 * servlet context and its connections.
 *
 * @version $Id$
 * @since 1.0.0
 */
public final class RelativeServletConnection extends AbstractServletConnection {

    private String connectionName;

    public RelativeServletConnection(String connectionName, String path, String queryString) {
        // path validation
        if (path == null) {
            throw new IllegalArgumentException("Path musn't be null.");
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException(
                            "The path has to start with a slash '/' because it is always absolute within this servlet context.");
        }

        this.connectionName = connectionName;

        // setup URI
        URI reqUri;
        try {
            this.uri = new URI((connectionName != null ? connectionName : "local"), null, path, queryString, null);
            this.uri = new URI("servlet", this.uri.toASCIIString(), null);
            reqUri =  new URI("servlet", null, path, queryString, null);
        } catch (URISyntaxException e) {
            String message = "Invalid relative servlet service URI created.";
            this.logger.error(message, e);
            throw new RuntimeException(message, e);
        }

        // lookup the servlet context
        if (ServletServiceContext.SUPER.equals(this.connectionName)) {
            // Super calls are resolved relative the current context and ordinary
            // calls relative to the last non super call in the call chain
            this.context = CallStackHelper.getCurrentServletContext();
        } else {
            this.context = CallStackHelper.getBaseServletContext();
        }
        if (this.context == null) {
            throw new NoServletContextAvailableException(
                            "A servlet connection can only be used with an available servlet context. [" + this.uri
                                            + "]");
        }

        // prepare request and response objects
        this.request = new ServletServiceRequest(reqUri, CallFrameHelper.getRequest());
        this.response = new ServletServiceResponse();

        if(this.logger.isDebugEnabled()) {
            this.logger.debug("Resolving relative servlet URI " + this.uri.toASCIIString());
        }
    }

    /**
     * Perform the actual connect that invokes the servlet service.
     */
    protected void performConnect() throws ServletException, IOException {
        final RequestDispatcher dispatcher;
        if (this.connectionName == null) {
            dispatcher = this.context.getRequestDispatcher(null);
        } else {
            dispatcher = this.context.getNamedDispatcher(this.connectionName);
        }

        if (dispatcher == null) {
            throw new ServletException("No dispatcher for connection" + this.connectionName);
        }
        dispatcher.forward(this.request, this.response);
    }

}
