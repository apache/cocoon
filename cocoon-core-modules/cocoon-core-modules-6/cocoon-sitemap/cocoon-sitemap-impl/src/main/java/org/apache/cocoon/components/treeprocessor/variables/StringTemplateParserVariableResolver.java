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

import java.util.Map;

import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.StringTemplateParser;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.template.expression.Substitutions;

/**
 * <p>{@link VariableResolver} that uses {@link StringTemplateParser} to resolve expressions.</p>
 *
 * @version $Id$
 */
public final class StringTemplateParserVariableResolver extends VariableResolver {

    public final static String ROLE = StringTemplateParserVariableResolver.class.getName();

    private StringTemplateParser stringTemplateParser;
    private ObjectModel objectModel;

    private Substitutions substitutions;


    public StringTemplateParserVariableResolver() {
        super();
    }

    public StringTemplateParser getStringTemplateParser() {
        return stringTemplateParser;
    }

    public void setStringTemplateParser(StringTemplateParser stringTemplateParser) {
        this.stringTemplateParser = stringTemplateParser;
    }

    public ObjectModel getObjectModel() {
        return objectModel;
    }

    public void setObjectModel(ObjectModel objectModel) {
        this.objectModel = objectModel;
    }

    public void setExpression(String expression) throws PatternException {
        this.originalExpr = expression;
        try {
            if (stringTemplateParser instanceof LegacySitemapStringTemplateParser)
                this.substitutions = new LegacySubstitutions((LegacySitemapStringTemplateParser) stringTemplateParser, null, expression);
            else
                this.substitutions = new Substitutions(stringTemplateParser, null, expression);
        } catch (Exception e) {
            throw new PatternException(e);
        }
    }

    public String resolve(InvokeContext context, Map objectModel) throws PatternException {
        try {
            if (this.substitutions instanceof LegacySubstitutions)
                return ((LegacySubstitutions) substitutions).toString(null, this.objectModel, context, objectModel);
            else
                return substitutions.toString(null, this.objectModel);
        } catch (Exception e) {
            throw new PatternException(e);
        }
    }
}
