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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: Web3ClientImpl.java,v 1.4 2003/07/10 22:14:32 reinhard Exp $
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
