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

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;

import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "order" tag
 *
 * This element has no attributes and contains a string describing the
 * print ordering (right then down, or down then right)
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @version $Id$
 */
public class EP_Order extends BaseElementProcessor {
    private NumericResult _print_order;

    /**
     * constructor
     */
    public EP_Order() {
        super(null);
        _print_order = null;
    }

    /**
     * @return print order (always one of the values in PrintOrder)
     *
     * @exception IOException
     */

    int getPrintOrder() throws IOException {
        if (_print_order == null) {
            _print_order = PrintOrder.extractPrintOrder(getData());
        }
        return _print_order.intValue();
    }
}   // end public class EP_Order
