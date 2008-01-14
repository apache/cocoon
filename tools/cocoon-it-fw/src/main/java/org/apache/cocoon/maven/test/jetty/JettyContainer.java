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