/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.util.Stack;

import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.instruction.Instruction;
import org.apache.cocoon.template.script.event.StartElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public interface InstructionFactory {
    public final static String ROLE = InstructionFactory.class.getName();

    public abstract boolean isInstruction(StartElement element);

    public abstract Instruction createInstruction(ParsingContext parsingContext, StartElement startElement,
            Attributes attrs, Stack stack) throws SAXException;
}