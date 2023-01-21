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

import org.apache.cocoon.forms.FormsException;

/**
 * The {@link WidgetDefinition} corresponding to a {@link Union} widget.
 *
 * @version $Id$
 */
public class UnionDefinition extends AbstractContainerDefinition {

    private String caseWidgetId;


    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        super.initializeFrom(definition);

        if (!(definition instanceof UnionDefinition)) {
            throw new FormsException("Parent definition " + definition.getClass().getName() + " is not a UnionDefinition.",
                                     getLocation());
        }

        UnionDefinition other = (UnionDefinition) definition;

        this.caseWidgetId = other.caseWidgetId;
    }

    public void setCaseWidgetId(String id) {
        checkMutable();
        caseWidgetId = id;
    }

    public String getCaseWidgetId() {
        return caseWidgetId;
    }

    public Widget createInstance() {
        return new Union(this);
    }
}

