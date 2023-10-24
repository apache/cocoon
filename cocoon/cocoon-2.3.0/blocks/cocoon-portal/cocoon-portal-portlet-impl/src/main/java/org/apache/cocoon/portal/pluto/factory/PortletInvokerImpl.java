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
package org.apache.cocoon.portal.pluto.factory;

import java.io.IOException;

import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;

import org.apache.cocoon.portal.pluto.servlet.PortletServlet;
import org.apache.pluto.core.CoreUtils;
import org.apache.pluto.core.InternalPortletRequest;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * This component invokes a portlet (using the servlet dispatcher).
 *
 * $Id$
 */
public class PortletInvokerImpl
    extends org.apache.pluto.invoker.impl.PortletInvokerImpl {

    protected final PortletDefinition portletDefinition;

    public PortletInvokerImpl(PortletDefinition portletDefinition,
                              ServletConfig servletConfig) {
        super(portletDefinition, servletConfig);
        this.portletDefinition = portletDefinition;
    }

    /**
     * generic method to be used called by both, action and render
     */
    protected void invoke(PortletRequest portletRequest, PortletResponse portletResponse, Integer methodID) 
    throws PortletException,IOException {
        InternalPortletRequest internalPortletRequest = CoreUtils.getInternalRequest(portletRequest);
        ServletRequest servletRequest = ((javax.servlet.http.HttpServletRequestWrapper)internalPortletRequest).getRequest();
        try {
            servletRequest.setAttribute(PortletServlet.PORTLET_DEFINITION, this.portletDefinition);
            super.invoke(portletRequest, portletResponse, methodID);
        } finally {
            servletRequest.removeAttribute(PortletServlet.PORTLET_DEFINITION);
        }
    }
}
