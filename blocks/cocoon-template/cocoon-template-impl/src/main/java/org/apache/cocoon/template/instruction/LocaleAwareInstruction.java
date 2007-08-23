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

import java.util.Locale;
import java.util.Stack;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.environment.ValueHelper;
import org.apache.cocoon.template.script.event.StartElement;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class LocaleAwareInstruction extends Instruction {
    private Subst locale;

    public LocaleAwareInstruction(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) throws SAXException {
        super(raw);
        this.locale = parsingContext.getStringTemplateParser().compileExpr(attrs.getValue("locale"), null, getLocation());
    }

    protected Locale getLocale(ObjectModel objectModel) throws Exception {
        Object locVal = null;
        if (this.locale != null) {
            locVal = this.locale.getValue(objectModel);
            if (locVal == null)
                locVal = this.locale.getStringValue(objectModel);
        }

        if (locVal != null)
            return (locVal instanceof Locale ? (Locale) locVal : ValueHelper.parseLocale(locVal.toString(), null));
        else
            return Locale.getDefault();
    }
}
