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
package org.apache.cocoon.components.source.impl;


import java.io.Serializable;

import org.apache.cocoon.caching.CachedResponse;
import org.apache.excalibur.source.SourceValidity;

/**
 * Cached response for caching Source contents and extra meta information.
 */
public final class CachedSourceResponse extends CachedResponse {
    
    private byte[] m_binary;
    private byte[] m_xml;
    private Serializable m_extra;
    
    public CachedSourceResponse(SourceValidity validity) {
        super(validity, null);
    }
    
    public byte[] getBinaryResponse() {
        return m_binary;
    }
    
    public void setBinaryResponse(byte[] binary) {
        m_binary = binary;
    }
    
    public byte[] getXMLResponse() {
        return m_xml;
    }
    
    public void setXMLResponse(byte[] xml) {
        m_xml = xml;
    }
    
    public void setExtra(Serializable extra) {
        m_extra = extra;
    }
    
    public Serializable getExtra() {
        return m_extra;
    }
    
}
