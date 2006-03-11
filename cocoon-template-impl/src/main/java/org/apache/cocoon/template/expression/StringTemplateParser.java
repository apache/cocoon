/*
 * $Id$
 *
 * Created on 2005-09-06
 *
 * Copyright (c) 2005, MobileBox sp. z o.o.
 * All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.apache.cocoon.template.expression;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.List;
import java.io.Reader;

public interface StringTemplateParser {
    public static String ROLE = StringTemplateParser.class.getName();

    /*
    * Compile a boolean expression (returns either a Compiled Expression or a
    * Boolean literal)
    */
    JXTExpression compileBoolean(String val, String msg, Locator location) throws SAXException;

    /*
    * Compile an integer expression (returns either a Compiled Expression or an
    * Integer literal)
    */
    JXTExpression compileInt(String val, String msg, Locator location) throws SAXException;

    JXTExpression compileExpr(String inStr, String errorPrefix, Locator location) throws SAXParseException;

    /**
     * Parse a set of expressions spaced with literals
     */
    List parseSubstitutions(Reader in, String errorPrefix, Locator location) throws SAXParseException;

    List parseSubstitutions(Reader in) throws Exception;
}
