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
 * @version CVS $Id: Logicsheet.java,v 1.3 2004/02/06 22:24:40 joerg Exp $
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
