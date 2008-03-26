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
package org.apache.cocoon.template;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.InstructionFactory;
import org.apache.cocoon.template.script.Parser;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.SAXException;

public class TemplateConsumer extends Parser implements XMLConsumer {
	private JXTemplateGenerator generator;
	private StringTemplateParser stringTemplateParser;
	private InstructionFactory instructionFactory;

	public JXTemplateGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(JXTemplateGenerator generator) {
		this.generator = generator;
	}

	public StringTemplateParser getStringTemplateParser() {
		return stringTemplateParser;
	}

	public void setStringTemplateParser(StringTemplateParser stringTemplateParser) {
		this.stringTemplateParser = stringTemplateParser;
	}

	public InstructionFactory getInstructionFactory() {
		return instructionFactory;
	}

	public void setInstructionFactory(InstructionFactory instructionFactory) {
		this.instructionFactory = instructionFactory;
	}

	public void initialize() {
		setParsingContext(new ParsingContext(stringTemplateParser, instructionFactory));
	}

	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
			throws ProcessingException, SAXException, IOException {
		generator.setup(resolver, objectModel, null, parameters);
	}

	public void endDocument() throws SAXException {
		super.endDocument();
		generator.performGeneration(getStartEvent(), null);
	}

	void setConsumer(XMLConsumer consumer) {
		generator.setConsumer(consumer);
	}
}