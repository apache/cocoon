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
package org.apache.cocoon.template.v2.script;

public class AttributeToken extends AbstractToken {
    String namespace;
    String lname;
    String qname;

    public void setup(String namespace, String lname, String qname) {
        this.namespace = namespace;
        this.lname = lname;
        this.qname = qname;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getLName() {
        return lname;
    }

    public String getQName() {
        return qname;
    }
}

