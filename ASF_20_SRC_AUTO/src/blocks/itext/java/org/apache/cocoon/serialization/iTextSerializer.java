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
package org.apache.cocoon.serialization;

import java.io.OutputStream;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.caching.CacheableProcessingComponent;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

import org.xml.sax.SAXException;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.xml.SAXiTextHandler;

/**
 * @author <a href="mailto:tcurdt@dff.st">Torsten Curdt</a>
 * @version CVS $Id: iTextSerializer.java,v 1.7 2004/03/05 13:01:57 bdelacretaz Exp $
 */
final public class iTextSerializer extends AbstractSerializer implements Configurable, CacheableProcessingComponent {

    private final static boolean LANDSCAPE = true;
    private final static boolean PORTRAIT = false;

    private String mimetype = "application/pdf";
    private boolean setContentLength = true;
    private Rectangle pageSize;
    private boolean pageOrientation;
    private Document document = null;

    private Rectangle getPageSize(final String s) throws ConfigurationException {
        // TC: we could use reflection here instead
        if ("letter".equalsIgnoreCase(s)) {
            return PageSize.LETTER;
        }
        else if ("a4".equalsIgnoreCase(s)) {
            return PageSize.A4;
        }
        else if ("a5".equalsIgnoreCase(s)) {
            return PageSize.A5;
        }
        else {
            throw new ConfigurationException("page size [" + String.valueOf(s) + "] is not yet recognized");
        }
    }

    private boolean getOrientation(final String o) throws ConfigurationException {
        if ("portrait".equalsIgnoreCase(o)) {
            return PORTRAIT;
        }
        else if ("landscape".equalsIgnoreCase(o)) {
            return LANDSCAPE;
        }
        else {
            throw new ConfigurationException("orientation must be either portrait or landscape but is [" + String.valueOf(o) + "]");
        }
    }

    public void configure(Configuration conf) throws ConfigurationException {
        this.setContentLength = conf.getChild("set-content-length").getValueAsBoolean(true);
        this.mimetype = conf.getAttribute("mime-type");

        this.pageSize = getPageSize(conf.getAttribute("page-size","A4"));
        this.pageOrientation = getOrientation(conf.getAttribute("page-orientation","portrait"));

        if (pageOrientation == LANDSCAPE) {
            pageSize = pageSize.rotate();
        }

        getLogger().debug("iTextSerializer mime-type:" + mimetype);
    }

    public String getMimeType() {
        return mimetype;
    }

    public void startDocument() throws SAXException {
        getLogger().debug("starting PDF document");
        super.startDocument();
    }

    public void endDocument() throws SAXException {
        super.endDocument();
        getLogger().debug("finished PDF document");
    }

    public void setOutputStream(OutputStream out) {
        this.document = new Document(this.pageSize);

        try {
            PdfWriter.getInstance(document, out);
        }
        catch(Exception e) {
            getLogger().error("cannot create pdf writer instance",e);
            //TC: FIXME! shouldn't we throw an exception here? what kind?
        }

        SAXiTextHandler handler = new SAXiTextHandler(document);
        handler.setControlOpenClose(true);
        this.contentHandler = handler;
    }

    public java.io.Serializable getKey() {
        return "1";
    }

    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    public boolean shouldSetContentLength() {
        return this.setContentLength;
    }
}
