/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.butterfly.components.pipeline.impl;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import junit.framework.TestCase;

import org.apache.butterfly.xml.WhitespaceFilter;
import org.apache.butterfly.xml.dom.DOMBuilder;
import org.custommonkey.xmlunit.XMLUnit;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.xml.sax.InputSource;

/**
 * Description of GroovySitemapTestCase.
 * 
 * @version CVS $Id: GroovySitemapTestCase.java,v 1.1 2004/07/27 20:54:17 ugo Exp $
 */
public class GroovySitemapTestCase extends TestCase {
    
    private BeanFactory beanFactory;

    /**
     * @param name
     */
    public GroovySitemapTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        ClassPathResource res = new ClassPathResource("beans.xml"); 
        beanFactory = new XmlBeanFactory(res);
    }

    public void testGroovySitemap() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        Class pipelineClass = loader.parseClass(getClass().getResourceAsStream("Pipeline.groovy"));
        Class myPipelineClass = loader.parseClass(getClass().getResourceAsStream("MyPipeline.groovy"));
        GroovyObject pipeline = (GroovyObject) myPipelineClass.newInstance();
        pipeline.setProperty("beanFactory", beanFactory);
        Object[] args = { "index.html" };
        pipeline.invokeMethod("setup", args);
        DOMBuilder builder = new DOMBuilder();
        pipeline.invokeMethod("process", new Object[] { null, new WhitespaceFilter(builder) });
        assertTrue("Output from pipeline does not match control file.",
                XMLUnit.compareXML(
                        XMLUnit.buildControlDocument(new InputSource("testdata/traxtest-result.xml")),
                        builder.getDocument()).similar());
    }
}
