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
package org.apache.cocoon.template.jxtg.script.event;

import org.apache.cocoon.template.jxtg.expression.JXTExpression;

public class StartForEach extends StartInstruction {
    public StartForEach(StartElement raw, JXTExpression items,
            JXTExpression var, JXTExpression varStatus, JXTExpression begin,
            JXTExpression end, JXTExpression step, Boolean lenient) {
        super(raw);
        this.items = items;
        this.var = var;
        this.varStatus = varStatus;
        this.begin = begin;
        this.end = end;
        this.step = step;
        this.lenient = lenient;
    }

    final JXTExpression items;
    final JXTExpression var;
    final JXTExpression varStatus;
    final JXTExpression begin;
    final JXTExpression end;
    final JXTExpression step;
    final Boolean lenient;

    public JXTExpression getBegin() {
        return begin;
    }

    public JXTExpression getEnd() {
        return end;
    }

    public JXTExpression getItems() {
        return items;
    }

    public Boolean getLenient() {
        return lenient;
    }

    public JXTExpression getStep() {
        return step;
    }

    public JXTExpression getVar() {
        return var;
    }

    public JXTExpression getVarStatus() {
        return varStatus;
    }
}