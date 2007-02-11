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
package org.apache.cocoon.ant;

import java.io.IOException;
import java.io.OutputStream;

/**
 *   A OutputStream writting no bytes at all.
 *
 * @author    huber@apache.org
 * @version CVS $Id: NullOutputStream.java,v 1.2 2004/03/05 10:07:25 bdelacretaz Exp $
 */
public class NullOutputStream extends OutputStream {
    /**
     *   Description of the Method
     *
     * @param  b                Description of Parameter
     * @exception  IOException  Description of Exception
     */
    public void write(int b) throws IOException { }


    /**
     *   Description of the Method
     *
     * @param  b                Description of Parameter
     * @exception  IOException  Description of Exception
     */
    public void write(byte b[]) throws IOException { }


    /**
     *   Description of the Method
     *
     * @param  b                Description of Parameter
     * @param  off              Description of Parameter
     * @param  len              Description of Parameter
     * @exception  IOException  Description of Exception
     */
    public void write(byte b[], int off, int len) throws IOException { }
}


