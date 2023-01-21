package org.apache.cocoon;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A basic Spring test case.
 */
public abstract class BaseSpringTest extends TestCase {

    /**
     * Spring {@link BeanFactory}.
     */
    protected BeanFactory factory;

    /**
     * Returns the Spring config file name.
     * 
     * @return Config file name.
     */
    protected abstract String getSpringConfigFile();

    /**
     * Bring up Spring IoC container.
     */
    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext context = new ClassPathXmlApplicationContext(
                getClass().getResource(getSpringConfigFile()).toExternalForm());
        factory = context;
    }

    /**
     * Tear down disposes Spring container.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        factory = null;
    }

}