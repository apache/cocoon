/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.corona.servlet.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HttpContextHelper {

    private static final String HTTP_SERVLET_REQUEST_KEY = HttpServletRequest.class.getName();

    private static final String HTTP_SERVLET_RESPONSE_KEY = HttpServletResponse.class.getName();

    public static HttpServletRequest getRequest(Map<String, ? extends Object> parameters) {
        Object parameter = parameters.get(HTTP_SERVLET_REQUEST_KEY);
        if (parameter instanceof HttpServletRequest) {
            return (HttpServletRequest) parameter;
        }

        throw new IllegalStateException(
                "A HttpServletRequest is not available. This might indicate an invocation outside a servlet.");
    }

    public static HttpServletResponse getResponse(Map<String, ? extends Object> parameters) {
        Object parameter = parameters.get(HTTP_SERVLET_RESPONSE_KEY);
        if (parameter instanceof HttpServletResponse) {
            return (HttpServletResponse) parameter;
        }

        throw new IllegalStateException(
                "A HttpServletResponse is not available. This might indicate an invocation outside a servlet.");
    }

    public static void storeRequest(HttpServletRequest httpServletRequest, Map<String, Object> parameters) {
        parameters.put(HTTP_SERVLET_REQUEST_KEY, httpServletRequest);
    }

    public static void storeResponse(HttpServletResponse httpServletResponse, Map<String, Object> parameters) {
        parameters.put(HTTP_SERVLET_RESPONSE_KEY, httpServletResponse);
    }
}
