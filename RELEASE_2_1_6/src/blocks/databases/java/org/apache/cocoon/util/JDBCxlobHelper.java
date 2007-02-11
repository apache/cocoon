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
package org.apache.cocoon.util;

import java.io.InputStream;

/**
 * Helper to pass a length plus an InputStream to JDBCTypeConversions
 * to insert BLOB (setBinaryStream) or CLOB (setAsciiStream) via data using
 * org.apache.cocoon.acting.modular.DatabaseAction. Create an input module
 * that returns an instance of this class and use that in your database.xml
 *
 * @version CVS $Id: JDBCxlobHelper.java,v 1.3 2004/03/05 13:01:55 bdelacretaz Exp $
 */
public class JDBCxlobHelper {
    public int length = 0;
    public InputStream inputStream = null;
}

