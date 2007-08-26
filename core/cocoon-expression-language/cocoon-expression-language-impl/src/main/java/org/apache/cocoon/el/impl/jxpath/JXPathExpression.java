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
package org.apache.cocoon.el.impl.jxpath;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.el.Expression;
import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.impl.jexl.JSIntrospector;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.Variables;
import org.mozilla.javascript.NativeArray;
import org.w3c.dom.Node;

/**
 * @version SVN $Id$
 */
public class JXPathExpression implements Expression {
    private final String language;
    private final String expression;
    private final CompiledExpression compiledExpression;
    private boolean lenient = false;

    public static final String LENIENT = "lenient";

    public JXPathExpression(String language, String expression)
        throws ExpressionException {
        this.language = language;
        this.expression = expression;
        this.compiledExpression = JXPathContext.compile(expression);
    }

    public Object evaluate(ObjectModel objectModel)
        throws ExpressionException{
        return this.compiledExpression.getValue(getContext(objectModel));
    }

    public Iterator iterate(ObjectModel objectModel)
        throws ExpressionException {
        final JXPathContext jxpathContext = getContext(objectModel);
        Object val =
            this.compiledExpression.getPointer(jxpathContext, this.expression).getNode();
        // FIXME: workaround for JXPath bug
        if (val instanceof NativeArray)
            return new JSIntrospector.NativeArrayIterator((NativeArray) val);
        else
            return new Iterator() {
                    Iterator iter =
                        compiledExpression.iteratePointers(jxpathContext);
                    
                    public boolean hasNext() {
                        return iter.hasNext();
                    }
                    
                    public Object next() {
                        return ((Pointer)iter.next()).getNode();
                    }
                    
                    public void remove() {
                        iter.remove();
                    }
                };
    }

    public void assign(ObjectModel objectModel, Object value)
        throws ExpressionException {
        this.compiledExpression.setValue(getContext(objectModel), value);
    }

    public String getExpression() {
        return this.expression;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setProperty(String property, Object value) {
        if (LENIENT.equals(property))
            this.lenient = ((Boolean)value).booleanValue();
    }

    // Hack: try to prevent JXPath from converting result to a String
    public Object getNode(ObjectModel objectModel) throws ExpressionException {
        Iterator iter =
            this.compiledExpression.iteratePointers(getContext(objectModel));
        if (iter.hasNext()) {
            Pointer first = (Pointer)iter.next();
            if (iter.hasNext()) {
                List result = new LinkedList();
                result.add(first.getNode());
                boolean dom = (first.getNode() instanceof Node);
                while (iter.hasNext()) {
                    Object obj = ((Pointer)iter.next()).getNode();
                    dom = dom && (obj instanceof Node);
                    result.add(obj);
                }
                Object[] arr;
                if (dom) {
                    arr = new Node[result.size()];
                } else {
                    arr = new Object[result.size()];
                }
                result.toArray(arr);
                return arr;
            }
            return first.getNode();                    
        }
        return null;
    }

    private JXPathContext getContext(ObjectModel objectModel) {
        // This could be made more efficient by caching the
        // JXPathContext within the Context object.
        
        JXPathContext jxobjectModel = JXPathContext.newContext(objectModel.get(ObjectModel.CONTEXTBEAN));
        jxobjectModel.setVariables(new VariableAdapter(objectModel));
        jxobjectModel.setLenient(this.lenient);
        jxobjectModel.setNamespaceContextPointer(new NamespacesTablePointer((NamespacesTable)objectModel.get(ObjectModel.NAMESPACE)));
        return jxobjectModel;
    }

    private static class VariableAdapter implements Variables {
        private ObjectModel objectModel;

        public VariableAdapter(ObjectModel objectModel) {
            this.objectModel = objectModel;
        }

        public void declareVariable(String name, Object value) {
            this.objectModel.put(name, value);
        }

        public Object getVariable(String name) {
            return this.objectModel.get(name);
        }

        public boolean isDeclaredVariable(String name) {
            return this.objectModel.containsKey(name);
        }

        public void undeclareVariable(String name) {
            throw new UnsupportedOperationException("Operation undeclareVariable is not supported");
        }
    }
}
