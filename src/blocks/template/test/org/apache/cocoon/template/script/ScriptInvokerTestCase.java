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
package org.apache.cocoon.template.script;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ExtendedSitemapComponentTestCase;
import org.apache.cocoon.el.GenericExpressionCompiler;
import org.apache.cocoon.template.v2.script.Script;
import org.apache.cocoon.template.v2.script.ScriptCompiler;
import org.apache.cocoon.template.v2.script.ScriptContext;
import org.apache.cocoon.template.v2.script.ScriptInvoker;
import org.apache.cocoon.template.v2.script.TagRepository;
import org.apache.cocoon.template.v2.tag.samples.DuplicateTag;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMBuilder;

public class ScriptInvokerTestCase extends ExtendedSitemapComponentTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
    String docBase = "resource://org/apache/cocoon/template/script/";
    ScriptCompiler compiler;
    TagRepository tagRepository;

    public void setUp() throws Exception {
        super.setUp();
        tagRepository = new TagRepository();
        tagRepository.enableLogging(getLogger());
        compiler = new ScriptCompiler(tagRepository, GenericExpressionCompiler
                .getInstance());
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void testElement() throws Exception {
        String inputURI = docBase + "ScriptInvoker-element.xml";
        parse(inputURI, compiler);
        Script script = compiler.getScript();

        DOMBuilder builder = new DOMBuilder();
        ScriptInvoker invoker = new ScriptInvoker(script,
                createContext(builder));
        invoker.invoke();
        assertEqual(load(inputURI), builder.getDocument());
    }

    public void testAttribute() throws Exception {
        String inputURI = docBase + "ScriptInvoker-attribute.xml";
        parse(inputURI, compiler);
        Script script = compiler.getScript();

        DOMBuilder builder = new DOMBuilder();
        ScriptInvoker invoker = new ScriptInvoker(script,
                createContext(builder));
        invoker.invoke();
        assertEqual(load(inputURI), builder.getDocument());
    }

    public void testTag() throws Exception {
        String inputURI = docBase + "ScriptInvoker-tag.xml";
        String outputURI = docBase + "ScriptInvoker-tag-output.xml";

        tagRepository.registerTag("testns", "duplicate", DuplicateTag.class);
        parse(inputURI, compiler);
        Script script = compiler.getScript();

        DOMBuilder builder = new DOMBuilder();
        ScriptInvoker invoker = new ScriptInvoker(script,
                createContext(builder));
        invoker.invoke();
        assertEqual(load(outputURI), builder.getDocument());
    }

    public void testExpression() throws Exception {
        String inputURI = docBase + "ScriptInvoker-expression.xml";
        String outputURI = docBase + "ScriptInvoker-expression-output.xml";

        parse(inputURI, compiler);
        Script script = compiler.getScript();

        DOMBuilder builder = new DOMBuilder();
        ScriptInvoker invoker = new ScriptInvoker(script,
                createContext(builder));
        invoker.invoke();
        assertEqual(load(outputURI), builder.getDocument());
    }

    public ScriptContext createContext(XMLConsumer consumer) {
        ScriptContext context = new ScriptContext();
        context.setConsumer(consumer);
        return context;
    }
}