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

import java.util.Stack;

import org.xml.sax.Attributes;

public class StartChoose extends StartInstruction {

    private StartWhen firstChoice;
    private StartOtherwise otherwise;

    public StartChoose(StartElement raw, Attributes attrs, Stack stack) {
        super(raw);
    }

    public void setFirstChoice(StartWhen firstChoice) {
        this.firstChoice = firstChoice;
    }

    public StartWhen getFirstChoice() {
        return firstChoice;
    }

    public void setOtherwise(StartOtherwise otherwise) {
        this.otherwise = otherwise;
    }

    public StartOtherwise getOtherwise() {
        return otherwise;
    }
}
