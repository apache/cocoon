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
package org.apache.garbage.serializer.util;

/**
 * The <code>DocType</code> class encapsulates informations regarding
 * the document type public and system IDs and root element name.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: DocType.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
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
