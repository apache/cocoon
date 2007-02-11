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
package org.apache.garbage.serializer.util;

/**
 * The <code>DocType</code> class encapsulates informations regarding
 * the document type public and system IDs and root element name.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: DocType.java,v 1.1 2003/09/04 12:42:42 cziegeler Exp $
 */
public class DocType {
    /** The name of the root element. */
    private String root_name = null;
    /** The configured system identifier. */
    private String public_id = null;
    /** The configured public identifier. */
    private String system_id = null;

    /**
     * Create a new <code>DocType</code> instance.
     * 
     * @param root_name The document root element name.
     * @param public_id The document type public identifier.
     * @param system_id The document type system identifier.
     */
    public DocType(String root_name, String system_id) {
        this(root_name, null, system_id);
    }

    /**
     * Create a new <code>DocType</code> instance.
     * 
     * @param root_name The document root element name.
     * @param public_id The document type public identifier.
     * @param system_id The document type system identifier.
     */
    public DocType(String root_name, String public_id, String system_id) {
        super();
        if (root_name == null)
            throw new NullPointerException("Invalid root document name");

        if ((public_id != null) && (system_id == null))
            throw new NullPointerException("Required System ID is NULL");

        this.root_name = root_name;
        this.public_id = public_id;
        this.system_id = system_id;
    }

    /**
     * Return the document root element name.
     */
    public String getName() {
        return(this.root_name);
    }

    /**
     * Return the document type public identifier or <b>null</b> if none
     * configured..
     */
    public String getPublicId() {
        return(this.public_id);
    }

    /**
     * Return the document type system identifier or <b>null</b> if none
     * configured..
     */
    public String getSystemId() {
        return(this.system_id);
    }

    /**
     * Check if the specified object is equal to this <code>DocType</code>
     * instance.
     */
    public boolean equals(Object object) {
        if (object == null) return(false);
        if (this == object) return(true);

        if (!(object instanceof DocType)) return(false);
        DocType doctype = (DocType)object;

        if (this.public_id != null) {
            return(this.public_id.equals(doctype.public_id) &&
                   this.system_id.equals(doctype.system_id) &&
                   this.root_name.equals(doctype.root_name));
        }

        if (this.system_id != null) {
            return(this.system_id.equals(doctype.system_id) &&
                   this.root_name.equals(doctype.root_name));
        }

        return(this.root_name.equals(doctype.root_name));
    }
}
