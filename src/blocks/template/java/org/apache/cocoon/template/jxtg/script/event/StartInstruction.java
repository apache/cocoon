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


public class StartInstruction extends Event {
    StartInstruction(StartElement startElement) {
        super(startElement.getLocation());
        this.startElement = startElement;
    }

    private final StartElement startElement;
    private EndInstruction endInstruction;

    public EndInstruction getEndInstruction() {
        return endInstruction;
    }

    public void setEndInstruction(EndInstruction endInstruction) {
        this.endInstruction = endInstruction;
    }
}