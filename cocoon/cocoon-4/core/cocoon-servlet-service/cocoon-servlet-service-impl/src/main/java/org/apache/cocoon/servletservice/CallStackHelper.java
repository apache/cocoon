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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.callstack.CallFrame;
import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.callstack.environment.CallFrameHelper;

/**
 * Helper class used for geting hold on the current servlet service
 *
 * @version $Id$
 * @since 2.2 
 */
public class CallStackHelper {

    /** Key for a value determing wether a call frame contains a super call or not */
    public final static String SUPER_CALL = "super";

    /**
     * This hook must be called each time a servlet service is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ServletException if at least one of the parameters is null
     */
    public static void enterServlet(ServletContext context, HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
        enterServlet(context, request, response, false);
    }

    /**
     * This hook must be called each time a super servlet service is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ServletException if at least one of the parameters is null
     */
    public static void enterSuperServlet(ServletContext context, HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
        enterServlet(context, request, response, true);
    }

    private static void enterServlet(ServletContext context, HttpServletRequest request, HttpServletResponse response, boolean superCall)
    throws ServletException {
        if (null == context) throw new ServletException("The context is not set.");
        if (null == request) throw new ServletException("The request is not set.");
        if (null == response) throw new ServletException("The response is not set.");
        

        CallStack.enter();
        CallStack.getCurrentFrame().setAttribute(SUPER_CALL, new Boolean(superCall));
        CallFrameHelper.setContext(context);
        CallFrameHelper.setRequest(request);
        CallFrameHelper.setResponse(response);
    }

    /**
     * This hook must be called each time a servlet service is left.
     *
     * <p>It's the counterpart to the {@link #enterServlet(ServletContext)}
     * method.</p>
     */
    public static void leaveServlet() {
        CallStack.leave();
    }

    /**
     * Use this method for getting the context that should be used for
     * resolving a polymorphic servlet protocol call 
     * @return a servlet context
     */
    public static ServletContext getBaseServletContext() {
        for(int i = CallStack.size() - 1; i >= 0; i--) {
            CallFrame frame = CallStack.frameAt(i);
            if (frame.hasAttribute(SUPER_CALL) && !((Boolean)frame.getAttribute(SUPER_CALL)).booleanValue())
                return (ServletContext) frame.getAttribute(CallFrameHelper.CONTEXT_OBJECT);
        }
        return null;
    }

    /**
     * Use this method for getting the context that should be used for
     * resolving a servlet protocol call to a super servlet service 
     * @return a servlet context
     */
    public static ServletContext getCurrentServletContext() {
        return CallFrameHelper.getContext();
    }
}
