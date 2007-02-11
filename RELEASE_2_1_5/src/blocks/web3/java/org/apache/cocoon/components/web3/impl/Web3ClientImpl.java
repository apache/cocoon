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
package org.apache.cocoon.components.web3.impl;

import org.apache.cocoon.components.web3.Web3Client;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.excalibur.pool.Poolable;

import com.sap.mw.jco.JCO;
import com.sap.mw.jco.IRepository;

import java.net.URL;
import java.util.Properties;
import java.util.Date;

/**
 * The standard interface for R3Clients in Web3.
 *
 * @author <a href="mailto:michael.gerzabek@at.efp.cc">Michael Gerzabek</a>
 * @since 2.1
 * @version CVS $Id: Web3ClientImpl.java,v 1.5 2004/03/05 13:02:25 bdelacretaz Exp $
 */
public class Web3ClientImpl extends AbstractLogEnabled 
implements Web3Client, Disposable, Recyclable, Poolable {

    protected JCO.Client client = null;
    protected String repository = null;
        
    public void initClient(JCO.Client client) {
        this.client = client;
        this.repository = "" + (new Date ()).getTime();
    }
    
    public void releaseClient() {
        JCO.releaseClient(this.client);
        this.client = null;
    }
    
    public void dispose() {
        this.client = null;
        this.repository = null;
    }
    
    public IRepository getRepository() {
        if (null != this.repository) {
            return new JCO.Repository (this.repository, this.client);
        }
        return null;
    }
    
    public void abort(String message) {
        this.client.abort (message);
    }
    
    public Object clone() {
        return null;
    }
    
    public void confirmTID(String tid) {
        this.client.confirmTID (tid);
    }
    
    public void connect() {
        this.client.connect ();
    }
    
    public String createTID() {
        return this.client.createTID ();
    }
    
    public void disconnect() {
        this.client.disconnect ();
    }
    
    public void execute(JCO.Function function) {
        this.client.execute (function);
    }
    
    public void execute(JCO.Function function, String tid) {
        this.client.execute (function, tid);
    }
    
    public void execute(JCO.Function function, String tid, String queue_name) { 
        this.client.execute (function, tid, queue_name);
    }
    
    public void execute(JCO.Function function, String tid, 
                        String queue_name, int queue_pos) {
        this.client.execute (function, tid, queue_name, queue_pos);
    }
    
    public void execute(String name, JCO.ParameterList input, 
                        JCO.ParameterList output) {
        this.client.execute (name, input, output);
    }
    
    public void execute(String name, JCO.ParameterList input, 
                        JCO.ParameterList output, JCO.ParameterList tables) {
        this.client.execute (name, input, output, tables);
    }
    
    public void execute(String name, JCO.ParameterList input, 
                        JCO.ParameterList tables, String tid) {
        this.client.execute (name, input, tables, tid);
    }
    
    public void execute(java.lang.String name, JCO.ParameterList input, 
                        JCO.ParameterList tables, String tid, 
                        String queue_name) {
        this.client.execute (name, input, tables, tid, queue_name);
    }
    
    public void execute(java.lang.String name, JCO.ParameterList input, 
                        JCO.ParameterList tables, String tid, 
                        String queue_name, int queue_pos) {
        this.client.execute (name, input, tables, tid, queue_name, queue_pos);
    }
    
    public boolean getAbapDebug() {
        return this.client.getAbapDebug ();
    }
    
    public String getASHost() {
        return this.client.getASHost ();
    }

    public JCO.Attributes getAttributes() {
        return this.client.getAttributes ();
    }
    
    public String getClient() {
        return this.client.getClient ();
    }
    
    public String getGroup() {
        return this.client.getGroup ();
    }
    
    public String getGWHost() {
        return this.client.getGWHost ();
    }

    public String getGWServ() {
        return this.client.getGWServ ();
    }
    
    public String getLanguage() {
        return this.client.getLanguage ();
    }
    
    public String getMSHost() {
        return this.client.getMSHost ();
    }
    
    public String[][] getPropertyInfo() {
        return this.client.getPropertyInfo ();
    }
    
    public int getSapGui() {
        return this.client.getSapGui ();
    }
    
    public byte getState() {
        return this.client.getState ();
    }
    
    public String getSystemID() {
        return this.client.getSystemID ();
    }
    
    public String getSystemNumber() {
        return this.client.getSystemNumber ();
    }
    
    public String getTPName() {
        return this.client.getTPName ();
    }
    
    public boolean getTrace() { 
        return this.client.getTrace ();
    }
    
    public URL getURL() {
        return this.client.getURL ();
    }
    
    public String getUser() {
        return this.client.getUser ();
    }
    
    public boolean isAlive() {
        return this.client.isAlive ();
    }
    
    public void ping() {
        this.client.ping ();
    }
    
    public void setAbapDebug(boolean debug) {
        this.client.setAbapDebug (debug);
    }
    
    public void setProperty(String key, String value) {
        this.client.setProperty (key, value);
    }
    
    public void setSapGui(int use_sapgui) {
        this.client.setSapGui (use_sapgui);
    }
    
    public void setTrace(boolean trace) {
        this.client.setTrace (trace);
    }
    
    public final Properties getProperties() {
        return this.client.getProperties ();
    }
    
    public final String getProperty(String key) {
        return this.client.getProperty (key);
    }
    
    public boolean isValid() {
        return this.client.isValid ();
    }
    
    public void setThroughput(JCO.Throughput throughput) {
        this.client.setThroughput (throughput);
    }   
    
    public void recycle() {
        this.client = null;
        this.repository = null;
    }
    
}
