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
 * @version CVS $Id: XScriptObject.java,v 1.4 2004/03/08 14:01:54 cziegeler Exp $
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
