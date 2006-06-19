/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.expression;

import java.util.Iterator;

/**
 * @version $Id$
 */
public abstract class AbstractExpression implements Expression {

    private String language;
    private String expression;

    public AbstractExpression(String language, String expression) {
        this.language = language;
        this.expression = expression;
    }

    public String getExpression() {
        return this.expression;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setProperty(String property, Object value) {
        // has no properties
    }

    protected static final Iterator EMPTY_ITER = new Iterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            return null;
        }

        public void remove() {
            // EMPTY
        }
    };

}
