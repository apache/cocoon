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
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Parameter extends Instruction {
    final String name;
    final String optional;
    private final String defaultValue;

    public Parameter(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException {

        super(raw);

        Locator locator = getLocation();
        if (stack.size() == 0 || !(stack.peek() instanceof Define)) {
            throw new SAXParseException("<parameter> not allowed here", locator, null);
        } else {
            this.name = attrs.getValue("name");
            this.optional = attrs.getValue("optional");
            this.defaultValue = attrs.getValue("default");
            if (this.name == null) {
                throw new SAXParseException("parameter: \"name\" is required", locator, null);
            }
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
