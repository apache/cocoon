/*
 * Copyright 2004, Ugo Cei.
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.test;

import org.custommonkey.xmlunit.XMLTestCase;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;


/**
 * A superclass for testcases of sitemap components.
 * 
 * @version CVS $Id: SitemapComponentTestCase.java,v 1.3 2004/07/27 20:54:17 ugo Exp $
 */
public class SitemapComponentTestCase extends XMLTestCase {
    
    protected BeanFactory beanFactory;

    /**
     * @param name
     */
    public SitemapComponentTestCase(String name) {
        super(name);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        ClassPathResource res = new ClassPathResource("beans.xml"); 
        beanFactory = new XmlBeanFactory(res);
    }

    protected Object getBean(String name) {
        return beanFactory.getBean(name);
    }
}
