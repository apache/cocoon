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
package org.apache.cocoon.template.script;

import org.apache.cocoon.el.DefaultContext;
import org.apache.cocoon.xml.XMLConsumer;

public class ScriptContext extends DefaultContext {
    XMLConsumer consumer;
    ScriptInvoker scriptInvoker;

    public XMLConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    public ScriptInvoker getScriptInvoker() {
        return scriptInvoker;
    }

    public void setScriptInvoker(ScriptInvoker scriptInvoker) {
        this.scriptInvoker = scriptInvoker;
    }
}