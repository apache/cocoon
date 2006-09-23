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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.util.location.LocatedException;
import org.apache.cocoon.util.location.LocationUtils;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Parse XSP expressions. Expressions are embedded in attribute="value" and text elements and are
 * expanded by the
 * {@link org.apache.cocoon.components.language.markup.xsp.XSPMarkupLanguage.PreProcessFilter PreProcessFilter}
 * and have the form {#expression}. To prevent interpolation, use {##quote}, which results in the
 * text {#quote}.
 * An exception is thrown if the closing brace is missing.
 * <p>
 * The parser has a rudimentary understanding of expressions concerning
 * nested braces and braces inside quoted strings and character constants.
 * All valid Java, Javascript, and Python expressions can be used.
 * <p>
 * Example: &lt;h1&gt;Hello {#user.getName()}&lt;/h1&gt; &lt;img or
 * src=&quot;image_{#image.getId()}&quot;/&gt;
 * <p>
 *
 * @version SVN $Id$
 */
public class XSPExpressionParser {

    /**
     * Handler interface for parsed expressions and text fragments. The parser calls the handler to
     * process these.
     */
    public static interface Handler {
        public void handleText(char[] chars, int start, int length) throws SAXException;

        public void handleExpression(char[] chars, int start, int length) throws SAXException;
    }

    /**
     * Parser state.
     */
    protected static abstract class State {
        /**
         * Consume the next character
         * 
         * @param parser The parser
         * @param ch The character to consume
         * @throws SAXException If there is an error in the expression
         */
        public abstract void consume(XSPExpressionParser parser, char ch) throws SAXException;

        /**
         * Finish processing. Default behaviour is to throw an expression. States that are legal end
         * states must overwrite this method.
         * 
         * @param parser The parser
         * @throws SAXException It is illegal to finish processing in this state.
         */
        public void done(XSPExpressionParser parser) throws SAXException {
            throw new SAXException("Incomplete XSP expression {#"+parser.getExpression());
        }
    }

    /**
     * Parser state in a quoted string.
     */
    protected static class QuotedState extends State {
        private final char quote;

        /**
         * Create state to process quotes strings.
         *
         * @param quote The quote character to delimit strings
         */
        public QuotedState(char quote) {
            this.quote = quote;
        }

        /**
         * Consume the next character
         * 
         * @param parser The parser
         * @param ch The character to consume
         * @throws SAXException If there is an error in the expression
         */
        public void consume(XSPExpressionParser parser, char ch) throws SAXException {
            parser.append(ch);
            if (ch == quote && !parser.isEscaped())
                parser.setState(EXPRESSION_STATE);
            else if (ch == '\\')
                parser.setEscaped(!parser.isEscaped());
            else
                parser.setEscaped(false);
        }
    }

    /**
     * The parser is parsing text.
     */
    protected static final State TEXT_STATE = new State() {
        public void consume(XSPExpressionParser parser, char ch) throws SAXException {
            switch (ch) {
                case '{':
                    parser.setState(LBRACE_STATE);
                    break;

                default:
                    parser.append(ch);
            }
        }

        /**
         * Handle remaining text. It is legal to end in text mode.
         * 
         * @see State#done(XSPExpressionParser)
         */
        public void done(XSPExpressionParser parser) throws SAXException {
            parser.handleText();
        }
    };

    /**
     * The parser has encountered '{' in <code>{@link TEXT_STATE}</code>.
     */
    protected static final State LBRACE_STATE = new State() {
        public void consume(XSPExpressionParser parser, char ch) throws SAXException {
            switch (ch) {
                case '#':
                    parser.setState(TEXT_HASH_STATE);
                    break;

                default:
                    parser.append('{');
                    parser.append(ch);
                    parser.setState(TEXT_STATE);
            }
        }

        /**
         * Handle remaining text. It is legal to end text with '{'.
         * 
         * @see State#done(XSPExpressionParser)
         */
        public void done(XSPExpressionParser parser) throws SAXException {
            // Append the pending '{'
            parser.append('{');
            parser.handleText();
        }
    };

    /**
     * The parser has encountered '#' in <code>{@link LBRACE_STATE}</code>.
     */
    protected static final State TEXT_HASH_STATE = new State() {
        public void consume(XSPExpressionParser parser, char ch) throws SAXException {
            switch (ch) {
                case '#':
                    parser.append('{');
                    parser.append('#');
                    parser.setState(TEXT_STATE);
                    break;

                default:
                    parser.handleText();
                    parser.initExpression();
                    parser.setState(EXPRESSION_STATE);
                    EXPRESSION_STATE.consume(parser, ch);
            }
        }
    };

    /**
     * The parser is parsing an expression.
     */
    protected static final State EXPRESSION_STATE = new State() {
        public void consume(XSPExpressionParser parser, char ch) throws SAXException {
            switch (ch) {
                case '{':
                    parser.incrNesting();
                    parser.append(ch);
                    break;

                case '}':
                    if (parser.decrNesting() > 0) {
                        parser.append(ch);
                    }
                    else {
                        parser.handleExpression();
                        parser.setState(TEXT_STATE);
                    }
                    break;

                case '"':
                    parser.append(ch);
                    parser.setState(EXPRESSION_STRING_STATE);
                    break;

                case '\'':
                    parser.append(ch);
                    parser.setState(EXPRESSION_CHAR_STATE);
                    break;

                case '´':
                    parser.append(ch);
                    parser.setState(EXPRESSION_SHELL_STATE);
                    break;

                default:
                    parser.append(ch);
            }
        }
    };

    /**
     * The parser has encountered '"' in <code>{@link EXPRESSION_STATE}</code>
     * to start a string constant.
     */
    protected static final State EXPRESSION_STRING_STATE = new QuotedState('"');

    /**
     * The parser has encountered '\'' in <code>{@link EXPRESSION_STATE}</code>
     * to start a character constant.
     */
    protected static final State EXPRESSION_CHAR_STATE = new QuotedState('\'');

    /**
     * The parser has encountered '`' (Backtick, ASCII 0x60) in
     * <code>{@link EXPRESSION_STATE}</code> to start a Python string constant.
     */
    protected static final State EXPRESSION_SHELL_STATE = new QuotedState('`');

    /**
     * The parser state
     */
    private State state = TEXT_STATE;

    /**
     * The nesting level of braces.
     */
    private int nesting = 0;

    /**
     * Flag whether previous character was a backslash to escape quotes.
     */
    private boolean escaped = false;

    /**
     * The handler for parsed text and expression fragments.
     */
    private Handler handler;

    /**
     * The buffer for the current text or expression fragment. We do our own StringBuffer here to
     * save some allocations of char arrays for the handler.
     */
    private char[] buf = new char[256];

    /**
     * The current size of the fragment in the buffer.
     */
    private int bufSize;

    /**
     * The delty by which the buffer grows if it is too small.
     */
    private int bufGrow = 256;

    /**
     * Create a new <code>{@link XSPExpressionParser}</code>.
     * 
     * @param handler The handler for parsed text and expression fragments.
     */
    public XSPExpressionParser(Handler handler) {
        this.handler = handler;
    }

    /**
     * Parses a character sequence.
     * 
     * @param chars The character sequence to parse
     * @throws SAXException If there is an error in the sequence.
     */
    public void consume(CharSequence chars) throws SAXException {
        int end = chars.length();

        for (int i = 0; i < end; ++i) {
            char ch = chars.charAt(i);
            state.consume(this, ch);
        }
    }

    /**
     * Parses part of a character array.
     * 
     * @param chars The characters
     * @param start The start position in the character array
     * @param length The number of characters to parse
     * @throws SAXException If there is an error in the sequence.
     */
    public void consume(char[] chars, int start, int length) throws SAXException {
        int end = start + length;

        for (int i = start; i < end; ++i) {
            char ch = chars[i];
            state.consume(this, ch);
        }
    }

    /**
     * Flushes the parser
     * 
     * @param locator The SAX locator to determine the current parse position
     * @param description The description of the current parse context
     * @throws SAXException If there is an error in the parsed text.
     *         A wrapped LocatedException contains the location of the parse error.
     */
    public void flush(Locator locator, String description) throws SAXException {
        try {
            state.done(this);
            bufSize = 0;
            state = TEXT_STATE;
        }
        catch(SAXException ex) {
            throw new SAXException(new LocatedException(ex.getMessage(), ex,
                                                        LocationUtils.getLocation(locator, description)));
        }
    }

    protected State getState() {
        return state;
    }

    protected void setState(State state) {
        this.state = state;
    }

    protected void initExpression() {
        nesting = 1;
        escaped = false;
    }

    protected void incrNesting() {
        nesting++;
    }

    protected int decrNesting() {
        return --nesting;
    }

    protected void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }

    protected boolean isEscaped() {
        return escaped;
    }

    protected String getExpression() {
        return new String(buf, 0, bufSize);
    }

    protected void handleText() throws SAXException {
        if (bufSize > 0) {
            handler.handleText(buf, 0, bufSize);
            bufSize = 0;
        }
    }

    protected void handleExpression() throws SAXException {
        if (bufSize == 0) {
            throw new SAXException("Illegal empty expression.");
        }

        handler.handleExpression(buf, 0, bufSize);

        bufSize = 0;
    }

    protected void append(char ch) {
        if (bufSize + 1 >= buf.length) {
            char[] newBuf = new char[buf.length + bufGrow];
            System.arraycopy(buf, 0, newBuf, 0, buf.length);
            buf = newBuf;
        }

        buf[bufSize] = ch;
        ++bufSize;
    }
}
