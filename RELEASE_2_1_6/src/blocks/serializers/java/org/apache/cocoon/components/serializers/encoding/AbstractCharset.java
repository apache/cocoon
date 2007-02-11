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
 * @version CVS $Id: AbstractCharset.java,v 1.1 2004/04/21 09:33:22 pier Exp $
 */
public abstract class AbstractCharset implements Charset {

    /** The name of this <code>Charset</code>. */
    private String name;
    /** All alias names for this <code>Charset</code>. */
    private String aliases[];

    /**
     * Create a new instance of this <code>AbstractCharset</code>.
     *
     * @param name This <code>Charset</code> name.
     * @param aliases This <code>Charset</code> alias names.
     * @throws NullPointerException If one of the arguments is <b>null</b>.
     */
    public AbstractCharset(String name, String aliases[]) {
        super();
        if (name == null) throw new NullPointerException("Invalid name");
        if (aliases == null) throw new NullPointerException("Invalid aliases");
        this.name = name;
        this.aliases = aliases;
    }

    /**
     * Return the primary name of this <code>Charset</code>
     */
    public String getName() {
        return(this.name);
    }

    /**
     * Return all alias names for this <code>Charset</code>
     */
    public String[] getAliases() {
        String array[] = new String[this.aliases.length];
        System.arraycopy(this.aliases, 0, array, 0, array.length);
        return(array);
    }

    /**
     * Compare an object to this <code>Charset</code> instances for equality.
     */
    public boolean equals(Object object) {
        if (object instanceof Charset) return(equals((Charset)object));
        return(false);
    }

    /**
     * Compare two <code>Charset</code> instances for equality.
     */
    public boolean equals(Charset charset) {
        if (charset == null) return(false);
        if ((charset.getClass().getName().equals(this.getClass().getName()))
            && (charset.getName().equals(this.getName()))) return(true);
        return(false);
    }

    /**
     * Return a <code>String</code> representation of this
     * <code>Charset</code>.
     */
    public String toString() {
        return(super.toString() + "[" + this.getName() + "]");
    }
}
