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
package org.apache.cocoon.template.instruction;

import org.apache.cocoon.template.script.event.Event;

/**
 * @version SVN $Id$
 */
public class MacroContext {
    private final String macroQName;
    private final Event bodyStart;
    private final Event bodyEnd;

    public MacroContext(String macroQName, Event bodyStart, Event bodyEnd) {
        this.macroQName = macroQName;
        this.bodyStart = bodyStart;
        this.bodyEnd = bodyEnd;
    }

    public Event getBodyEnd() {
        return bodyEnd;
    }

    public Event getBodyStart() {
        return bodyStart;
    }

    public String getMacroQName() {
        return macroQName;
    }
}
