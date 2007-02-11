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
package org.apache.cocoon.faces.samples;

/**
 * Hello World sample
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class Hello {
    private String value;


    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if ("".equals(value)) {
            value = null;
        }
        this.value = value;
    }

    public String doButton1Action() {
        return "done";
    }

    public String doButton2Action() {
        return "flip";
    }
}
