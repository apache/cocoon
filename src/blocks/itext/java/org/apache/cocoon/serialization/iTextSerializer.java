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
 * @version CVS $Id: iTextSerializer.java,v 1.5 2003/05/26 09:55:28 tcurdt Exp $
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

    public void recycle() {
        super.recycle();
    }

    public boolean shouldSetContentLength() {
        return this.setContentLength;
    }
}
