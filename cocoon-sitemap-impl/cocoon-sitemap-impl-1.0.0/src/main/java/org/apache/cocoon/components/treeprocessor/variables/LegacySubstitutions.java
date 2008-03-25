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
package org.apache.cocoon.components.treeprocessor.variables;

import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.expression.Substitutions;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public class LegacySubstitutions extends Substitutions {

    public LegacySubstitutions(LegacySitemapStringTemplateParser stringTemplateParser, Locator location, String stringTemplate) throws SAXException {
        super(stringTemplateParser, location, stringTemplate);
    }

    public String toString(Locator location, ObjectModel objectModel) throws SAXException {
        throw new UnsupportedOperationException();
    }

    public String toString(Locator location, ObjectModel objectModel, InvokeContext context, Map oldObjectModel) throws SAXParseException {
        StringBuffer buf = new StringBuffer();

        Iterator i = iterator();
        while (i.hasNext()) {
            Subst subst = (Subst) i.next();

            Object val;
            try {
                if (subst instanceof LegacySitemapStringTemplateParser.SitemapExpressionSubstitution)
                    val = ((LegacySitemapStringTemplateParser.SitemapExpressionSubstitution) subst).getStringValue(context, oldObjectModel);
                else
                    val = subst.getValue(objectModel);
            } catch (Exception e) {
                throw new SAXParseException(e.getMessage(), location, e);
            }

            if (val != null) {
                buf.append(val.toString());
            }
        }
        
        return buf.toString();
    }
}
