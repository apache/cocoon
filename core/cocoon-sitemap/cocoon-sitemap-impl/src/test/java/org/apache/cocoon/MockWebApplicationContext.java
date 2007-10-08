package org.apache.cocoon;

import javax.servlet.ServletContext;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class MockWebApplicationContext extends GenericApplicationContext implements WebApplicationContext {
    ServletContext sc;

    public MockWebApplicationContext(ServletContext context) {
        this.sc = context;
    }

    public ServletContext getServletContext() {
        return this.sc;
    }
}
