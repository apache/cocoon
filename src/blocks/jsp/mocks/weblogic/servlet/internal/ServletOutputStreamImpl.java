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
package weblogic.servlet.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

/**
 * **********************************************************************
 * *                            W A R N I N G                           *
 * **********************************************************************
 *
 *  This is a mock object of the class, not the actual class.
 *  It's used to compile the code in absence of the actual class.
 *
 *  This class is created by hand, not automatically.
 *
 * **********************************************************************
 * 
 * @version CVS $Id: ServletOutputStreamImpl.java,v 1.3 2004/03/05 13:01:58 bdelacretaz Exp $
 */
 
public class ServletOutputStreamImpl extends ServletOutputStream {

    public ServletOutputStreamImpl() {
        super();
    }

    public ServletOutputStreamImpl(ByteArrayOutputStream baos) {
        super();
    }
    
    public void setImpl( ServletResponseImpl impl ){
    }
    
    public void setExpectedCloseCalls(int closeCall) {
    }

    public void setExpectingWriteCalls(boolean expectingWriteCall) {
    }

    public void setThrowIOException(boolean throwException) {
    }

    public void close() throws IOException {
    }

    public String toString() {
        return "";
    }

    public void write(int b) throws IOException {
    }

    public void setupClearContents () {
    }

    public String getContents() {
        return "";
    }

    public void verify() {
    }
}
