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
package org.apache.cocoon.template.expression;

import java.io.CharArrayReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.el.parsing.Subst;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public class Substitutions {

    private final List substitutions;
    private final boolean hasSubstitutions;

    public Substitutions(StringTemplateParser stringTemplateParser, Locator location, String stringTemplate) throws SAXException {
        this(stringTemplateParser, location, new StringReader(stringTemplate));
    }

    public Substitutions(StringTemplateParser stringTemplateParser, Locator location, char[] chars, int start, int length)
            throws SAXException {
        this(stringTemplateParser, location, new CharArrayReader(chars, start, length));
    }

    private Substitutions(StringTemplateParser stringTemplateParser, Locator location, Reader in) throws SAXException {
        this.substitutions = stringTemplateParser.parseSubstitutions(in, "", location);
        this.hasSubstitutions = !substitutions.isEmpty();
    }

    public boolean hasSubstitutions() {
        return this.hasSubstitutions;
    }

    public Iterator iterator() {
        return this.substitutions.iterator();
    }

    public int size() {
        return this.substitutions.size();
    }

    public Object get(int pos) {
        return this.substitutions.get(pos);
    }

    public String toString(Locator location, ObjectModel objectModel) throws SAXException {
        StringBuffer buf = new StringBuffer();

        Iterator i = iterator();
        while (i.hasNext()) {
            Subst subst = (Subst) i.next();

            Object val;
            try {
                val = subst.getValue(objectModel);
            } catch (Exception e) {
                throw new SAXParseException(e.getMessage(), location, e);
            //FIXME: Don't catch java.lang.Error
            //} catch (Error err) {
            //    throw new SAXParseException(err.getMessage(), location, new ErrorHolder(err));
            }

            if (val != null) {
                buf.append(val.toString());
            }
        }

        return buf.toString();
    }
}
