/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Base implementation of <code>MarkupLanguage</code>. This class uses
 * logicsheets as the only means of code generation. Code generation
 * should be decoupled from this context!!!
 *
 * @version $Id$
 */
public abstract class CocoonMarkupLanguage extends AbstractMarkupLanguage {

    /**
     * Store the dependencies of the currently loaded program.
     */
    private final Set dependencies = new HashSet();

    /** The default constructor. */
    public CocoonMarkupLanguage() {
    }

    /**
     * Recycle this component: clear logic sheet list and dependencies.
     */
    public void recycle() {
        this.dependencies.clear();
        super.recycle();
    }

    /**
     * Prepare the input source for logicsheet processing and code generation
     * with a preprocess filter.
     * The return <code>XMLFilter</code> object is the first filter on the
     * transformer chain.
     *
     * The markup language preprocess filter adds information on the root element
     * such as creation-date, file-name and file-path, plus it use the the passed
     * programming language to quote <code>Strings</code> on PCDATA node.
     *
     * @param filename The source filename
     * @param language The target programming language
     * @return The preprocess filter
     *
     * @see PreProcessFilter
     */
    protected AbstractXMLPipe getPreprocessFilter(String filename,
                                                  AbstractXMLPipe filter,
                                                  ProgrammingLanguage language) {
        return new PreProcessFilter(filter, filename, language);
    }

    /**
     * Returns a filter that chain on the fly the requested transformers for source
     * code generation. This method scans the input SAX events for
     * &lt;?xml-logicsheet?&gt; processing instructions and top-level
     * &lt;prefix:logicsheet&gt; elements. Logicsheet declarations are removed from
     * the input document.
     *
     * @param logicsheetMarkupGenerator the logicsheet markup generator
     * @return XMLFilter the filter that build on the fly the transformer chain
     */
    protected TransformerChainBuilderFilter getTransformerChainBuilder(LogicsheetCodeGenerator logicsheetMarkupGenerator)
    {
        return new CocoonTransformerChainBuilderFilter(logicsheetMarkupGenerator);
    }

    // This is required here to avoid IllegalAccessError when
    // CocoonTransformerChainBuilderFilter invokes the method.
    protected void addLogicsheetToList(LanguageDescriptor language,
                                       String logicsheetLocation)
    throws IOException, SAXException, ProcessingException
    {
        super.addLogicsheetToList(language, logicsheetLocation);
    }

    /**
     * Add a dependency on an external file to the document for inclusion in
     * generated code. This is used to populate a list of <code>File</code>'s
     * tested for change on each invocation; this information is used to assert
     * whether regeneration is necessary. XSP uses &lt;xsp:dependency&gt;
     * elements for this purpose.
     *
     * @param location The file path of the dependent file
     * @see AbstractMarkupLanguage
     * @see org.apache.cocoon.generation.ServerPagesGenerator
     * @see org.apache.cocoon.generation.AbstractServerPage
     */
    protected void addDependency(String location) {
        dependencies.add(location);
    }

    /**
     * Returns the root element for this language.
     */
    public abstract String getRootElement();


    //
    //  Inner classes
    //

    /**
     * Preprocess filter for Cocoon Markup languages.
     * It looks for PI event other that &lt;?xml-logisheet href=&quot;...&quot;&gt;
     * for quoting them;
     * It adds creation-date, file-name and file-path attributes to the root
     * Element;
     * And it quotes the PCDATA based by calling the quote method of the
     * programming language.
     *
     * @see org.xml.sax.ContentHandler
     */
    public class PreProcessFilter extends AbstractXMLPipe {

        protected AbstractXMLPipe filter;

        protected String filename;

        protected boolean isRootElem;

        protected ProgrammingLanguage language;

        protected String localPrefix;

        /**
         * @param filename the filename
         * @param language the programming language
         */
        public PreProcessFilter (AbstractXMLPipe filter, String filename, ProgrammingLanguage language) {
            super();

            this.filename = filename;
            this.language = language;
            this.filter = filter;

            // Put meself in front of filter
            super.setLexicalHandler(this.filter);
            super.setContentHandler(this.filter);
        }

        public void setConsumer(XMLConsumer consumer) {
            // Add consumer after filter
            this.filter.setConsumer(consumer);
        }

