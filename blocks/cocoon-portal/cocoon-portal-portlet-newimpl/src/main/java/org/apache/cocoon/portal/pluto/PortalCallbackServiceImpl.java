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
package org.apache.cocoon.portal.pluto;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.PortletURLProvider;
import org.apache.pluto.spi.ResourceURLProvider;

/**
 * @version $Id$
 */
public class PortalCallbackServiceImpl
    extends AbstractBean
    implements PortalCallbackService {

    /**
     * @see org.apache.pluto.spi.PortalCallbackService#addResponseProperty(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String, java.lang.String)
     */
    public void addResponseProperty(HttpServletRequest request, PortletWindow window, String name, String value) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.apache.pluto.spi.PortalCallbackService#getPortletURLProvider(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public PortletURLProvider getPortletURLProvider(HttpServletRequest request, PortletWindow window) {
        return new PortletURLProviderImpl(this.portalService, window, null);
    }

    /**
     * @see org.apache.pluto.spi.PortalCallbackService#getRequestProperties(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public Map getRequestProperties(HttpServletRequest request, PortletWindow window) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.apache.pluto.spi.PortalCallbackService#getResourceURLProvider(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public ResourceURLProvider getResourceURLProvider(HttpServletRequest request, PortletWindow window) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.apache.pluto.spi.PortalCallbackService#setResponseProperty(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String, java.lang.String)
     */
    public void setResponseProperty(HttpServletRequest request, PortletWindow window, String name, String value) {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.apache.pluto.spi.PortalCallbackService#setTitle(javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String)
     */
    public void setTitle(HttpServletRequest request, PortletWindow window, String title) {
        // TODO Auto-generated method stub

    }
}
