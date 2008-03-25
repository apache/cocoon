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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.cocoon.el.Expression;
import org.apache.cocoon.el.impl.jxpath.JXPathExpression;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;

/**
 * @version $Id$
 */
public class JXTExpression implements Subst {

    private String raw;
    private Object compiledExpression;

    protected static final Iterator NULL_ITER = new Iterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException();
        }

        public void remove() {
            // EMPTY
        }
    };

    public JXTExpression(String raw, Object expr) {
        this.raw = raw;
        this.compiledExpression = expr;
    }

    public Object getCompiledExpression() {
        return compiledExpression;
    }

    public void setCompiledExpression(Object compiledExpression) {
        this.compiledExpression = compiledExpression;
    }

    public String getRaw() {
        return raw;
    }

    // Geting the value of the expression in various forms

    // Hack: try to prevent JXPath from converting result to a String
    public Object getNode(ObjectModel objectModel) throws Exception {
        Object compiled = getCompiledExpression();
        if (compiled instanceof Expression) {
            return ((Expression) compiled).getNode(objectModel);
        }

        return getRaw();
    }

    public Iterator getIterator(ObjectModel objectModel) throws Exception {
        Iterator iter;
        if (getCompiledExpression() != null || getRaw() != null) {
            if (getCompiledExpression() instanceof Expression) {
                iter = ((Expression) this.getCompiledExpression()).iterate(objectModel);
            } else {
                // literal value
                iter = new Iterator() {
                    Object val = this;

                    public boolean hasNext() {
                        return val != null;
                    }

                    public Object next() {
                        Object res = val;
                        if (res != null ) {
                            val = null;
                            return res;
                        }
                        throw new NoSuchElementException();
                    }

                    public void remove() {
                        // EMPTY
                    }
                };
            }
        } else {
            iter = NULL_ITER;
        }

        return iter;
    }

    public Object getValue(ObjectModel objectModel) throws Exception {
        Object compiled = getCompiledExpression();
        if (compiled != null) {
            if (compiled instanceof Expression) {
                return ((Expression) compiled).evaluate(objectModel);
            }

            return compiled;
        }

        return getRaw();
    }

    //
    // FIXME getXXXValue methods are all inconsistent. Is there a reason for this?
    //

    public Boolean getBooleanValue(ObjectModel objectModel) throws Exception {
        Object res = getValue(objectModel);
        return res instanceof Boolean ? (Boolean) res : null;
    }

    public String getStringValue(ObjectModel objectModel) throws Exception {
        Object res = getValue(objectModel);
        if (res != null) {
            return res.toString();
        }

        if (getCompiledExpression() == null) {
            return getRaw();
        }

        return null;
    }

    public Number getNumberValue(ObjectModel objectModel) throws Exception {
        Object res = getValue(objectModel);
        if (res instanceof Number) {
            return (Number) res;
        }

        if (res != null) {
            return Double.valueOf(res.toString());
        }

        return null;
    }

    public int getIntValue(ObjectModel objectModel) throws Exception {
        Object res = getValue(objectModel);
        return res instanceof Number ? ((Number) res).intValue() : 0;
    }

    public void setLenient(Boolean lenient) {
        if (this.compiledExpression instanceof Expression) {
            //TODO: hack! bases on particular expression implementation.
            ((Expression) this.compiledExpression).setProperty(JXPathExpression.LENIENT, lenient);
        }
    }
}
