/* ========================================================================== *
 * Copyright (C) 2004-2005 Pier Fumagalli <http://www.betaversion.org/~pier/> *
 *                            All rights reserved.                            *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== */

package org.apache.cocoon.components.validation.jaxp;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

final class JaxpInput implements LSInput {
    
    private final InputSource input;
    private boolean cert = false;
    private String data = null;
    private String base = null;

    public JaxpInput(InputSource input) {
        if (input == null) throw new NullPointerException("Null InputSource");
        this.input = input;
    }

    public Reader getCharacterStream() {
        return this.input.getCharacterStream();
    }

    public void setCharacterStream(Reader reader) {
        this.input.setCharacterStream(reader);
    }

    public InputStream getByteStream() {
        return this.input.getByteStream();
    }

    public void setByteStream(InputStream stream) {
        this.input.setByteStream(stream);
    }

    public String getStringData() {
        return this.data; 
    }

    public void setStringData(String data) {
        this.data = data;
    }

    public String getSystemId() {
        return this.input.getSystemId();
    }

    public void setSystemId(String systemId) {
        this.input.setSystemId(systemId);
    }

    public String getPublicId() {
        return this.input.getPublicId();
    }

    public void setPublicId(String publicId) {
        this.input.setPublicId(publicId);
    }

    public String getBaseURI() {
        if (this.base != null) return this.base;
        return this.input.getSystemId();
    }

    public void setBaseURI(String base) {
        this.base = base;
    }

    public String getEncoding() {
        return this.input.getEncoding();
    }

    public void setEncoding(String encoding) {
        this.input.setEncoding(encoding);
    }

    public boolean getCertifiedText() {
        return this.cert;
    }

    public void setCertifiedText(boolean cert) {
        this.cert = cert;
    }
}