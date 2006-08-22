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
package org.apache.cocoon.woody.formmodel;

/**
 * The {@link WidgetDefinition} part of a Repeater widget, see {@link Repeater} for more information.
 * 
 * @version $Id$
 */
public class RepeaterDefinition extends AbstractContainerDefinition {
    private int initialSize = 0;

    public RepeaterDefinition(int initialSize) {
        super();
        this.initialSize = initialSize;
    }

    public Widget createInstance() {
        return new Repeater(this);
    }
    
    public int getInitialSize() {
        return this.initialSize;
    }
}
