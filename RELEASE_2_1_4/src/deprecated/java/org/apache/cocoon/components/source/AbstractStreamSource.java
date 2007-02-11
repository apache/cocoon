/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache Cocoon" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.cocoon.components.source;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ModifiableSource;
import org.apache.cocoon.util.ClassUtils;
import org.apache.excalibur.xml.sax.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * This abstract class provides convenience methods to implement
 * a stream based Source. Implement getInputStream(), getSystemId() and
 * optionally override refresh(), recycle(), getLastModified() and
 * getContentLength() to obtain a valid Source implementation.
 * <p>
 * This base implementation provides services to parse HTML sources
 * (HTML is not valid XML) using JTidy, if present. The source is
 * considered to contain HTML if <code>isHTMLContent()</code> returns
 * true.
 *
 * @deprecated Use the new Avalon Excalibur Source Resolving
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractStreamSource.java,v 1.4 2003/12/23 15:28:33 joerg Exp $
 */
public abstract class AbstractStreamSource extends AbstractLogEnabled
    implements ModifiableSource {

    /** Is JTidy available? */
    private static Class jtidyClass;

    /** Properties used for converting HTML to XML */
    private static Properties xmlProperties;

    /** The TrAX factory for serializing xml */
    public static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    /**
     * Test if JTidy is available
     */
    static {
        jtidyClass = null;
        try {
            jtidyClass = ClassUtils.loadClass("org.w3c.tidy.Tidy");
        } catch (ClassNotFoundException cnfe) {
            // ignore
        }
        xmlProperties = new Properties();
        xmlProperties.put(OutputKeys.METHOD, "xml");
        xmlProperties.put(OutputKeys.OMIT_XML_DECLARATION, "no");
    }

    /** The ComponentManager needed for streaming */
    protected ComponentManager manager;

    /**
     * Construct a new object
     */
    protected AbstractStreamSource(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Does this source contain HTML ? If true, JTidy will be used (if available) to
     * parse the input as XML.
     * <p>
     * The default here is to return false. Concrete subclasses should override
     * this if needed.
     */
    protected boolean isHTMLContent() {
        return false;
    }

    /**
     * Return a new <code>InputSource</code> object
     */
    public InputSource getInputSource() throws IOException, ProcessingException {

        InputStream stream = this.getInputStream();
        if (jtidyClass != null && isHTMLContent()) {
            try {
                final Object xhtmlconvert = jtidyClass.newInstance();
                Method m = jtidyClass.getMethod("setXmlOut", new Class[] { Class.forName("java.lang.Boolean")});
                m.invoke(xhtmlconvert, new Object[] { new Boolean(true) });
                m = jtidyClass.getMethod("setXHTML", new Class[] {Class.forName("java.lang.Boolean")});
                m.invoke(xhtmlconvert, new Object[] { new Boolean(true) });
                m = jtidyClass.getMethod("setShowWarnings", new Class[] { Class.forName("java.lang.Boolean")});
                m.invoke(xhtmlconvert, new Object[] { new Boolean(false) });
                m = jtidyClass.getMethod("parseDOM", new Class[] { Class.forName("java.io.InputStream"), Class.forName("java.io.OutputStream")});
                final Document doc = (Document)m.invoke(xhtmlconvert, new Object[] { stream, null });
                final StringWriter writer = new StringWriter();
                final Transformer transformer;
                transformer = transformerFactory.newTransformer();
                transformer.setOutputProperties(xmlProperties);
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                final String xmlstring = writer.toString();
                InputSource newObject = new InputSource(new java.io.StringReader(xmlstring));
                newObject.setSystemId(this.getSystemId());
                return newObject;
            } catch (Exception ignore) {
                // Let someone else worry about what we got . This is as before.
                this.refresh();
                stream = this.getInputStream();
            }
        }
        InputSource newObject = new InputSource(stream);
        newObject.setSystemId(this.getSystemId());
        return newObject;
    }

    /**
     * Stream content to a content handler or to an XMLConsumer.
     *
     * @throws SAXException if failed to parse source document.
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        SAXParser parser = null;
        try {
            parser = (SAXParser)this.manager.lookup(SAXParser.ROLE);

            parser.parse( this.getInputSource(), handler);
        } catch (SAXException e) {
            // Preserve original exception
            throw e;
        } catch (Exception e){
            throw new SAXException("Exception during processing of "
                                          + this.getSystemId(), e);
        } finally {
            if (parser != null) this.manager.release( (Component)parser);
        }
    }

    /**
     * Override this method to set the Content Length
     *
     */
    public long getContentLength() {
      return -1;
    }

    /**
     * Override this method to set the Last Modification date
     *
     */
    public long getLastModified() {
      return 0;
    }

    /**
     * Returns <code>true</code> if <code>getInputStream()</code> succeeds.
     * Subclasses can provide a more efficient implementation.
     */
    public boolean exists() {
        try {
            InputStream stream = getInputStream();
            stream.close();
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * To be overriden in concrete subclasses if needed.
     */
    public void recycle() {
    }

    /**
     * To be overriden in concrete subclasses if needed.
     */
    public void refresh() {
    }
}
