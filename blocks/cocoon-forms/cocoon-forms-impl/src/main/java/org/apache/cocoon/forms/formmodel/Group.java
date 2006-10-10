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
package org.apache.cocoon.forms.formmodel;


/**
 * A container {@link Widget} which can hold zero or more child widgets.
 *
 * @version $Id$
 */
public class Group extends AbstractContainerWidget {

    private static final String GROUP_EL = "group";

    private final GroupDefinition definition;


    public Group(GroupDefinition definition) {
        super(definition);
        this.definition = definition;
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    /**
     * @return "group"
     */
    public String getXMLElementName() {
        return GROUP_EL;
    }

}
