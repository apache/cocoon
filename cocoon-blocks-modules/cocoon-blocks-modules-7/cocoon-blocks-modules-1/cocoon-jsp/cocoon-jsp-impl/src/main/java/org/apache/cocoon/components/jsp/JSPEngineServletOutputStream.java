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
package org.apache.cocoon.components.jsp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;

/**
 * Stub implementation of ServletOutputStream.
 */
public final class JSPEngineServletOutputStream extends ServletOutputStream {

    private final ByteArrayOutputStream output;
    private final PrintWriter writer;

    public JSPEngineServletOutputStream() throws UnsupportedEncodingException {
        this.output = new ByteArrayOutputStream();
        this.writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
    }
    
    final PrintWriter getWriter() {
        return this.writer;
    }
    
    public void write(int b) throws IOException  {
        this.output.write(b);
    }
    
    final byte[] toByteArray() {
        this.writer.flush();
        byte[] bytes = output.toByteArray();
        return bytes;
    }
    
}
