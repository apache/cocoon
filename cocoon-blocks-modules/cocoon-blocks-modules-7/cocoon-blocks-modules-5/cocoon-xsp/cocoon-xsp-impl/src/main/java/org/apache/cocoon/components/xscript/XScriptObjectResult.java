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
package org.apache.cocoon.components.xscript;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An <code>XScriptObject</code> created from a JAXP
 * <code>Result</code> object.
 *
 * @version $Id$
 * @since   August 30, 2001
 */
public class XScriptObjectResult extends XScriptObject {
    /**
     * The XML content of this object.
     */
    String content;

    public XScriptObjectResult(XScriptManager manager, String content) {
        super(manager);
        this.content = content;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content.getBytes());
    }

    public long getContentLength() {
        return content.length();
    }

    public String getURI() {
        // FIXME: Return a real URL that identifies this object
        return "file:/";
    }

    public String toString() {
        return "XScriptObjectResult: " + content;
    }
}
