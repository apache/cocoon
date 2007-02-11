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
package org.apache.cocoon.portal.pluto.test;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Iterator;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletPreferences;

/**
 * This is a very simple test portlet
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
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

        PortletPreferences prefs = req.getPreferences();
        String key = req.getParameter("name");
        String value = req.getParameter("value");
        if (key != null && value != null) {
            prefs.setValue(key, value);
            try {
                prefs.store();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        writer.write("<p>I'm running in: " + req.getPortalContext().getPortalInfo() + "</p>");
        writer.write("<p>Current portlet mode: " + req.getPortletMode() + "</p>");
        writer.write("<p>Current window state: " + req.getWindowState() + "</p>");
        writer.write("<p>Portlet Preferences:</p>");
        PortletPreferences prefs = req.getPreferences();
        Map map = prefs.getMap();
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry me = (Map.Entry)iter.next();
            String key = (String)me.getKey();
            String[] values = (String [])me.getValue();
            if (values.length == 1) {
                writer.write("    Key: " + key + " Value: " + values[0] + "<br />");
            } else if (values.length > 1) {
                writer.write("    Key: " + key + " Value: " + values[0] + "<br />");
                for (int i=1; i < values.length; ++i) {
                    writer.write("       ");
                    for (int j=0; j < key.length(); ++j) {
                        writer.write(" ");
                    }
                    writer.write(" Value: " + values[i] + "<br />");
                }
            } else {
                writer.write("    Key: " + key + " Value: <br />");
            }
        }

        PortletURL url;
        url = res.createActionURL();
        url.setPortletMode(PortletMode.EDIT);

        writer.write("<form method=\"POST\" action=\"" + url.toString() + "\"><br />");
        writer.write("Update Preferences: <br />");
        writer.write("  Key: <input type=\"text\" name=\"name\" size=\"16\" maxlength=\"16\">");
        writer.write("  Value: <input type=\"text\" name=\"value\" size=\"32\" maxlength=\"32\">");
        writer.write("<br /><input type=\"submit\" value=\"Update Preferences\"/>");
        writer.write("</form>");
        writer.write("<table><tr><td>Change Portlet Mode:</td>");

        url = res.createRenderURL();
        url.setPortletMode(PortletMode.EDIT);
        this.writeLink(writer, url, "Edit");


        url = res.createRenderURL();
        url.setPortletMode(PortletMode.HELP);
        this.writeLink(writer, url, "Help");

        url = res.createRenderURL();
        url.setPortletMode(PortletMode.VIEW);
        this.writeLink(writer, url, "View");
        
        writer.write("</tr><tr><td>Change Window Mode:</td>");
        url = res.createRenderURL();
        url.setWindowState(WindowState.MINIMIZED);
        this.writeLink(writer, url, "Minimized");

        url = res.createRenderURL();
        url.setWindowState(WindowState.NORMAL);
        this.writeLink(writer, url, "Normal");

        url = res.createRenderURL();
        url.setWindowState(WindowState.MAXIMIZED);
        this.writeLink(writer, url, "Maximized");
        writer.write("</tr></table>");
    }

    protected void writeLink(Writer writer, PortletURL url, String text) 
    throws IOException {
        writer.write("<td><a href=\"");
        writer.write(url.toString());
        writer.write("\">");
        writer.write(text);
        writer.write("</a></td>");
    }
}
