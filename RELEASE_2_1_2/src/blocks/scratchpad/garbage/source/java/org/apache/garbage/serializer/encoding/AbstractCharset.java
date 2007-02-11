/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.serializer.encoding;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: AbstractCharset.java,v 1.1 2003/09/04 12:42:35 cziegeler Exp $
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
