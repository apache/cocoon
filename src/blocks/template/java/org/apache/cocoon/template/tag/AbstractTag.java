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
package org.apache.cocoon.template.tag;

import org.apache.cocoon.template.script.AbstractToken;
import org.apache.cocoon.template.script.ElementToken;
import org.apache.cocoon.template.script.ScriptContext;

public abstract class AbstractTag extends AbstractToken implements
        ElementToken, Tag {
    protected int bodyStart;

    public int getBodyStart() {
        return bodyStart;
    }

    public void setBodyStart(int bodyStart) {
        this.bodyStart = bodyStart;
    }

    public abstract void invoke(ScriptContext context) throws Exception;

    public void invokeBody(ScriptContext context) throws Exception {
        context.getScriptInvoker().invoke(bodyStart, end);
    }
}