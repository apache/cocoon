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
package org.apache.cocoon.precept.stores.dom.simple;

import java.util.Collection;

/**
 * @author Torsten Curdt <tcurdt@dff.st>
 * @version CVS $Id: AttributeNode.java,v 1.3 2004/03/05 13:02:20 bdelacretaz Exp $
 */
public class AttributeNode extends Node {

    public AttributeNode(String name, Collection constraints) {
        super(name, constraints);
    }

    public void toStringBuffer(StringBuffer sb, int depth) {
        sb.append(" ").append(name).append("=");
        sb.append("\"").append(String.valueOf(getValue())).append("\"");
    }
}
