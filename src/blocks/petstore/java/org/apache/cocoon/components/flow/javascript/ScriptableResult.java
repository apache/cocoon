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
package org.apache.cocoon.components.flow.javascript;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NotAFunctionException;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @version CVS $Id: ScriptableResult.java,v 1.4 2004/03/05 13:02:03 bdelacretaz Exp $
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


