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
package org.apache.cocoon.components.language.markup;

import org.apache.excalibur.xml.xslt.XSLTProcessor;
import org.apache.excalibur.xml.xslt.XSLTProcessorException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.sax.TransformerHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * A code-generation logicsheet. This class is actually a wrapper for
 * a "standard" XSLT stylesheet stored as <code>trax.Templates</code>
 * object.  Though this will change shortly: a new markup language
 * will be used for logicsheet authoring; logicsheets written in this
 * language will be transformed into an equivalent XSLT stylesheet
 * anyway... This class should probably be based on an interface...
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: Logicsheet.java,v 1.4 2004/03/08 13:58:31 cziegeler Exp $
 */
public class Logicsheet extends AbstractLogEnabled
{
    /**
     * The Source Resolver object for this logicsheet.
     */
    private SourceResolver resolver;

    /**
     * The system id to resolve
     */
    private String systemId;

    /**
     * the template namespace's list
     */
    protected Map namespaceURIs = new HashMap();

    /**
     * The ServiceManager of this instance.
     */
    private ServiceManager manager;

    public Logicsheet(Source source, ServiceManager manager, SourceResolver resolver)
        throws SAXException, IOException, ProcessingException
    {
        this.resolver = resolver;
        this.systemId = source.getURI();
        this.manager = manager;
    }

    public Logicsheet(String systemId, ServiceManager manager, SourceResolver resolver)
        throws SAXException, IOException, SourceException, ProcessingException
    {
        this.resolver = resolver;
        this.manager = manager;
        Source source = null;
        try {
            source = this.resolver.resolveURI( systemId );
            this.systemId = source.getURI();
        } finally {
            this.resolver.release( source );
        }
    }

    public String getSystemId()
    {
        return this.systemId;
    }

    /**
     * This will return the list of namespaces in this logicsheet.
     */
    public Map getNamespaceURIs() throws ProcessingException
    {
        // Force the parsing of the Source or, if nothing changed,
        // return the old content of namespaces.
        getTransformerHandler();
        return namespaceURIs;
    }

    /**
     * Obtain the TransformerHandler object that will perform the
     * transformation associated with this logicsheet.
     *
     * @return a <code>TransformerHandler</code> value
     */
    public TransformerHandler getTransformerHandler() throws ProcessingException
    {
        XSLTProcessor xsltProcessor = null;
        Source source = null;
        try {
            xsltProcessor = (XSLTProcessor)this.manager.lookup(XSLTProcessor.ROLE);
            source = this.resolver.resolveURI( this.systemId );

            // If the Source object is not changed, the
            // getTransformerHandler() of XSLTProcessor will simply return
            // the old template object. If the Source is unchanged, the
            // namespaces are not modified either.
            XMLFilter saveNSFilter = new SaveNamespaceFilter(namespaceURIs);
            return xsltProcessor.getTransformerHandler(source, saveNSFilter);

        } catch (ServiceException e) {
            throw new ProcessingException("Could not obtain XSLT processor", e);
        } catch (MalformedURLException e) {
            throw new ProcessingException("Could not resolve " + this.systemId, e);
        } catch (SourceException e) {
            throw SourceUtil.handle("Could not resolve " + this.systemId, e);
        } catch (IOException e) {
            throw new ProcessingException("Could not resolve " + this.systemId, e);
        } catch (XSLTProcessorException e) {
            throw new ProcessingException("Could not transform " + this.systemId, e);
        } finally {
            this.manager.release(xsltProcessor);
            // Release used resources
            this.resolver.release( source );
        }
    }

    /**
     * This filter listen for source SAX events, and register the declared
     * namespaces into a <code>Map</code> object.
     *
     * @see org.xml.sax.XMLFilter
     * @see org.xml.sax.ContentHandler
     */
    protected class SaveNamespaceFilter extends XMLFilterImpl {
        private Map originalNamepaceURIs;

        /**
         * The contructor needs an initialized <code>Map</code> object where it
         * can store the found namespace declarations.
         * @param originalNamepaceURIs a initialized <code>Map</code> instance.
         */
        public SaveNamespaceFilter(Map originalNamepaceURIs) {
            this.originalNamepaceURIs = originalNamepaceURIs;
        }

        public void setParent(XMLReader reader) {
            super.setParent(reader);
            reader.setContentHandler(this);
        }

        public void startDocument() throws SAXException {
            super.startDocument();
        }

        public void startPrefixMapping(String prefix, String uri)
            throws SAXException
        {
            originalNamepaceURIs.put(uri, prefix);
            super.startPrefixMapping(prefix, uri);
        }

        public void startElement (String namespaceURI, String localName,
                                  String qName, Attributes atts)
            throws SAXException
        {
            super.startElement(namespaceURI, localName, qName, atts);
        }
    }
}
