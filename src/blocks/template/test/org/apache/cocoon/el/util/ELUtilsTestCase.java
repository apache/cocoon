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
package org.apache.cocoon.el.util;

import java.util.ArrayList;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.container.ContainerTestCase;

public class ELUtilsTestCase extends ContainerTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
    TestParseHandler handler;

    public Logger getLogger() {
        return logger;
    }

    public void setUp() throws Exception {
        super.setUp();
        handler = new TestParseHandler();
    }

    public void testParse1() throws Exception {
        ELUtils.parse("123", handler);

        assertEquals(1, handler.texts.size());
        assertEquals(0, handler.expressions.size());

        assertEquals("123", handler.texts.get(0));
    }

    public void testParse2() throws Exception {
        ELUtils.parse("${expr}", handler);

        assertEquals(0, handler.texts.size());
        assertEquals(1, handler.expressions.size());

        assertEquals("expr", handler.expressions.get(0));
    }

    public void testParse3() throws Exception {
        ELUtils.parse("123${expr}234", handler);

        assertEquals(2, handler.texts.size());
        assertEquals(1, handler.expressions.size());

        assertEquals("123", handler.texts.get(0));
        assertEquals("expr", handler.expressions.get(0));
        assertEquals("234", handler.texts.get(1));
    }

    public void testParse4() throws Exception {
        try {
            ELUtils.parse("${expr", handler);
            fail("Should throw exception");
        } catch (Exception e) {
        }
    }

    public void testParse5() throws Exception {
        ELUtils.parse("${123\\}234}", handler);

        assertEquals(0, handler.texts.size());
        assertEquals(1, handler.expressions.size());

        assertEquals("123\\}234", handler.expressions.get(0));
    }

    public class TestParseHandler implements ParseHandler {
        public ArrayList texts = new ArrayList();

        public ArrayList expressions = new ArrayList();

        public void handleText(CharSequence characters) {
            texts.add(characters.toString());
        }

        public void handleExpression(CharSequence characters) {
            expressions.add(characters.toString());
        }

    }
}