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
package org.apache.cocoon.template.jxtg;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.environment.ObjectModelHelper;

public class JXTemplateGeneratorTestCase extends SitemapComponentTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
    String docBase = "resource://org/apache/cocoon/template/jxtg/";
    String JX = "jx";

    public Logger getLogger() {
        return this.logger;
    }

    public void testGenerate() throws Exception {
        String inputURI = docBase + "JXTemplateGenerator-generate.xml";

        assertEqual(load(inputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testJexlExpression() throws Exception {
        String inputURI = docBase + "JXTemplateGenerator-jexlExpression.xml";
        String outputURI = docBase + "JXTemplateGenerator-jexlExpression-output.xml";

        String protocol = ObjectModelHelper.getRequest(getObjectModel()).getProtocol();
        assertEquals("HTTP/1.1", protocol);

        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        assertEqual(load(outputURI), generate(JX, inputURI, parameters));
    }

    public void testJexlEnvExpression() throws Exception {
        String inputURI = docBase + "JXTemplateGenerator-jexlEnvExpression.xml";
        String outputURI = docBase + "JXTemplateGenerator-jexlEnvExpression-output.xml";

        String protocol = ObjectModelHelper.getRequest(getObjectModel()).getProtocol();
        assertEquals("HTTP/1.1", protocol);

        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        assertEqual(load(outputURI), generate(JX, inputURI, parameters));
    }

    public void testJXPathExpression() throws Exception {
        String inputURI = docBase + "JXTemplateGenerator-jxpathExpression.xml";
        String outputURI = docBase + "JXTemplateGenerator-jxpathExpression-output.xml";

        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        assertEqual(load(outputURI), generate(JX, inputURI, parameters));
    }

    public void testJXPathEnvExpression() throws Exception {
        String inputURI = docBase + "JXTemplateGenerator-jxpathEnvExpression.xml";
        String outputURI = docBase + "JXTemplateGenerator-jxpathEnvExpression-output.xml";

        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        assertEqual(load(outputURI), generate(JX, inputURI, parameters));
    }
}
