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

public interface Expression {
    public Object evaluate(Context context);

    public Object evaluate(Context context, Class toType);

    public boolean toBoolean(Context context);

    public byte toByte(Context context);

    public char toChar(Context context);

    public char[] toCharArray(Context context);

    public double toDouble(Context context);

    public float toFloat(Context context);

    public int toInt(Context context);

    public long toLong(Context context);

    public short toShort(Context context);

    public String toString(Context context);
}