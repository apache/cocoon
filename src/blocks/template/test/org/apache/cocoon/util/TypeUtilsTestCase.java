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
package org.apache.cocoon.util;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.container.ContainerTestCase;

public class TypeUtilsTestCase extends ContainerTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);

    public Logger getLogger() {
        return logger;
    }

    Object[] ones = new Object[] { new Byte("1"), Boolean.TRUE,
            new Double("1"), new Float("1"), new Integer("1"), new Long("1"),
            new Short("1"), new String("1") };

    Object[] zeroes = new Object[] { new Byte("0"), new Double("0"),
            new Float("0"), new Integer("0"), new Long("0"), new Short("0") };

    public void testConvert() {
        for (int i = 0; i < ones.length; i++) {
            for (int j = 0; j < ones.length; j++) {
                Class fromType = ones[i].getClass();
                Class toType = ones[j].getClass();

                //System.out.println(fromType.getName() + " -> " +
                // toType.getName());

                // Handle these cases separately
                if (fromType == Double.class && toType == String.class
                        || fromType == Float.class && toType == String.class
                        || fromType == String.class && toType == Boolean.class
                        || fromType == Boolean.class && toType == String.class) {
                    continue;
                }

                assertEquals(toType, TypeUtils.convert(ones[i], toType)
                        .getClass());
                assertEquals(ones[j], TypeUtils.convert(ones[i], toType));
            }
        }
        assertEquals("1.0", TypeUtils.convert(new Double("1"), String.class));
        assertEquals("1.0", TypeUtils.convert(new Float("1"), String.class));

    }

    public void testBooleanTrueSpecialCases() {
        assertEquals(Boolean.TRUE, TypeUtils.convert("true", Boolean.class));
        assertEquals("true", TypeUtils.convert(Boolean.TRUE, String.class));
    }

    public void testBooleanFalse() {
        for (int i = 0; i < zeroes.length; i++) {
            assertEquals(zeroes[i], TypeUtils.convert(Boolean.FALSE, zeroes[i]
                    .getClass()));
            assertEquals(Boolean.FALSE, TypeUtils.convert(zeroes[i],
                    Boolean.class));
        }

        assertEquals(Boolean.FALSE, TypeUtils.convert("false", Boolean.class));
        assertEquals("false", TypeUtils.convert(Boolean.FALSE, String.class));
    }

    public void testPrimitives() {
        assertTrue(1 == TypeUtils.toByte("1"));
        assertTrue(1 == TypeUtils.toDouble("1"));
        assertTrue(1 == TypeUtils.toFloat("1"));
        assertTrue(1 == TypeUtils.toInt("1"));
        assertTrue(1 == TypeUtils.toLong("1"));
        assertTrue(1 == TypeUtils.toShort("1"));
        assertTrue(true == TypeUtils.toBoolean("true"));
    }

    public void testPrimitivesNull() {
        assertTrue(0 == TypeUtils.toByte(null));
        assertTrue(0 == TypeUtils.toDouble(null));
        assertTrue(0 == TypeUtils.toFloat(null));
        assertTrue(0 == TypeUtils.toInt(null));
        assertTrue(0 == TypeUtils.toLong(null));
        assertTrue(0 == TypeUtils.toShort(null));
        assertTrue(false == TypeUtils.toBoolean(null));
    }

}

