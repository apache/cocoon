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
package org.apache.cocoon.components.web3;

/**
 * Constants in Web3.
 *
 * @since 2.1
 * @version $Id$
 */
public interface Web3 {

    String URI                 = "http://apache.org/cocoon/Web3-Rfc/1.0";

    String INCLUDE_ELEM        = "include";
    String INCLUDE_NAME_ATTR   = "name";
    String INCLUDE_CLASS_ATTR  = "streamer";

    String IMPORT_ELEM         = "import";
    String EXPORT_ELEM         = "export";
    String TABLES_ELEM         = "tables";
    
    String FIELD_ELEM          = "field";
    String FIELD_NAME_ATTR     = "name";
    
    String ROW_ELEM            = "row";
    String ROW_ID_ATTR         = "id";
    
    String STRUCTURE_ELEM      = "structure";
    String STRUCTURE_NAME_ATTR = "name";
    
    String TABLE_ELEM          = "table";
    String TABLE_NAME_ATTR     = "name";

    String ABAP_EXCEPTION_ELEM = "abap-exception";
    String PROCESSING_X_ELEM   = "processing-exception";
}
