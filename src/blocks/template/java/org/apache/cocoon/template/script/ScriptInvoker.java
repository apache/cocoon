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

import org.apache.cocoon.template.tag.Tag;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class ScriptInvoker {

    public static final String CDATA = "CDATA";

    Script script;

    ScriptContext context;

    XMLConsumer consumer;

    public ScriptInvoker(Script script, ScriptContext context) {
        this.script = script;
        this.context = context;
        this.consumer = context.getConsumer();
        context.setScriptInvoker(this);
    }

    public void invoke() throws Exception {
        consumer.startDocument();
        invoke(0, script.size());
        consumer.endDocument();
    }

    public void invoke(int start, int end) throws Exception {

        for (int i = start; i < end;) {
            Token tok = script.get(i);

            if (tok instanceof PlainElementToken) {
                PlainElementToken token = (PlainElementToken) tok;
                Attributes attributes = getElementAttributes(
                        token.getStart() + 1, token.getBodyStart());
                consumer.startElement(token.getNamespace(), token.getLName(),
                        token.getQName(), attributes);
                invoke(token.getBodyStart(), token.getEnd());
                consumer.endElement(token.getNamespace(), token.getLName(),
                        token.getQName());

            } else if (tok instanceof Tag) {
                ((Tag) tok).invoke(context);

            } else if (tok instanceof CharactersToken) {
                char[] characters = ((CharactersToken) tok).getCharacters();
                consumer.characters(characters, 0, characters.length);

            } else if (tok instanceof ExpressionToken) {
                char[] value = ((ExpressionToken) tok).getExpression()
                        .toCharArray(context);
                consumer.characters(value, 0, value.length);

            } else {
                throw new RuntimeException("Illegal token");
            }

            i = tok.getEnd();
        }
    }

    public Attributes getElementAttributes(int start, int end) throws Exception {
        AttributesImpl attributes = new AttributesImpl();

        for (int i = start; i < end;) {
            Token tok = script.get(i);

            if (tok instanceof AttributeToken) {
                AttributeToken token = (AttributeToken) tok;
                String value = getText(token.getStart() + 1, token.getEnd());
                attributes.addAttribute(token.getNamespace(), token.getLName(),
                        token.getQName(), CDATA, value);
                i = token.getEnd();
            } else {
                throw new RuntimeException("Illegal token");
            }
        }

        return attributes;
    }

    public String getText(int start, int end) throws Exception {
        StringBuffer text = new StringBuffer();

        for (int i = start; i < end; i++) {
            Token token = script.get(i);

            if (token instanceof CharactersToken)
                text.append(((CharactersToken) token).getCharacters());
            else if (token instanceof ExpressionToken)
                text.append(((ExpressionToken) token).getExpression().toString(
                        context));
            else
                throw new RuntimeException("Illegal token");
        }
        return text.toString();
    }
}