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
package org.apache.cocoon.template.v2.script;

import java.nio.CharBuffer;
import java.util.Stack;

import org.apache.cocoon.el.ExpressionCompiler;
import org.apache.cocoon.el.util.ELUtils;
import org.apache.cocoon.el.util.ParseHandler;
import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ScriptCompiler extends AbstractXMLConsumer {
    static final int TEXT = 1;
    static final int EXPRESSION = 2;
    Script script = new Script();
    TextParseHandler parseHandler = new TextParseHandler();
    Stack openedTokens = new Stack();
    TagRepository tagRepository;
    ExpressionCompiler expressionCompiler;

    public ScriptCompiler(TagRepository tagRepository,
            ExpressionCompiler expressionCompiler) {
        this.tagRepository = tagRepository;
        this.expressionCompiler = expressionCompiler;
    }

    public Script getScript() {
        return script;
    }

    public void startElement(String namespace, String lname, String qname,
            Attributes atts) throws SAXException {
        ElementToken token;

        if (tagRepository.contains(namespace, lname)) {
            token = tagRepository.getTag(namespace, lname);
        } else {
            token = new PlainElementToken();
            ((PlainElementToken) token).setup(namespace, lname, qname);
        }

        script.add(token);
        openedTokens.push(token);

        for (int i = 0; i < atts.getLength(); i++) {
            AttributeToken attrToken = new AttributeToken();
            attrToken.setup(atts.getURI(i), atts.getLocalName(i), atts
                    .getQName(i));
            script.add(attrToken);
            ELUtils.parse(CharBuffer.wrap(atts.getValue(i)), parseHandler);
            attrToken.setEnd(script.size());
        }

        token.setBodyStart(script.size());
    }

    public void endElement(String namespace, String lname, String qname)
            throws SAXException {
        Token token = (Token) openedTokens.pop();
        token.setEnd(script.size());
    }

    public void characters(char ch[], int start, int length)
            throws SAXException {
        ELUtils.parse(CharBuffer.wrap(ch, start, length), parseHandler);
    }

    public class TextParseHandler implements ParseHandler {
        public void handleExpression(CharSequence characters) {
            ExpressionToken token = new ExpressionToken();
            token.setup(expressionCompiler.compile(characters.toString()));
            script.addAtom(token);
        }

        public void handleText(CharSequence characters) {
            CharactersToken token = new CharactersToken();
            token.setup(characters);
            script.addAtom(token);
        }
    }
}