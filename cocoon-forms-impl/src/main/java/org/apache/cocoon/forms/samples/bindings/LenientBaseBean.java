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

package org.apache.cocoon.forms.samples.bindings;

/**
 * LenientBaseBean
 * @version $Id$
 */
public class LenientBaseBean {
    protected String breakingField;
    protected String surviveField;

    protected LenientBaseBean(String initVal) {
        this.breakingField = initVal;
        this.surviveField = initVal;
    }

    public String toString() {
        final String className = this.getClass().getName();
        final String state = "[breakingField=" +breakingField + "|surviveField="+ surviveField+"]";
        return className + state;
    }
}
