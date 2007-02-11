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
package com.sap.mw.jco;

import java.util.Properties;
import java.net.URL;

/**
 * **********************************************************************
 * *                            W A R N I N G                           *
 * **********************************************************************
 *
 *  This is a mock object of the class, not the actual class.
 *  It's used to compile the code in absence of the actual class.
 *
 *  This clsss is created by hand, not automatically.
 *
 * **********************************************************************
 * 
 * @version CVS $Id: JCO.java,v 1.3 2004/03/05 13:02:26 bdelacretaz Exp $
 */
 
public class JCO {

    public static void addClientPool(String SID, int poolsize, Properties properties) {
    }
    
    public static void setTraceLevel(int level) {
    }
    
    public static JCO.Client getClient(String key) {
        return null;
    }
    
    public static void releaseClient(JCO.Client client) {
    }
    
    public static void removeClientPool(String key) {
    }
    
    public static JCO.PoolManager getClientPoolManager() {
        return null;
    }
    
    public static class PoolManager {
        
        public JCO.Pool getPool(String name) {
            return null;
        }
    }
    
    public static class Pool {
     
        public final void setTrace(boolean trace) {
        }
    }
    
    public static class Function {
        
        public JCO.ParameterList getImportParameterList() {
            return null;
        }
        
        public JCO.ParameterList getExportParameterList() {
            return null;
        }
        
        public JCO.ParameterList getTableParameterList() {
            return null;
        }    
        
        public String getName() {
            return null;
        }
    }
    
    public static class ParameterList extends Record {
    }
    
    public static class Attributes {
    }
    
    public static class Throughput {
    }
    
    public static class Repository implements IRepository {
        public Repository (String s, JCO.Client client) {
        }
        
        public IFunctionTemplate getFunctionTemplate(String name) {
            return null;
        }
    }
    
    public static class Record {
        
        public String getName(int index) {
            return null;
        }
        
        public final JCO.Field getField(int index) {
            return null;
        }
        
        public final JCO.Field getField(String name) {
            return null;
        }
        
        public String getString(int index) {
            return null;
        }
        
        public JCO.Structure getStructure(int index) {
            return null;
        }
        
        public JCO.Structure getStructure(String name) {
            return null;
        }
        
        public JCO.Table getTable(int index) {
            return null;
        }
        
        public JCO.Table getTable(String name) {
            return null;
        }
        
        public int getFieldCount() {
            return -1;
        }
    }
    
    public static class Field {
        
        public boolean isStructure() {
            return false;
        }
        
        public void setValue(String value) throws JCO.ConversionException {
        }
    }
    
    public static class Table extends Record {
        
        public void appendRow() {
        }
        
        public final int getNumRows() {
            return -1;
        }
        
        public void setRow(int pos) {
        }
    }
    
    public static class Structure extends Record {
    }
    
    public static class AbapException extends Throwable {
        
        public final String getKey() {
            return null;
        }
    }
    
    public static class ConversionException extends Throwable {
    }
    
    public static class Client {
        
        public void abort(String message) {
        }
        
        public void connect() {
        }
        
        public Object clone() {
            return null;
        }
        
        public void disconnect() {
        }
        
        public void execute(JCO.Function function) {
        }
        
        public void execute(String name, 
                    JCO.ParameterList input,
                    JCO.ParameterList output) {
        }
        
        public void execute(String name,
                    JCO.ParameterList input,
                    JCO.ParameterList output,
                    JCO.ParameterList tables) {
        }
        
        public void execute(JCO.Function function,
                    java.lang.String tid) {
        }
        
        public void execute(JCO.Function function,
                    String tid,
                    String queue) {
        }
        
        public void execute(JCO.Function function,
                    String tid,
                    String queue,
                    int pos) {
        }
        
        public void execute(String name,
                    JCO.ParameterList input,
                    JCO.ParameterList tables,
                    String tid) {
        }
        
        public void execute(String name,
                    JCO.ParameterList input,
                    JCO.ParameterList tables,
                    String tid,
                    String queue) {
        }
        
        public void execute(String name,
                    JCO.ParameterList input,
                    JCO.ParameterList tables,
                    String tid,
                    String queue,
                    int pos) {
        }
        
        public String createTID() {
            return null;
        }
        
        public void confirmTID(String tid) {
        }
        
        public final void ping() {
        }
        
        public boolean getAbapDebug() {
            return false;
        }

        public String getASHost() {
            return null;
        }

        public JCO.Attributes getAttributes() {
            return null;
        }

        public String getClient() {
            return null;
        }

        public String getGroup() {
            return null;
        }

        public String getGWHost() {
            return null;
        }

        public String getGWServ() {
            return null;
        }

        public String getLanguage() {
            return null;
        }

        public String getMSHost() {
            return null;
        }

        public String[][] getPropertyInfo() {
            return null;
        }

        public int getSapGui() {
            return -1;
        }

        public byte getState() {
            return 0;
        }

        public String getSystemID() {
            return null;
        }

        public String getSystemNumber() {
            return null;
        }

        public String getTPName() {
            return null;
        }

        public boolean getTrace() { 
            return false;
        }

        public URL getURL() {
            return null;
        }

        public String getUser() {
            return null;
        }

        public boolean isAlive() {
            return false;
        }

        public void setAbapDebug(boolean debug) {
        }

        public void setProperty(String key, String value) {
        }

        public void setSapGui(int use_sapgui) {
        }

        public void setTrace(boolean trace) {
        }

        public final Properties getProperties() {
            return null;
        }

        public final String getProperty(String key) {
            return null;
        }

        public boolean isValid() {
            return false;
        }

        public void setThroughput(JCO.Throughput throughput) {
        }           
    }
    
}
