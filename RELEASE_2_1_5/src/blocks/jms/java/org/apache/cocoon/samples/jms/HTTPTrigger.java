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
package org.apache.cocoon.samples.jms;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.hsqldb.Trigger;

/**
 * @version CVS $Id: HTTPTrigger.java,v 1.6 2004/03/05 13:01:57 bdelacretaz Exp $
 * @author <a href="mailto:chaul@apache.org">chaul</a>
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
            // FIXME: Method URLEncoder.encode(triggerName.toLowerCase(), "UTF-8") is absent on JDK1.3
            HttpURLConnection con = (HttpURLConnection) new URL(this.protocol, this.hostname, this.port, this.path+"?trigger="
                            + URLEncoder.encode(triggerName.toLowerCase())
                            + "&table="
                            + URLEncoder.encode(tableName.toLowerCase())).openConnection();
            con.connect();
            con.getContent();
            con.disconnect();
        } catch (Exception e) {
            // not much we can do here.
            throw new RuntimeException("Cannot execute trigger: "+e.getMessage());
        }
    }

}
