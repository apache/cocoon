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

import javax.servlet.ServletContext;

import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerFactory;
import org.springframework.web.context.ServletContextAware;

/**
 * @version $Id$
 */
public class PortalStartup
    extends AbstractBean
    implements ServletContextAware {

    protected ServletContext servletContext;

    protected String uniqueContainerName = "cocoon-portal";

    public static final String CONTAINER_KEY = PortalStartup.class.getName() + "/Container";

    /**
     */
    public void init() {
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Starting up Pluto Portal Driver...");
        }
        this.initContainer();
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("********** Pluto Portal Driver Started **********\n\n");
        }
    }

    /**
     */
    public void destroy() {
        if (this.getLogger().isInfoEnabled()) {
            this.getLogger().info("Shutting down Pluto Portal Driver...");
        }
        this.destroyContainer();
        if (this.getLogger().isInfoEnabled()) {
            this.getLogger().info("********** Pluto Portal Driver Shut Down **********\n\n");
        }
    }

    /**
     * Initializes the portlet container. This method constructs and initializes
     * the portlet container, and saves it to the servlet context scope.
     * @param servletContext  the servlet context.
     */
    private void initContainer() {
        try {
            // Create container services.
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Creating container services...");
            }
            ContainerServicesImpl containerServices = new ContainerServicesImpl();

            // Create portlet container.
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Creating portlet container...");
            }
            PortletContainerFactory factory =
         		PortletContainerFactory.getInstance();
            PortletContainer container = factory.createContainer(
                    this.uniqueContainerName,
                    containerServices,
                    containerServices);

            // Initialize portlet container.
            if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("Initializing portlet container...");
            }
            container.init(this.servletContext);

            // Save portlet container to the servlet context scope.
            this.servletContext.setAttribute(CONTAINER_KEY, container);
            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("Pluto portlet container started.");
            }

        } catch (PortletContainerException ex) {
            this.getLogger().error("Unable to start up portlet container: "
            		+ ex.getMessage(), ex);
        }
    }

    /**
     * Destroyes the portlet container and removes it from servlet context.
     * @param servletContext  the servlet context.
     */
    private void destroyContainer() {
        if (this.getLogger().isInfoEnabled()) {
            this.getLogger().info("Shutting down Pluto Portal Driver...");
        }
        PortletContainer container = (PortletContainer)
                servletContext.getAttribute(CONTAINER_KEY);
        if (container != null) {
            try {
                container.destroy();
                if (this.getLogger().isInfoEnabled()) {
                    this.getLogger().info("Pluto Portal Driver shut down.");
                }
            } catch (PortletContainerException ex) {
                this.getLogger().error("Unable to shut down portlet container: "
                        + ex.getMessage(), ex);
            } finally {
                servletContext.removeAttribute(CONTAINER_KEY);
            }
        }
    }

    /**
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }
}

