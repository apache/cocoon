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
package org.apache.cocoon.components.serializers.encoding;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import org.junit.Test;

public class XMLEncoderTest {

    private final XMLEncoder encoder = new XMLEncoder();

    /**
     * Test COCOON-2352: XMLEncoder doesn't support Unicode surrogate pairs.
     */
    @Test
    public void encodingSurrogatePairs() {
        char[] expectedValue = "&#x1F340;".toCharArray();

        assertTrue(encoder.encode('\uD83C').length == 0);
        assertTrue(Arrays.equals(expectedValue, encoder.encode('\uDF40')));
    }
}
