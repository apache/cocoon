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
package org.apache.cocoon.util;

import java.io.OutputStream;

/**
 * @author <a href="http://apache.org/~reinhard">Reinhard Poetz</a> 
 * @version CVS $Id: NullOutputStream.java,v 1.1 2004/03/11 09:14:32 cziegeler Exp $
 *
 * @since 2.1.4
 */
public final class NullOutputStream extends OutputStream {

    public void write(byte b[]) {
    }

    public void write(byte b[], int off, int len) {
    }

    public void write(int b) {
    }

}