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
package org.apache.cocoon.template.jxtg.script.event;

import java.util.Iterator;
import java.util.Stack;

import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartImport extends StartInstruction {

    private final AttributeEvent uri;
    private final JXTExpression select;

    public StartImport(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        // <import uri="${root}/foo/bar.xml" context="${foo}"/>
        Locator locator = getLocation();
        Iterator iter = raw.getAttributeEvents().iterator();
        AttributeEvent uri = null;
        JXTExpression select = null;
        while (iter.hasNext()) {
            AttributeEvent e = (AttributeEvent) iter.next();
            if (e.getLocalName().equals("uri")) {
                uri = e;
                break;
            }
        }
        if (uri != null) {
            // If "context" is present then its value will be used
            // as the context object in the imported template
            String context = attrs.getValue("context");
            if (context != null) {
                select = JXTExpression.compileExpr(context, "import: \"context\": ", locator);
            }
        } else {
            throw new SAXParseException("import: \"uri\" is required", locator, null);
        }
        this.uri = uri;
        this.select = select;
    }

    public AttributeEvent getUri() {
        return uri;
    }

    public JXTExpression getSelect() {
        return select;
    }
}
