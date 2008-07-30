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
package org.apache.cocoon.samples.jms;

import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.cocoon.util.NetUtils;
import org.hsqldb.Trigger;

/**
 * @version $Id$
 */
public class HTTPTrigger implements Trigger {

    protected String protocol = "http";
    protected String hostname = "localhost";
    protected int port = 8888;
    protected String path = "/samples/jms/database/jms-invalidate";

    /* 
     * @see org.hsqldb.Trigger#fire(java.lang.String, java.lang.String, java.lang.Object[])
     */
    public void fire(String triggerName, String tableName, Object[] row) {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(this.protocol, this.hostname, this.port, this.path+"?trigger="
                            + NetUtils.encode(triggerName.toLowerCase(), "utf-8")
                            + "&table="
                            + NetUtils.encode(tableName.toLowerCase(), "utf-8")).openConnection();
            con.connect();
            con.getContent();
            con.disconnect();
        } catch (Exception e) {
            // not much we can do here.
            throw new RuntimeException("Cannot execute trigger: "+e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.hsqldb.Trigger#fire(int, java.lang.String, java.lang.String, java.lang.Object[], java.lang.Object[])
     */
    public void fire(int arg0, String arg1, String arg2, Object[] arg3, Object[] arg4) {
        // TODO Auto-generated method stub
        
    }

}
