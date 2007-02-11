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
 * The <code>Charset</code> implementation provided by this factory
 * for the unknown charset.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: UnknownCharset.java,v 1.1 2003/09/04 12:42:36 cziegeler Exp $
 */
public class UnknownCharset implements Charset {

    /**
     * Create a new instance of this <code>UnknownCharset</code>.
     */
    public UnknownCharset() {
        super();
    }

    /**
     * Return the primary name of this <code>Charset</code>
     */
    public String getName() {
        return(null);
    }

    /**
     * Return all alias names for this <code>Charset</code>
     */
    public String[] getAliases() {
        return(new String[0]);
    }

    /**
     * Check if the specified character by representable in this specifiec
     * <code>Charset</code> instance.
     * <p>
     * This implementation always returns <b>true</b>.
     * </p>
     */
    public boolean allows(char c) {
        return(true);
    }

    /**
     * Compare two <code>Charset</code> instances for equality.
     */
    public boolean equals(Object object) {
        return(object instanceof UnknownCharset);
    }

    /**
     * Compare an object to this <code>Charset</code> instances for equality.
     */
    public boolean equals(Charset charset) {
        return(charset instanceof UnknownCharset);
    }
}
