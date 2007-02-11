/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto.test;

import java.io.IOException;
import java.io.Writer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * This is a very simple test portlet
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: TestPortlet.java,v 1.5 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class TestPortlet implements Portlet  {
    
    /* (non-Javadoc)
     * @see javax.portlet.Portlet#destroy()
     */
    public void destroy() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#init(javax.portlet.PortletConfig)
     */
    public void init(PortletConfig config) throws PortletException {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void processAction(ActionRequest req, ActionResponse res)
    throws PortletException, IOException {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.portlet.Portlet#render(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void render(RenderRequest req, RenderResponse res)
    throws PortletException, IOException {
        res.setContentType("text/html");
        Writer writer = res.getWriter();
        writer.write("<h1>Simple TestPortlet</h1>\n");
        writer.write("<p>This is a Cocoon test portlet.</p>\n");
        writer.write("<p>I'm running in: " + req.getPortalContext().getPortalInfo());
        writer.write("<p>Current portlet mode: " + req.getPortletMode() + "</p>");
        writer.write("<p>Current window state: " + req.getWindowState() + "</p>");
    }

}
