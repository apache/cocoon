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
import org.apache.cocoon.template.tag.AbstractTag;
import org.xml.sax.helpers.AttributesImpl;

public class ScriptCompilerTestCase extends ExtendedSitemapComponentTestCase {
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

    public void testParse() throws Exception {
        parse(docBase + "ScriptCompiler-parse.xml", compiler);
        Script script = compiler.getScript();
        assertEquals(2, script.size());
    }

    public void testElement() throws Exception {
        compiler.startElement("ns", "lname", "qname", new AttributesImpl());
        compiler.endElement("ns", "lname", "qname");
        Script script = compiler.getScript();

        assertEquals(1, script.size());

        PlainElementToken token = (PlainElementToken) script.get(0);
        assertEquals(0, token.getStart());
        assertEquals(1, token.getBodyStart());
        assertEquals(1, token.getEnd());
        assertEquals("ns", token.getNamespace());
        assertEquals("lname", token.getLName());
        assertEquals("qname", token.getQName());

    }

    public void testNestedElement() throws Exception {
        compiler.startElement("", "el0", "", new AttributesImpl());
        compiler.startElement("", "el1", "", new AttributesImpl());
        compiler.endElement("", "el1", "");
        compiler.endElement("", "el0", "");
        Script script = compiler.getScript();

        assertEquals(2, script.size());

        PlainElementToken token0 = (PlainElementToken) script.get(0);
        PlainElementToken token1 = (PlainElementToken) script.get(1);

        assertEquals("el0", token0.getLName());
        assertEquals(0, token0.getStart());
        assertEquals(2, token0.getEnd());

        assertEquals("el1", token1.getLName());
        assertEquals(1, token1.getStart());
        assertEquals(2, token1.getEnd());
    }

    public void testCharacters() throws Exception {
        compiler.characters("123".toCharArray(), 0, 3);
        Script script = compiler.getScript();

        assertEquals(1, script.size());

        CharactersToken token = (CharactersToken) script.get(0);
        assertEquals(0, token.getStart());
        assertEquals(1, token.getEnd());
        assertEquals("123", new String(token.getCharacters()));
    }

    public void testAttributes() throws Exception {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("ns", "lname", "qname", "CDATA", "value");

        compiler.startElement("", "element", "", attributes);
        compiler.endElement("", "element", "");

        Script script = compiler.getScript();
        assertEquals(3, script.size());

        PlainElementToken element = (PlainElementToken) script.get(0);
        assertEquals(0, element.getStart());
        assertEquals(3, element.getBodyStart());
        assertEquals(3, element.getEnd());

        AttributeToken attribute = (AttributeToken) script.get(1);
        assertEquals(1, attribute.getStart());
        assertEquals(3, attribute.getEnd());
        assertEquals("ns", attribute.getNamespace());
        assertEquals("lname", attribute.getLName());
        assertEquals("qname", attribute.getQName());

        CharactersToken characters = (CharactersToken) script.get(2);
        assertEquals(2, characters.getStart());
        assertEquals(3, characters.getEnd());
        assertEquals("value", new String(characters.getCharacters()));
    }

    public void testExpression() throws Exception {
        compiler.characters("${1}".toCharArray(), 0, 4);

        Script script = compiler.getScript();
        assertEquals(1, script.size());

        ExpressionToken token = (ExpressionToken) script.get(0);
        assertEquals(0, token.getStart());
        assertEquals(1, token.getEnd());
        assertNotNull(token.getExpression());
    }

    public void testTextAndExpression() throws Exception {
        compiler.characters("1${1}2".toCharArray(), 0, 6);

        Script script = compiler.getScript();
        assertEquals(3, script.size());

        assertTrue(script.get(0) instanceof CharactersToken);
        assertTrue(script.get(1) instanceof ExpressionToken);
        assertTrue(script.get(2) instanceof CharactersToken);
    }

    public void testTag() throws Exception {
        tagRepository.registerTag("ns", "dummy", DummyTag.class);
        Script script = compiler.getScript();

        compiler.startElement("ns", "dummy", "", new AttributesImpl());
        compiler.endElement("ns", "dummy", "");

        assertEquals(1, script.size());

        assertTrue(script.get(0) instanceof DummyTag);
    }

    public static class DummyTag extends AbstractTag {
        public void invoke(ScriptContext context) throws Exception {
        }
    }

}