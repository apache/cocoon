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
package org.apache.cocoon;

import java.net.URLEncoder;

/**
 * Check basic TraversableGenerator functionality.
 *
 * @version $Id: $
 */
public class WebdavStep3TestCase
    extends HtmlUnitTestCase
{
    static final String pageurl = "/samples/blocks/webdav/";
    /**
     * Testing basic TraversableGenerator functionality.
     */
    public void testTraversableGenerator()
        throws Exception
    {
        loadXmlPage(pageurl+"step1/repo/");
        addNamespace("collection", "http://apache.org/cocoon/collection/1.0");

        // FIXME: why XPath namespaces not working?

        final String xpathName = "/collection:collection/collection:resource/@name";
        String name = evalXPath(xpathName);
        if( name.length() != 0 ) {
            logger.info("Good, XPath namespaces finally working");
            assertXPath(xpathName, "contentA.xml");
        }
        else {
            logger.info("Damnit, XPath namespaces still not working");
            assertXPath("/*[name(.)='collection:collection']/*[name(.)='collection:resource']/@name", "contentA.xml");
        }
    }

    public void testContentB()
        throws Exception
    {
        final String step3url = pageurl+"step3/repo/dir2/contentB.xml";

        final String xpathTitle = "/html/body/form/p/input[@name='title']/@value";
        final String xpathPara1 = "(/html/body/form/p/textarea)[1]";
        final String xpathPara2 = "(/html/body/form/p/textarea)[2]";
        final String xpathAction = "/html/body/form/@action";

        final String xpathSuccess = "/page/sourceResult/execution";
        final String valueSuccess = "success";

        final String oldTitle = "Content B";
        final String oldPara1 = "First Paragraph";
        final String oldPara2 = "Second Paragraph";

        final String newTitle = "Title changed by WebdavStep3TestCase";
        final String newPara1 = "test1";
        final String newPara2 = "test2";

        // Check contents of contentB.xml

        loadHtmlPage(step3url);
        //assertXPath(xpathTitle, oldTitle);
        //assertXPath(xpathPara1, oldPara1);
        //assertXPath(xpathPara2, oldPara2);
        String action1 = evalXPath(xpathAction);

        // Change contents of contentB.xml

        loadXmlPage(action1+"?title="+URLEncoder.encode(newTitle)+"&para="+URLEncoder.encode(newPara1)+"&para="+URLEncoder.encode(newPara2));
        assertXPath(xpathSuccess, valueSuccess);

        // Check changes

        Thread.sleep(1000);
        loadHtmlPage(step3url);
        assertXPath(xpathTitle, newTitle);
        assertXPath(xpathPara1, newPara1);
        assertXPath(xpathPara2, newPara2);
        String action2 = evalXPath(xpathAction);

        // Undo changes

        loadXmlPage(action2+"?title="+URLEncoder.encode(oldTitle)+"&para="+URLEncoder.encode(oldPara1)+"&para="+URLEncoder.encode(oldPara2));
        assertXPath(xpathSuccess, valueSuccess);

        // Check success of undo

        Thread.sleep(1000);
        loadHtmlPage(step3url);
        assertXPath(xpathTitle, oldTitle);
        assertXPath(xpathPara1, oldPara1);
        assertXPath(xpathPara2, oldPara2);
    }
}
