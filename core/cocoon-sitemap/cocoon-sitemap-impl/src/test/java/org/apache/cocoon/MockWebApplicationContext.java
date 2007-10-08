package org.apache.cocoon;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class MockWebApplicationContext extends GenericApplicationContext implements WebApplicationContext {
    ServletContext sc;

    public MockWebApplicationContext(DefaultListableBeanFactory parent, ServletContext context) {
        super(parent);
        this.sc = context;
    }

    public MockWebApplicationContext(ServletContext context) {
        this.sc = context;
    }

    public ServletContext getServletContext() {
        return this.sc;
    }
}
