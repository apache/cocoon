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
package org.apache.cocoon.template.generation;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ExtendedSitemapComponentTestCase;
import org.apache.cocoon.template.tag.samples.DuplicateTag;
import org.apache.cocoon.xml.dom.DOMBuilder;

public class TemplateGeneratorTestCase extends ExtendedSitemapComponentTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);
    String docBase = "resource://org/apache/cocoon/template/generation/";
    TemplateGenerator generator;

    public void setUp() throws Exception {
        super.setUp();
        generator = new TemplateGenerator();
        generator.service(getManager());
        generator.enableLogging(logger);
    }

    public Logger getLogger() {
        return this.logger;
    }

    public void testGenerate() throws Exception {
        String inputURI = docBase + "TemplateGenerator-generate.xml";

        DOMBuilder builder = new DOMBuilder();
        generate(generator, inputURI, EMPTY_PARAMS, builder);

        assertEqual(load(inputURI), builder.getDocument());
    }

    public void testTag() throws Exception {
        String inputURI = docBase + "TemplateGenerator-tag.xml";
        String outputURI = docBase + "TemplateGenerator-tag-output.xml";

        generator.getTagRepository().registerTag("testns", "duplicate",
                DuplicateTag.class);
        DOMBuilder builder = new DOMBuilder();
        generate(generator, inputURI, EMPTY_PARAMS, builder);

        assertEqual(load(outputURI), builder.getDocument());
    }
}