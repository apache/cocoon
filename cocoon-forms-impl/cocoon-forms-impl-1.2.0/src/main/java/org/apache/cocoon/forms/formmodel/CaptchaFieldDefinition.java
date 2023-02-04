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
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * A {@link FieldDefinition} for {@link CaptchaField}s.
 *
 * @see <a href="http://www.captcha.net">www.captcha.net</a>
 * @version $Id$
 */
public class CaptchaFieldDefinition extends FieldDefinition {

    private int length;
    private ProcessInfoProvider processInfoProvider;

    public CaptchaFieldDefinition(ProcessInfoProvider processInfoProvider) {
        this.processInfoProvider = processInfoProvider;
    }

    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        super.initializeFrom(definition);

        if (!(definition instanceof CaptchaFieldDefinition)) {
            throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not a CaptchaFieldDefinition.",
                                     getLocation());
        }

        CaptchaFieldDefinition other = (CaptchaFieldDefinition) definition;

        this.length = other.length;
    }

    public Widget createInstance() {
        return new CaptchaField(this, processInfoProvider);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        checkMutable();
        this.length = length;
    }
}
