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

import java.util.ArrayList;
import java.util.List;

public class Script {
    ArrayList tokens = new ArrayList();

    public void add(Token token) {
        token.setStart(size());
        tokens.add(token);
    }

    public void addAtom(Token token) {
        add(token);
        token.setEnd(size());
    }

    public Token get(int i) {
        return (Token) tokens.get(i);
    }

    public int size() {
        return tokens.size();
    }

    public List getTokens() {
        return tokens;
    }
}