        public void setContentHandler(ContentHandler handler) {
            this.filter.setContentHandler(handler);
        }

        public void setLexicalHandler(LexicalHandler handler) {
            this.filter.setLexicalHandler(handler);
        }

        public void startDocument() throws SAXException {
            super.startDocument();
            isRootElem = true;
        }

        public void processingInstruction(String target, String data) throws SAXException {
            if (!"xml-logicsheet".equals(target)) {
                data = this.language.quoteString(data);
            }
            super.processingInstruction(target, data);
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (CocoonMarkupLanguage.this.getURI().equals(uri)) {
                this.localPrefix = prefix;
            }
            super.startPrefixMapping(prefix, uri);
        }

        public void startElement (String namespaceURI, String localName,
                          String qName, Attributes atts) throws SAXException {
             if (isRootElem) {
                 if (!CocoonMarkupLanguage.this.getURI().equals(namespaceURI) ||
                         !CocoonMarkupLanguage.this.getRootElement().equals(localName))
                 {
                     throw new SAXException("This page is not valid page of this markup langugage."
                             + " Root element is: " + namespaceURI + ":" + localName
                             + ", must be: " + CocoonMarkupLanguage.this.getURI()
                             + ":" + CocoonMarkupLanguage.this.getRootElement());
                 }

                 isRootElem=false;
                 // Store path and file name
                 int pos = this.filename.lastIndexOf(File.separatorChar);
                 String name = this.filename.substring(pos + 1);
                 String path = this.filename.substring(0, pos).replace(File.separatorChar, '/');
                 // update the attributes
                 AttributesImpl newAtts;
                 if (atts == null || atts.getLength() == 0) {
                     newAtts = new AttributesImpl();
                 } else {
                     newAtts = new AttributesImpl(atts);
                 }
                 newAtts.addAttribute("", "file-name", "file-name", "CDATA", name);
                 newAtts.addAttribute("", "file-path", "file-path", "CDATA", path);
                 newAtts.addAttribute("", "creation-date", "creation-date", "CDATA",
                         String.valueOf(System.currentTimeMillis()));
                 // forward element with the modified attribute
                 super.startElement(namespaceURI, localName, qName, newAtts);
            } else {
                super.startElement(namespaceURI, localName, qName, atts);
            }
        }
    }

    /**
     * This filter builds on the fly a chain of transformers. It extends the
     * <code>AbstractMarkupLanguage.TransformerChainBuilderFilter</code> so
     * it can add common markup language features such as:
     * <ul>
     * <li>Looking for &lt;?xml-logisheet href=&quot;...&quot?;&gt; PI and
     *     &lt;xsp:xml-logisheet location=&quot;...&quot;&gt; elements to register
     *     user defined logicsheets;</li>
     * <li>Adding all the dependencies related to the pages as
     *     &lt;xsp:dependency;&gt;...&lt;/xsp:dependency;&gt;</li>
     * </ul>
     *
     * @see org.xml.sax.ContentHandler
     */
    public class CocoonTransformerChainBuilderFilter extends TransformerChainBuilderFilter {

        private List startPrefix;

        private Object[] rootElement;

        private StringBuffer rootChars;

        private boolean isRootElem;

        private boolean insideRootElement;

        private boolean finished;

        private String localPrefix;

        /**
         * @param logicsheetMarkupGenerator the code generator
         */
        public CocoonTransformerChainBuilderFilter(
            LogicsheetCodeGenerator logicsheetMarkupGenerator)
        {
            super(logicsheetMarkupGenerator);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            // Retrieve logicsheets declared by processing-instruction
            if ("xml-logicsheet".equals(target)) {
                int start = data.indexOf("href");
                if (start >= 0) {
                    // add 6, for lenght of 'href', plus '=' char, plus '"' char
                    start += 6;
                    // get the quote char. Can be " or '
                    char quote = data.charAt(start-1);
                    int end = data.indexOf(quote, start);
                    String href = data.substring(start, end);

                    try {
                        CocoonMarkupLanguage.this.addLogicsheetToList(language, href);
                    } catch (ProcessingException pe) {
                        getLogger().warn("ProcessingException in SitemapMarkupLanguage", pe);
                        throw new SAXException (pe);
                    } catch (IOException ioe) {
                        getLogger().warn("CocoonMarkupLanguage.processingInstruction", ioe);
                        throw new SAXException (ioe);
                    }
                }
                // Do not forward the PI event.
                return;
            }

            // Call super when this is not a logicsheet related PI
            super.processingInstruction(target,data);
        }

