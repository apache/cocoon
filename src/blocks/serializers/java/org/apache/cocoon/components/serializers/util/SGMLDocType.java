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
package org.apache.cocoon.components.serializers.util;

/**
 * The <code>SGMLDocType</code> class encapsulates informations regarding
 * the document type public and system IDs and root element name for SGML
 * (like HTML) documents.
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: SGMLDocType.java,v 1.1 2004/04/30 19:34:46 pier Exp $
 */
public class SGMLDocType extends DocType {

    /**
     * Create a new <code>SGMLDocType</code> instance.
     * 
     * @param root_name The document root element name.
     */
    public SGMLDocType(String root_name) {
        this(root_name, null, null);
    }
    
    /**
     * Create a new <code>SGMLDocType</code> instance.
     * 
     * @param root_name The document root element name.
     * @param public_id The document type public identifier.
     * @param system_id The document type system identifier.
     */
    public SGMLDocType(String root_name, String public_id, String system_id) {
        super(root_name);
        this.public_id = public_id;
        this.system_id = system_id;
    }
}
