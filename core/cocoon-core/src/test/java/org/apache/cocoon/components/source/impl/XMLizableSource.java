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
package org.apache.cocoon.components.source.impl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Proxy like source adding XMLizable.
 *
 * @version $Id$
 */
public class XMLizableSource implements XMLizable, Source {

    public static final String SCHEME = "xml";
    
    private Source m_source;
    
    private ServiceManager m_manager;
    
    public XMLizableSource(Source source, ServiceManager manager) {
        m_source = source;
        m_manager = manager;
    }
    
    public Source getSource() {
        return m_source;
    }
    
    public String getSourceURI() {
        return m_source.getURI();
    }
    
    public boolean exists() {
        return m_source.exists();
    }

    public InputStream getInputStream() throws IOException {
        return m_source.getInputStream();
    }

    public String getURI() {
        return SCHEME + ":" + m_source.getURI();
    }

    public String getScheme() {
        return SCHEME;
    }

    public SourceValidity getValidity() {
        return m_source.getValidity();
    }

    public void refresh() {
        m_source.refresh();
    }

    public String getMimeType() {
        return m_source.getMimeType();
    }

    public long getContentLength() {
        return m_source.getContentLength();
    }

    public long getLastModified() {
        return m_source.getLastModified();
    }

    public void toSAX(ContentHandler handler) throws SAXException {
        try {
            SourceUtil.toSAX(m_manager, m_source, "text/xml", handler);
        } catch (Exception e) {
            throw new SAXException("Failure during toSAX",e);
        }
    }
}