        public void startDocument() throws SAXException {
            isRootElem = true;
            insideRootElement = false;
            finished = false;
            startPrefix = new ArrayList();
            rootChars = new StringBuffer();
        }

        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) throws SAXException {
            if (finished) {
                // Call super method
                super.startElement(namespaceURI, localName, qName, atts);
            } else {
                // Need more work
                if(isRootElem) {
                    localPrefix = "";
                    if (qName.indexOf(':') != -1)
                       localPrefix = qName.substring(0, qName.indexOf(':'));

                    isRootElem = false;
                    // Cache the root element and resend the SAX event when
                    // we've finished dealing with <xsp:logicsheet > elements
                    rootElement = new Object[4];
                    rootElement[0]=namespaceURI;
                    rootElement[1]=localName;
                    rootElement[2]=qName;
                    rootElement[3]=atts;
                } else {
                    insideRootElement = true;
                    // Retrieve logicsheets declared by top-level elements <xsp:logicsheet ...>
                    // And do not forward the startElement event
                    if (CocoonMarkupLanguage.this.getURI().equals(namespaceURI)
                            && "logicsheet".equals(localName)) {
                        String href = atts.getValue("location");
                        try {
                            CocoonMarkupLanguage.this.addLogicsheetToList(language, href);
                        } catch (ProcessingException pe) {
                            getLogger().warn("CocoonMarkupLanguage.startElement", pe);
                            throw new SAXException (pe);
                        } catch (IOException ioe) {
                            getLogger().warn("CocoonMarkupLanguage.startElement", ioe);
                            throw new SAXException (ioe);
                        }
                    } else {
                        // This element is not a <xsp:logicsheet> element, so finish
                        // by:
                        // * setting the 'fisnished' flag to true ;
                        // * refiring all the cached events ;
                        // * firing all the necessary event dealing with file dependencies
                        finished = true;

                        // Send SAX events 'startDocument'
                        super.startDocument();

                        // Send all prefix namespace
                        String [] prefixArray;
                        for (int i=0; i<startPrefix.size(); i++) {
                            prefixArray = (String []) startPrefix.get(i);
                            super.startPrefixMapping(prefixArray[0], prefixArray[1]);
                        }

                        // Send cached RootElement event
                        super.startElement(
                            (String) rootElement[0],
                            (String) rootElement[1],
                            (String) rootElement[2],
                            (Attributes) rootElement[3]
                        );

                        // Send cached characters
                        char[] ch = rootChars.toString().toCharArray();
                        if (ch.length > 0) {
                            super.characters(ch, 0, ch.length);
                        }

                        // Send the events dealing with dependencies.
                        // If some dependencies exist, then creates
                        // <xsp:dependency> elements
                        char[] locationChars;
                        Iterator iter = CocoonMarkupLanguage.this.dependencies.iterator();
                        while(iter.hasNext()) {
                            super.startElement(
                                (String) rootElement[0], "dependency", localPrefix + ":dependency", XMLUtils.EMPTY_ATTRIBUTES
                            );
                            locationChars = ((String) iter.next()).toCharArray();
                            super.characters(locationChars, 0 , locationChars.length);
                            super.endElement((String)rootElement[0], "dependency", localPrefix + ":dependency");
                        }

                        // And finally forward current Element.
                        super.startElement(namespaceURI, localName, qName, atts);
                    }
                }
            }
        }

        public void endElement(String namespaceURI, String localName,
                               String qName) throws SAXException {
            if (finished) {
                // Forward the events
                super.endElement(namespaceURI, localName, qName);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (finished) {
                super.characters(ch, start, length);
            } else if(!insideRootElement) {
                // Caching the PCDATA for the root element
                rootChars.append(ch, start, length);
            }
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if(finished) {
                super.startPrefixMapping(prefix, uri);
            } else {
                String[] prefixArray = new String [2];
                prefixArray[0]= prefix;
                prefixArray[1]= uri;
                startPrefix.add(prefixArray);
            }
        }
    }
}
