/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.apache.cocoon.components.flow.javascript;

import org.mozilla.javascript.*;
import java.sql.*;

/**
 *
 * @version CVS $Id: ScriptableResult.java,v 1.2 2004/02/23 01:19:45 coliver Exp $
 */
public class ScriptableResult extends ScriptableObject {

    public String getClassName() {
        return "Result";
    }
    
    public ScriptableResult() {
    }

    public static class Row extends ScriptableObject {

        public String getClassName() {
            return "Row";
        }

        public boolean has(String name, Scriptable start) {
            return super.has(name.toUpperCase(), start);
        }

        public Object get(String name, Scriptable start) {
            return super.get(name.toUpperCase(), start);
        }
        
        public void put(String name, Scriptable start, Object value) {
            super.put(name.toUpperCase(), start, value);
        }
    }

    ScriptableResult(Scriptable scope, 
                     ResultSet rs, int startRow, int maxRows) 
        throws SQLException, PropertyException, NotAFunctionException, JavaScriptException {
        Context cx = Context.getCurrentContext();
        Scriptable rowMap = cx.newObject(scope, "Array");
        put("rows", this, rowMap);
        Scriptable rowByIndex = cx.newObject(scope, "Array");
        put("rowsByIndex", this, rowByIndex);

        ResultSetMetaData rsmd = rs.getMetaData();
        int noOfColumns = rsmd.getColumnCount();

        // Create the column name array
        Scriptable columnNames = cx.newObject(scope, 
                                              "Array", 
                                              new Object[] {new Integer(noOfColumns)});
        put("columnNames", this, columnNames);
        for (int i = 1; i <= noOfColumns; i++) {
            columnNames.put(i-1, columnNames, rsmd.getColumnName(i));
        }

        // Throw away all rows upto startRow
        for (int i = 0; i < startRow; i++) {
            rs.next();
        }

        // Process the remaining rows upto maxRows
        int processedRows = 0;
        int index = 0;
        boolean isLimited = false;
        while (rs.next()) {
            if ((maxRows != -1) && (processedRows == maxRows)) {
                isLimited = true; 
                break;
            }
            Scriptable columns = cx.newObject(scope, "Array",
                                              new Object[] {new Integer(noOfColumns)});
            Scriptable columnMap = new Row();
            columnMap.setParentScope(columns.getParentScope());
            columnMap.setPrototype(getObjectPrototype(scope));

            // JDBC uses 1 as the lowest index!
            for (int i = 1; i <= noOfColumns; i++) {
                Object value =  rs.getObject(i);
                if (rs.wasNull()) {
                    value = null;
                }
                columns.put(i-1, columns, value);
                columnMap.put(rsmd.getColumnName(i), columnMap, value);
            }
            rowMap.put(index, rowMap, columnMap);
            rowByIndex.put(index, rowByIndex, columns);
            processedRows++;
            index++;
        }
        put("rowCount", this, new Integer(index));
        put("isLimitedByMaxRows", this, new Boolean(isLimited));
    }
}


