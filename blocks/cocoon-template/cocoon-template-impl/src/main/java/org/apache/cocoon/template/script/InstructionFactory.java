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
package org.apache.cocoon.template.script;

import java.util.Stack;

import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.instruction.Instruction;
import org.apache.cocoon.template.script.event.StartElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public interface InstructionFactory {

    String ROLE = InstructionFactory.class.getName();

    boolean isInstruction(StartElement element);

    Instruction createInstruction(ParsingContext parsingContext, StartElement startElement,
            Attributes attrs, Stack stack) throws SAXException;
}