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
package org.apache.cocoon.components.xscript;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.EmbeddedXMLPipe;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Date;

/**
 * <code>XScriptObject</code> is the root class implemented by all the
 * object types in XScript. Every XScriptObject is essentially a
 * Source object.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: XScriptObject.java,v 1.3 2004/02/07 15:20:09 joerg Exp $
 * @since August  4, 2001
 */
public abstract class XScriptObject implements Source, Serviceable {

    /**
     * The creation date of this <code>XScriptObject</code>.
     */
    Date lastModifiedDate = new Date();

    /**
     * The <code>XScriptManager</code> object that's managing this
     * <code>XScriptObject</code> value.
     */
    XScriptManager xscriptManager;

    protected ServiceManager serviceManager;

    /**
     * Creates a new <code>XScriptObject</code> instance.
     *
     * @param manager a <code>XScriptManager</code> value
     */
    public XScriptObject(XScriptManager manager) {
        this.xscriptManager = manager;
        ((XScriptManagerImpl) this.xscriptManager).register(this);
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;
    }

    /**
     * Apply the XSLT stylesheet defined by the <code>stylesheet</code>
     * variable to this instance. Return the result of the
     * transformation as an <code>XScriptObject</code>.
     *
     * @param stylesheet a <code>XScriptObject</code> value
     * @param params a <code>Parameters</code> value containing optional
     * arguments to be passed to the XSLT processor.
     * @return <code>XScriptObject</code> object containing the result
     * of the XSLT processing.
     * @exception IllegalArgumentException if an error occurs
     * @exception ProcessingException if an error occurs
     */
    public XScriptObject transform(XScriptObject stylesheet, Parameters params)
            throws IllegalArgumentException, ProcessingException {
        try {
            CharArrayWriter writer = new CharArrayWriter();
            StreamResult result = new StreamResult(writer);

            XSLTProcessor transformer
                    = (XSLTProcessor) serviceManager.lookup(XSLTProcessor.ROLE);

            try {
                transformer.transform(this, stylesheet, params, result);
            } finally {
                serviceManager.release(transformer);
            }

            return new XScriptObjectResult(xscriptManager, writer.toString());
        } catch (XSLTProcessorException ex) {
            throw new ProcessingException(ex);
        } catch (Exception ex) {
            throw new ProcessingException(ex);
        }
    }

    public void toEmbeddedSAX(ContentHandler handler) throws SAXException {
        EmbeddedXMLPipe newHandler = new EmbeddedXMLPipe(handler);
        toSAX(newHandler);
    }

    /* The Source interface methods. */

    public void toSAX(ContentHandler handler) throws SAXException {
        SAXParser parser = null;
        try {
            parser = (SAXParser) serviceManager.lookup(SAXParser.ROLE);
            InputSource source = getInputSource();
            parser.parse(source, handler);
        } catch (SAXException e) {
            throw e;
        } catch (Exception e) {
            throw new SAXException(e);
        } finally {
            if (parser != null) {
                serviceManager.release(parser);
            }
        }
    }

    public long getLastModified() {
        return lastModifiedDate.getTime();
    }

    public abstract long getContentLength();

    public InputSource getInputSource() throws ProcessingException, IOException {
        InputSource is = new InputSource(getInputStream());
        is.setSystemId(getURI());
        return is;
    }

    public void recycle() {
    }

    public String getScheme() {
        return "xscript";
    }

    public void refresh() {
    }

    public String getMimeType() {
       return "text/xml";
    }

    public SourceValidity getValidity() {
        return null;
    }

    public boolean exists() {
        return true;
    }
}
