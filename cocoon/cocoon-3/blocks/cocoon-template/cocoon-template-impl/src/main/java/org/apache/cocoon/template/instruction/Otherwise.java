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
package org.apache.cocoon.template.instruction;

import java.util.Stack;

import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.event.StartElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Otherwise extends Instruction {
    public Otherwise(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        if (stack.size() != 0 && (stack.peek() instanceof Choose)) {
            Choose startChoose = (Choose) stack.peek();
            startChoose.setOtherwise(this);
        } else {
            throw new SAXParseException("<otherwise> must be within <choose>",
                                        getLocation(), null);
        }
    }
}
