/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.maven.test.jetty;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

public class JettyContainer {

    private static Server jetty;
    private int port;

    void start(String contextPath, String webappPath, int port) throws Exception {
        this.port = port;
        WebAppContext webContext = new WebAppContext(webappPath, contextPath);
        JettyContainer.jetty = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        JettyContainer.jetty.setConnectors(new Connector[] { connector });
        JettyContainer.jetty.addHandler(webContext);
        JettyContainer.jetty.start();
    }

    void stop() throws Exception {
        if (JettyContainer.jetty != null) {
            JettyContainer.jetty.stop();
        }
    }

    public int getPort() {
        return this.port;
    }

}