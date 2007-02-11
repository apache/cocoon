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
package org.apache.cocoon.forms.expression;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import org.outerj.expression.AbstractExpression;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;

/**
 * Sum function. This function returns the sum of all of its argument, but
 * it accepts Collections or Iterators as arguments. When it finds such an 
 * argument it iterates on all it's values, and try to sum them as well. It 
 * accepts String and any instance of Number.
 */
public class SumFunction extends AbstractExpression {

    public Object evaluate(ExpressionContext context) throws ExpressionException {
        BigDecimal result = new BigDecimal("0");
        for(int i = 0; i < arguments.size(); i++) {
            Expression function = (Expression)arguments.get(i);
            Object ret = function.evaluate(context);
            if (ret instanceof Collection) {
                ret = ((Collection)ret).iterator();
            }
            if (ret instanceof Iterator) {
               Iterator iter = (Iterator)ret;
               while (iter.hasNext()) {
                   Object p = iter.next();
                   BigDecimal db = null;
                   if (p instanceof BigDecimal) {
                       db =(BigDecimal)p;
                   } else if (p instanceof Long) {
                       db = new BigDecimal(((Long)p).longValue());
                   } else if (p instanceof Integer) {
                       db = new BigDecimal(((Integer)p).intValue());
                   } else if (p instanceof Double) {
                       db = new BigDecimal(((Double)p).doubleValue());
                   } else if (p instanceof Float) {
                       db = new BigDecimal(((Float)p).floatValue());
                   } else if (p instanceof BigInteger) {
                       db = new BigDecimal((BigInteger)p);
                   } else if (p instanceof Number) {
                       db = new BigDecimal(((Number)p).doubleValue());
                   } else if (p instanceof String) {
                       db = new BigDecimal((String)p);
                   } else {
                       throw new IllegalArgumentException("Cannot sum an argument of type " + p.getClass().getName());
                   }
                   result = result.add(db);
               }
            } else {
                result = result.add((BigDecimal)function.evaluate(context));
            }
        }
        return result;        
    }

    public Class getResultType() {
        return BigDecimal.class;
    }

    public String getDescription() {
        return "Summatory";
    }

}
