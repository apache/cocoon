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
package org.apache.cocoon.template.jxtg;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.el.objectmodel.ObjectModel;

/**
 * @version SVN $Id$
 */
public class JXTemplateGeneratorTestCase extends SitemapComponentTestCase {
    String docBase = "resource://org/apache/cocoon/template/jxtg/";
    String JX = "jx";
    Map flowContext = new HashMap();
    ObjectModel newObjectModel;

    public class StringContainer {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public StringContainer(String value) {
            this.value = value;
        }
    }

    public void setUp() throws Exception {
        super.setUp();

        newObjectModel = (ObjectModel) getBeanFactory().getBean(ObjectModel.ROLE);
    }

    public Map getFlowContext() {
        return this.flowContext;
    }

    public void addFlowContextToObjectModel(ObjectModel newObjectModel) {
        FlowHelper.setContextObject(getObjectModel(), newObjectModel, flowContext);
    }

    public ObjectModel getNewObjectModel() {
        return this.newObjectModel;
    }

    public void testGenerate() throws Exception {
        String inputURI = docBase + "generate.xml";

        assertEqual(load(inputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testJexlExpression() throws Exception {
        String inputURI = docBase + "jexlExpression.xml";
        String outputURI = docBase + "jexlExpression-output.xml";

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testJexlEnvExpression() throws Exception {
        String inputURI = docBase + "jexlEnvExpression.xml";
        String outputURI = docBase + "jexlEnvExpression-output.xml";

        getNewObjectModel().markLocalContext();
        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        getFlowContext().put("test", "bar");
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, parameters));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testJXPathExpression() throws Exception {
        String inputURI = docBase + "jxpathExpression.xml";
        String outputURI = docBase + "jxpathExpression-output.xml";

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testJXPathEnvExpression() throws Exception {
        String inputURI = docBase + "jxpathEnvExpression.xml";
        String outputURI = docBase + "jxpathEnvExpression-output.xml";

        getNewObjectModel().markLocalContext();
        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        getFlowContext().put("test", "bar");
        addFlowContextToObjectModel(getNewObjectModel());

        assertEquals("HTTP/1.1", getRequest().getProtocol());
        assertEqual(load(outputURI), generate(JX, inputURI, parameters));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testJXChoose() throws Exception {
        String inputURI = docBase + "jxChoose.xml";
        String outputURI = docBase + "jxChoose-output.xml";

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testJXForEach() throws Exception {
        String inputURI = docBase + "jxForEach.xml";
        String outputURI = docBase + "jxForEach-output.xml";

        getNewObjectModel().markLocalContext();
        String[] array = { "one", "two", "three" };
        getFlowContext().put("test", array);
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testJXMacro() throws Exception {
        String inputURI = docBase + "jxMacro.xml";
        String outputURI = docBase + "jxMacro-output.xml";

        getNewObjectModel().markLocalContext();
        getFlowContext().put("container", new StringContainer("foobar"));
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testJXDynamicMacro() throws Exception {
        String inputURI = docBase + "jxDynamicMacro.xml";
        String outputURI = docBase + "jxDynamicMacro-output.xml";

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testJXSet() throws Exception {
        String inputURI = docBase + "jxSet.xml";
        String outputURI = docBase + "jxSet-output.xml";

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
    }

    public void testAttribute() throws Exception {
        String inputURI = docBase + "jxAttribute.xml";
        String outputURI = docBase + "jxAttribute-output.xml";

        getNewObjectModel().markLocalContext();
        Calendar cal = new GregorianCalendar(1979, 0, 1, 10, 21, 33);
        getFlowContext().put("date", cal.getTime());
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testFormatDate() throws Exception {
        String inputURI = docBase + "formatDate.xml";
        String outputURI = docBase + "formatDate-output.xml";

        getNewObjectModel().markLocalContext();
        Calendar cal = new GregorianCalendar(1979, 0, 1, 10, 21, 33);
        getFlowContext().put("date", cal.getTime());
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testFormatNumber() throws Exception {
        String inputURI = docBase + "formatNumber.xml";
        String outputURI = docBase + "formatNumber-output.xml";

        getNewObjectModel().markLocalContext();
        getFlowContext().put("value", new Double(979.0101));
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testOut() throws Exception {
        String inputURI = docBase + "jxOut.xml";
        String outputURI = docBase + "jxOut-output.xml";
        String includeURI = docBase + "jxOutInclude.xml";

        getNewObjectModel().markLocalContext();
        getFlowContext().put("value", "simple");
        getFlowContext().put("xml", "<root><node>value</node></root>");
        getFlowContext().put("document", load(includeURI));
        addFlowContextToObjectModel(getNewObjectModel());

        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));

        getNewObjectModel().cleanupLocalContext();
    }

    public void testImport() throws Exception {
        String inputURI = docBase + "jxImport.xml";
        String outputURI = docBase + "jxImport-output.xml";
        String importURI = docBase + "jxImportChild.xml";

        getNewObjectModel().markLocalContext();
        getFlowContext().put("importURI", importURI);
        addFlowContextToObjectModel(getNewObjectModel());
        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
        getNewObjectModel().cleanupLocalContext();
    }

    public void testPrefixMapping() throws Exception {
        String inputURI = docBase + "jxPrefixMapping.xml";
        String outputURI = docBase + "jxPrefixMapping-output.xml";
        getNewObjectModel().markLocalContext();
        assertEqual(load(outputURI), generate(JX, inputURI, EMPTY_PARAMS));
        getNewObjectModel().cleanupLocalContext();
    }
}
