/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.serializers.util;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The <code>DocType</code> class encapsulates informations regarding
 * the document type public and system IDs and root element name.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id$
 */
public class DocType {

    private static final char S_DOCTYPE_1[] = "<!DOCTYPE ".toCharArray();
    private static final char S_DOCTYPE_2[] = " PUBLIC \"".toCharArray();
    private static final char S_DOCTYPE_3[] = "\" \"".toCharArray();
    private static final char S_DOCTYPE_4[] = " SYSTEM \"".toCharArray();
    private static final char S_DOCTYPE_5[] = "\">".toCharArray();
    private static final char S_DOCTYPE_6[] = ">".toCharArray();
    
    /** The name of the root element. */
    protected String root_name = null;
    /** The configured system identifier. */
    protected String public_id = null;
    /** The configured public identifier. */
    protected String system_id = null;

    /**
     * Create a new <code>DocType</code> instance.
     * 
     * @param root_name The document root element name.
     */
    public DocType(String root_name) {
        this(root_name, null, null);
    }
    
    /**
     * Create a new <code>DocType</code> instance.
     * 
     * @param root_name The document root element name.
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
     * Return the document type declaration as a string
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(S_DOCTYPE_1); // [<!DOCTYPE ]
        buf.append(this.root_name);
        if (this.public_id != null) {
            buf.append(S_DOCTYPE_2); // [ PUBLIC "]
            buf.append(this.public_id);
            /* This is wrong in XML, but not in SGML/HTML */
            if (this.system_id != null) {
                buf.append(S_DOCTYPE_3); // [" "]
                buf.append(this.system_id);
            }
            buf.append(S_DOCTYPE_5); // [">]
        } else if (this.system_id != null) {
            buf.append(S_DOCTYPE_4); // [ SYSTEM "]
            buf.append(this.system_id);
            buf.append(S_DOCTYPE_5); // [">]
        } else {
            buf.append(S_DOCTYPE_6); // [>]
        }
        return(buf.toString());
    }

    /**
     * Check if the specified object is equal to this <code>DocType</code>
     * instance.
     */
    public boolean equals(Object object) {
        if (!(object instanceof DocType)) {
            return false;
        }
        DocType rhs = (DocType)object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(public_id, rhs.public_id)
            .append(system_id, rhs.system_id)
            .append(root_name, rhs.root_name)
            .isEquals();
    }

    /**
     * Returns the hash code value for this docType
     */
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(public_id)
            .append(system_id)
            .append(root_name)
            .toHashCode();
    }
}
