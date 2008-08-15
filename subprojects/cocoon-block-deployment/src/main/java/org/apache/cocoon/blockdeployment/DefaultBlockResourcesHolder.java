package org.apache.cocoon.blockdeployment;

import java.util.Map;

import javax.servlet.ServletContext;

public class DefaultBlockResourcesHolder implements BlockResourcesHolder,
        org.apache.cocoon.spring.configurator.BlockResourcesHolder {
    
    ServletContext servletContext;

    public Map getBlockContexts() {
        return (Map)servletContext.getAttribute(BlockDeploymentServletContextListener.BLOCK_CONTEXT_MAP);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

}
