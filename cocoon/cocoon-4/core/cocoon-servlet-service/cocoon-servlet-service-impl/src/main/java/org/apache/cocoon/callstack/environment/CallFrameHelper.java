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
package org.apache.cocoon.callstack.environment;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.callstack.CallFrame;
import org.apache.cocoon.callstack.CallStack;

/**
 * A set of constants and methods to access the content of the call frame.
 * <p>
 * The call frame is used to pass information about the Request, Response and
 * Context of the calling environment to components used while the call frame
 * is active.
 * <p>
 *
 * @version $Id$
 */
public final class CallFrameHelper {

    /** Key for the environment {@link HttpServletRequest} in the call frame. */
    public final static String REQUEST_OBJECT  = "request";

    /** Key for the environment {@link HttpServletResponse} in the call frame. */
    public final static String RESPONSE_OBJECT = "response";

    /** Key for the environment {@link ServletContext} in the call frame. */
    public final static String CONTEXT_OBJECT  = "context";

    private CallFrameHelper() {
        // Forbid instantiation
    }
    
    public static final void setEnvironment(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        CallFrame frame = CallStack.getCurrentFrame();
        frame.setAttribute(REQUEST_OBJECT, request);
        frame.setAttribute(RESPONSE_OBJECT, response);
        frame.setAttribute(CONTEXT_OBJECT, context);
    }

    public static final HttpServletRequest getRequest() {
        return (HttpServletRequest) CallStack.getCurrentFrame().getAttribute(REQUEST_OBJECT);
    }

    public static final void setRequest(HttpServletRequest request) {
        CallStack.getCurrentFrame().setAttribute(REQUEST_OBJECT, request);
    }

    public static final HttpServletResponse getResponse() {
        return (HttpServletResponse) CallStack.getCurrentFrame().getAttribute(RESPONSE_OBJECT);
    }

    public static final void setResponse(HttpServletResponse response) {
        CallStack.getCurrentFrame().setAttribute(RESPONSE_OBJECT, response);
    }

    public static final ServletContext getContext() {
        return (ServletContext) CallStack.getCurrentFrame().getAttribute(CONTEXT_OBJECT);
    }

    public static final void setContext(ServletContext context) {
        CallStack.getCurrentFrame().setAttribute(CONTEXT_OBJECT, context);
    }
}
