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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.CascadingIOException;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Text serializer converts XML into plain text.
 * It omits all XML tags and writes only character events to the output.
 * Internally, text serializer uses XML serializer with {@link OutputKeys#METHOD}
 * set to <code>text</code>.
 *
 * <p>Input document must have at least one element - root element - which
 * should wrap all the text inside it.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: TextSerializer.java,v 1.4 2003/12/06 21:22:09 cziegeler Exp $
 */
public class TextSerializer extends AbstractTextSerializer {

    /**
     * Set to true after first XML element
     */
    private boolean hasRootElement;

    /**
     * Set to true after first XML element
     */
    private boolean hadNoRootElement;

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        this.format.put(OutputKeys.METHOD, "text");
    }

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out) throws IOException {
        super.setOutputStream(out);
        try {
            TransformerHandler handler = this.getTransformerHandler();
            handler.getTransformer().setOutputProperties(format);
            handler.setResult(new StreamResult(this.output));
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
       } catch (Exception e) {
            final String message = "Cannot set TextSerializer outputstream";
            throw new CascadingIOException(message, e);
        }
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
            throws SAXException {
        this.hasRootElement = true;
        super.startElement(uri, loc, raw, a);
    }

    public void characters(char c[], int start, int len)
            throws SAXException {
        if (!this.hasRootElement) {
            this.hasRootElement = this.hadNoRootElement = true;
            getLogger().warn("Encountered text before root element. Creating <text> wrapper element.");
            super.startElement("", "text", "text", new AttributesImpl());
        }
        super.characters(c, start, len);
    }

    public void endDocument() throws SAXException {
        if (this.hadNoRootElement) {
            super.endElement("", "text", "text");
        }
        super.endDocument();
    }

    public void recycle() {
        super.recycle();
        this.hasRootElement = false;
        this.hadNoRootElement = false;
    }
}
