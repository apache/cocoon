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
package org.apache.cocoon.profiler.debugging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.wrapper.AbstractRequestWrapper;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.dom.DOMUtil;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * This is an experimental debugger.
 * @since 2.2
 * @version $Id$
 */
public class Debugger extends AbstractLogEnabled
                      implements Serviceable, Disposable, Initializable, Contextualizable {

    protected static final String DEBUGGER_KEY = Debugger.class.getName();
    protected static final String SITEMAP_COUNTER_KEY = DEBUGGER_KEY + "/sitemap-counter";
    public static final String REQUEST_PARAMETER = "remote-debug";

    protected PrintWriter     writer;
    protected BufferedReader  reader;
    protected String          information;
    protected Socket          socket;
    protected ServiceManager  manager;
    protected DOMParser       parser;
    protected XPathProcessor  processor;
    protected String          debugInfo;
    protected boolean         finished;
    protected Context         context;
    private Stack infoStack = new Stack();
    
    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
        this.processor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        int pos = this.debugInfo.indexOf(':');
        
        String host = this.debugInfo.substring(0, pos);
        int port = Integer.parseInt(this.debugInfo.substring(pos + 1));

        getLogger().info("Trying to open connection to: " + host + " using port " + port);
        this.socket = new Socket(host, port);
        this.socket.setKeepAlive(true);
        this.socket.setSoTimeout(60);

        this.writer = new PrintWriter(socket.getOutputStream());
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.finished = false;
        Map objectModel = ContextHelper.getObjectModel(this.context);
        objectModel.put(DEBUGGER_KEY, this);
        Request oldRequest = ObjectModelHelper.getRequest(objectModel);
        Request newRequest = new RequestWrapper(oldRequest);
        objectModel.put(ObjectModelHelper.REQUEST_OBJECT, newRequest);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.context != null ) {
            Map objectModel = ContextHelper.getObjectModel(this.context);
            objectModel.remove(DEBUGGER_KEY);
            this.context = null;
        }
        if ( this.manager != null ) {
            this.manager.release(this.parser);
            this.manager.release(this.processor);
            this.parser = null;
            this.processor = null;
            this.manager = null;
        }
    }

    public Debugger(){
        this.finished = true;
    }

    public void setDebugInfo(String value) {
        this.debugInfo = value;
    }

    public void sendFinal(String value) {
        if (this.finished) {
            return;
        }
        this.writer.write("<message>\n");
        this.writer.write(value);
        this.writer.write("</message>\n");
        this.writer.write(0);
        this.writer.flush();
    }
    
    public void send(String value) {
        if (this.finished) {
            return;
        }
        
        this.sendFinal(value);
                
        if (!isSocketReaderReady()) {
            this.getLogger().warn("Socket disconnected.");
            this.close();
        }

        int read;
        final StringBuffer response = new StringBuffer();
        do {
            try {
                read = this.reader.read();
                if (read > 0) {
                    response.append((char)read);
                }
            } catch (IOException ioe) {
                this.getLogger().warn("IOException during reading - stop debugging.");
                this.close();
                return;
            }
        } while (read > 0);
        
        Document doc;
        try {
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(response.toString()));
            doc = this.parser.parseDocument(is);
        } catch (Exception any) {
            this.getLogger().warn("Exception during parsing - stop debugging.");
            // if an exception during parsing occurs, we simply stop debugging
            this.close();
            return;
        }
       
        try {
            String status = DOMUtil.getValueOf(doc, "message/status", this.processor);
        
            if ( status != null && !status.equals("0")) {
                this.finished = true;
            }
        } catch (ProcessingException any) {
            this.getLogger().warn("Exception during parsing of status - stop debugging.");
            // if an exception during parsing occurs, we simply stop debugging
            close();
            return;
        }
        /*
         * Not used
        NodeList list = DOMUtil.getNodeListFromPath(doc, new String[] {"message","status","get"});
        if (list != null && list.getLength() > 0) {
            StringBuffer message = new StringBuffer();
            for(int i = 0; i < list.getLength(); i++) {
                final Node current = list.item(i);
                final String getKey = DOMUtil.getValueOfNode(current, "key");
                message.append("<get>\n");
                message.append("<key>").append(getKey).append("</key>\n");
                VariableResolver vr = VariableResolverFactory.getResolver(getKey, this.manager);
                final String getValue = vr.resolve(this.context, this.objectModel);
                message.append("<value>").append(getValue).append("</value>\n");
                message.append("</get>\n");
            }
            
            // FIXME: Avoid recursion!
            this.send(message.toString());
        }
         */
    }
    
    /**
     * Close the connection
     */
    public void close() {
        if (this.socket != null) {
            this.getLogger().info("Closing connection.");
            this.finished = true;
            try {
                this.writer.close();
            } catch (Exception ignore) {
                // ignore the exception
            }
            try {
                this.reader.close();
            } catch (Exception ignore) {
                // ignore the exception
            }
            try {
                this.socket.close();
            } catch (Exception ignore) {
                // ignore the exception
            }
            this.socket = null;
            this.reader = null;
            this.writer = null;
        }
    }
    
    public void sendSitemapElement(String type, String configuration) {
        if (this.finished) {
            return;
        }
        this.sendSitemapElement(type, configuration, null);
    }
    
    public void sendSitemapElement(String type, String configuration, Parameters p) {
        if (this.finished) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<sitemap-element>\n");
        if (this.information != null) {
            buffer.append("<information>\n");
            buffer.append(this.information).append('\n');
            buffer.append("</information>\n");
        }
        buffer.append("<statement type=\"").append(type).append("\">\n");
        if (configuration != null) {
            buffer.append(configuration).append('\n');
        }
        if (p != null) {
            String[] names = p.getNames();
            if (names != null && names.length > 0) {
                buffer.append("<parameters>\n");
                for(int i=0; i < names.length; i++) {
                    buffer.append("<parameter name=\"").append(names[i]).append("\">");
                    buffer.append(xmlText(p.getParameter(names[i], ""))).append("</parameter>\n");
                }
                buffer.append("</parameters>\n");
            }
        }
        buffer.append("</statement>\n");
        buffer.append("</sitemap-element>\n");
        
        this.send(buffer.toString());
    }

    public void pushInformation(Map infoMap) {
        if ( !this.infoStack.empty() ) {
            final Map oldMap = (Map)this.infoStack.peek();
            infoMap = new HashMap(infoMap);
            final Iterator i = oldMap.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry entry = (Map.Entry)i.next();
                final String key = entry.getKey().toString();
                if ( key.indexOf(':') == -1) {
                    infoMap.put("../"+key, entry.getValue());
                } else {
                    infoMap.put(key, entry.getValue());
                }
            }
        }
        
        this.infoStack.push(infoMap);
        this.calcInfo();
    }
    
    public void popInformation() {
        if ( !this.infoStack.empty() ) {
            this.infoStack.pop();
        }
        this.calcInfo();
    }
    
    public void clearInformation() {
        this.infoStack.clear();
        this.calcInfo();
    }
    
    /**
     * Calculate the current information string
     */
    protected void calcInfo() {
        if ( this.infoStack.empty() ) {
            this.information = null;
        } else {
            Map value = (Map)this.infoStack.peek();
            Iterator e = value.keySet().iterator();
            StringBuffer buffer = new StringBuffer("<values>\n");
            while (e.hasNext()) {
                Object key = e.next();
                Object singleValue = value.get(key);
                buffer.append("<value name=\"").append(key).append("\">");
                buffer.append(xmlText(singleValue.toString())).append("</value>\n");
            }
            buffer.append("</values>\n");
            this.information = buffer.toString();
        }
    }
    
    /**
     * Get the debugger
     */
    static public Debugger getDebugger(Map objectModel) {
        return (Debugger)objectModel.get(DEBUGGER_KEY);
    }

    public static Integer getSitemapCounter(Map objectModel) {
        Integer i = (Integer)objectModel.get(SITEMAP_COUNTER_KEY);
        if ( i == null ) {
            i = new Integer(0);
            objectModel.put(SITEMAP_COUNTER_KEY, i);
        }
        return i;
    }
    
    public static Integer incSitemapCounter(Map objectModel) {
        Integer i = getSitemapCounter(objectModel);
        i = new Integer(i.intValue() + 1 );
        objectModel.put(SITEMAP_COUNTER_KEY, i);        
        return i;
    }
    
    public static Integer decSitemapCounter(Map objectModel) {
        Integer i = getSitemapCounter(objectModel);
        i = new Integer(i.intValue() - 1 );
        objectModel.put(SITEMAP_COUNTER_KEY, i);
        return i;
    }

    private boolean isSocketReaderReady() {
        try {
            while (!reader.ready() /*&& !this.socket.isInputShutdown()*/) {
                try {
                    java.lang.Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore the exception 
                }
            }
            return reader.ready();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param name
     * @param info
     */
    public void addNamedInformation(String name, Map info) {
        Map last = (Map)this.infoStack.peek();
        if ( last != null && name != null && info != null) {
            Iterator iter = info.entrySet().iterator();
            while ( iter.hasNext() ) {
                Map.Entry current = (Entry) iter.next();
                String key = current.getKey().toString();
                last.put('#'+name+':'+key, current.getValue());
            }
        }        
        this.calcInfo();
    }
    
    public static String xmlText(String text) {
        return "<![CDATA[" + text + "]]>";
    }
    
    public static String xmlElement(String name, String text) {
        return "<"+name+">"+xmlText(text)+"</"+name+">\n";        
    }

    /**
     * We wrap the request to remove the request parameter from the list
     * of parameters. This ensures that internal requests are not debugged.
     */
    protected final static class RequestWrapper extends AbstractRequestWrapper {

        public RequestWrapper(Request request) {
            super(request);
        }

        /**
         * @see org.apache.cocoon.environment.Request#getParameter(java.lang.String)
         */
        public String getParameter(String name) {
            if (REQUEST_PARAMETER.equals(name)) {
                return null;
            }
            return super.getParameter(name);
        }

        /**
         * @see org.apache.cocoon.environment.Request#getParameterNames()
         */
        public Enumeration getParameterNames() {
            // put all parameter names into a set
            Set parameterNames = new HashSet();
            Enumeration names = super.getParameterNames();
            while (names.hasMoreElements()) {
                String name = (String)names.nextElement();
                if (!REQUEST_PARAMETER.equals(name)) {
                    parameterNames.add(names.nextElement());
                }
            }
            return new EnumerationFromIterator(parameterNames.iterator());
        }

        final class EnumerationFromIterator implements Enumeration {
            private Iterator iter;
            EnumerationFromIterator(Iterator iter) {
                this.iter = iter;
            }

            public boolean hasMoreElements() {
                return iter.hasNext();
            }
            public Object nextElement() { return iter.next(); }
        }

        /**
         * @see org.apache.cocoon.environment.Request#getParameterValues(java.lang.String)
         */
        public String[] getParameterValues(String name) {
            if (REQUEST_PARAMETER.equals(name)) {
                return null;
            }
            return super.getParameterValues(name);
        }

        /**
         * @see org.apache.cocoon.environment.Request#getQueryString()
         */
        public String getQueryString() {
            String qs = super.getQueryString();
            int pos = qs.indexOf(Debugger.REQUEST_PARAMETER + '=');
            if (pos != -1) {
                int end = qs.indexOf("&", pos+1);
                if (end == -1) {
                    if (pos == 0) {
                        qs = null;
                    } else {
                        qs = qs.substring(0, pos-2);   
                    }
                } else {
                    if (pos == 0) {
                        qs = qs.substring(end+1);
                    } else {
                        qs = qs.substring(0, pos-2) + qs.substring(end);
                    }
                }
            }
            return qs;
        }

    }
}
