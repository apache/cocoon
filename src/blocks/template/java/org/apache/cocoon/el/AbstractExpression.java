/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.el;

import org.apache.cocoon.util.TypeUtils;

public abstract class AbstractExpression implements Expression {
    public abstract Object evaluate(Context context);

    public Object evaluate(Context context, Class toType) {
        return TypeUtils.convert(evaluate(context), toType);
    }

    public boolean toBoolean(Context context) {
        return TypeUtils.toBoolean(evaluate(context));
    }

    public byte toByte(Context context) {
        return TypeUtils.toByte(evaluate(context));
    }

    public char toChar(Context context) {
        return TypeUtils.toChar(evaluate(context));
    }

    public char[] toCharArray(Context context) {
        return toString(context).toCharArray();
    }

    public double toDouble(Context context) {
        return TypeUtils.toDouble(evaluate(context));
    }

    public float toFloat(Context context) {
        return TypeUtils.toFloat(evaluate(context));
    }

    public int toInt(Context context) {
        return TypeUtils.toInt(evaluate(context));
    }

    public long toLong(Context context) {
        return TypeUtils.toLong(evaluate(context));
    }

    public short toShort(Context context) {
        return TypeUtils.toShort(evaluate(context));
    }

    public String toString(Context context) {
        String str = (String) evaluate(context, String.class);
        return str == null ? "" : str;
    }
}