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

package org.apache.cocoon.woody.datatype;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.excalibur.testcase.ExcaliburTestCase;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.WrapperServiceManager;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DocumentWrapper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.ResourceSource;
import org.apache.excalibur.xml.sax.XMLizable;
import org.custommonkey.xmlunit.Diff;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Test case for Woody's DynamicSelectionList datatype.
 * @version CVS $Id: DynamicSelectionListTestCase.java,v 1.2 2003/10/19 17:20:56 ugo Exp $
 */
public class DynamicSelectionListTestCase extends ExcaliburTestCase {

    protected ServiceManager serviceManager;
    protected DatatypeManager datatypeManager;
    protected DocumentBuilder parser;

    /**
     * Construct a new test case.
     * @param name The test case's name.
     */
    public DynamicSelectionListTestCase(String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        serviceManager = new WrapperServiceManager(manager); 
        datatypeManager = (DatatypeManager) serviceManager.lookup(DatatypeManager.ROLE);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newDocumentBuilder();
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if (datatypeManager != null) {
            serviceManager.release(datatypeManager);
        }
        super.tearDown();
    }
    
    /**
     * Test the generateSaxFragment method.
     * @throws MalformedURLException
     * @throws ParserConfigurationException
     */
    public void testGenerateSaxFragment() throws Exception {
        DOMBuilder dest = new DOMBuilder();
        XMLizableSource source =
            new XMLizableSource("resource://org/apache/cocoon/woody/datatype/DynamicSelectionListTestCase.source.xml");
        Element datatypeElement = (Element) source.getDocument().getElementsByTagNameNS(Constants.WD_NS, "convertor").item(0);
        Datatype datatype = datatypeManager.createDatatype(datatypeElement, false);
        DynamicSelectionList list = 
            new DynamicSelectionList(datatype, null, serviceManager);
        list.generateSaxFragment(dest, Locale.ENGLISH, source);
        ResourceSource expectedSource =
            new ResourceSource("resource://org/apache/cocoon/woody/datatype/DynamicSelectionListTestCase.dest.xml");
        Document expected = parser.parse(expectedSource.getInputStream());
        assertEqual("Test if output is what is expected",
                expected, dest.getDocument());
    }

    /**
     * Check is the source document is equal to the one produced by the method under test.
     * @param message A message to print in case of failure.
     * @param expected The expected (source) document.
     * @param actual The actual (output) document.
     */
    private void assertEqual(String message, Document expected, Document actual) {
        expected.getDocumentElement().normalize();
        actual.getDocumentElement().normalize();
        // DIRTY HACK WARNING: we add the "xmlns:wi" attribute reported
        // by DOM, as expected, but not generated by the method under test,
        // otherwise the comparison would fail. 
        actual.getDocumentElement().setAttribute(Constants.WI_PREFIX,
                Constants.WI_NS);
        Diff diff =  new Diff(expected, actual);
        assertTrue(message + ", " + diff.toString(), diff.similar());
    }

    /**
     * Print a document to a writer for debugging purposes.
     * @param document The document to print.
     * @param out The writer to write to.
     */
    public final void print(Document document, Writer out) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try
        {
          javax.xml.transform.Transformer serializer = factory.newTransformer();
          serializer.transform(new DOMSource(document), new StreamResult(out));
        } 
        catch (TransformerException te)
        {
          te.printStackTrace();
        }
    }
    
    /**
     * A class that implements both the 
     * {@link org.apache.excalibur.xml.sax.XMLizable} and 
     * {@link org.apache.excalibur.source.Source} interfaces by delegating to a 
     * {@link org.apache.cocoon.xml.domDocumentWrapper} and to a 
     * {@link org.apache.excalibur.source.impl.ResourceSource},
     * respectively.
     */
    class XMLizableSource implements XMLizable, Source {

        private ResourceSource source;
        private Document document;
        private DocumentWrapper wrapper;
        
        /**
         * Create a new XMLizableSource from the provided resource URI.
         * @param uri An URI of the form "resource://..."
         * @throws SAXException
         * @throws IOException
         * @throws ParserConfigurationException
         * @throws FactoryConfigurationError
         */
        public XMLizableSource(String uri) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
            source = new ResourceSource(uri);
            document = parser.parse(source.getInputStream());
            wrapper = new DocumentWrapper(document);
        }
        
        /**
         * Return the document parsed from the source.
         * @return A DOM Document.
         */
        public Document getDocument() {
            return document;
        }

        // The following methods delegate either to the DocumentWrapper
        // or to the Source.        
        public void toSAX(ContentHandler handler) throws SAXException {
            wrapper.toSAX(handler);
        }

        public boolean equals(Object obj) {
            return source.equals(obj);
        }

        public boolean exists() {
            return source.exists();
        }

        public long getContentLength() {
            return source.getContentLength();
        }

        public InputStream getInputStream()
            throws IOException, SourceNotFoundException {
            return source.getInputStream();
        }

        public long getLastModified() {
            return source.getLastModified();
        }

        public String getMimeType() {
            return source.getMimeType();
        }

        public String getScheme() {
            return source.getScheme();
        }

        public String getURI() {
            return source.getURI();
        }

        public SourceValidity getValidity() {
            return source.getValidity();
        }

        public int hashCode() {
            return source.hashCode();
        }

        public void refresh() {
            source.refresh();
        }

        public String toString() {
            return source.toString();
        }

    }
}
