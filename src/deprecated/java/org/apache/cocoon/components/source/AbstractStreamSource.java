/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 * @version CVS $Id: AbstractStreamSource.java,v 1.6 2004/03/05 13:02:40 bdelacretaz Exp $
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
                m.invoke(xhtmlconvert, new Object[] { Boolean.TRUE });
                m = jtidyClass.getMethod("setXHTML", new Class[] {Class.forName("java.lang.Boolean")});
                m.invoke(xhtmlconvert, new Object[] { Boolean.TRUE });
                m = jtidyClass.getMethod("setShowWarnings", new Class[] { Class.forName("java.lang.Boolean")});
                m.invoke(xhtmlconvert, new Object[] { Boolean.FALSE });
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
