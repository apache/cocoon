package org.apache.cocoon;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public class MockWebApplicationContext extends GenericApplicationContext implements WebApplicationContext {
	ServletContext sc;
	public MockWebApplicationContext(ConfigurableListableBeanFactory factory, ServletContext context) {
		super((DefaultListableBeanFactory) factory);
		this.sc = context;
	}

	public ServletContext getServletContext() {
		return this.sc;
	}
}
