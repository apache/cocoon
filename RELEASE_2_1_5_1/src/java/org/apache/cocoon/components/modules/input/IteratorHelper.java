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
package org.apache.cocoon.components.modules.input;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Wraps an Enumeration and provides Iterator interface.
 *
 * @version CVS $Id: IteratorHelper.java,v 1.4 2004/03/05 13:02:48 bdelacretaz Exp $
 */
class IteratorHelper implements Iterator {
    Enumeration enum = null;
    public IteratorHelper( Enumeration e ) { this.enum = e; }
    public boolean hasNext() { return this.enum.hasMoreElements(); }
    public Object next() { return this.enum.nextElement(); }
    /** ignored */
    public void remove() {}
}
