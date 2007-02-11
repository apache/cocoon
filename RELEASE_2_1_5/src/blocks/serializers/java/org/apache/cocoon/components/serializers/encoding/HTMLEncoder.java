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
package org.apache.cocoon.components.serializers.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: HTMLEncoder.java,v 1.1 2004/04/27 18:35:21 pier Exp $
 */
public class HTMLEncoder extends XHTMLEncoder {

    private char APOSTROPHE[] = { '\'' };

    /**
     * Create a new instance of this <code>HTMLEncoder</code>.
     */
    public HTMLEncoder() {
        super("X-W3C-HTML");
    }
    
    /**
     * Create a new instance of this <code>HTMLEncoder</code>.
     *
     * @param name A name for this <code>Encoding</code>.
     * @throws NullPointerException If one of the arguments is <b>null</b>.
     */
    protected HTMLEncoder(String name) {
        super(name);
    }
    
    /**
     * Return true or false wether this encoding can encode the specified
     * character or not.
     */
    protected boolean compile(char c) {
        if (c == '\'') return(true);
        return(super.compile(c));
    }

    /**
     * Return an array of characters representing the encoding for the
     * specified character.
     */
    public char[] encode(char c) {
        if (c == '\'') return(APOSTROPHE);
        return (super.encode(c));
    }
}