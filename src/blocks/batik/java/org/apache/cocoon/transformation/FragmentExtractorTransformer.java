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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.sax.XMLSerializer;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.util.HashUtil;

import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.excalibur.store.Store;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The transformation half of the FragmentExtractor.
 *
 * This transformer recieves an incoming stream of xml and replaces
 * fragments with an fragment extractor locator pointing to the fragments.
 *
 * The extracted fragments are identified by their element name and namespace URI.
 * The default is to extract SVG images ("svg" elements in namespace
 * "http://www.w3.org/2000/svg"), but this can be overriden in the configuration:
 * <pre>
 *   &lt;extract-uri&gt;http://my/namespace/uri&lt;/extract-uri&gt;
 *   &lt;extract-element&gt;my-element&lt;/extract-element&gt;
 * </pre>
 *
 * Fragment extractor locator format is following:
 * <pre>
 *   &lt;fe:fragment xmlns:fe="http://apache.org/cocoon/fragmentextractor/2.0" fragment-id="..."/&gt;
 * </pre>
 *
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Id: FragmentExtractorTransformer.java,v 1.9 2004/01/22 00:02:34 vgritsenko Exp $
 */
public class FragmentExtractorTransformer extends AbstractTransformer
    implements CacheableProcessingComponent, Configurable, Serviceable, Disposable, Recyclable {

    public static final String FE_URI = "http://apache.org/cocoon/fragmentextractor/2.0";

    private static final String EXTRACT_URI_NAME = "extract-uri";
    private static final String EXTRACT_ELEMENT_NAME = "extract-element";

    private static final String EXTRACT_URI = "http://www.w3.org/2000/svg";
    private static final String EXTRACT_ELEMENT = "svg";

    private String extractURI;
    private String extractElement;

    /** The ServiceManager instance */
    protected ServiceManager manager;

    private XMLSerializer serializer;

    private Map prefixMap;

    private int extractLevel;

    private int fragmentID;

    private String requestURI;

    /**
     * Configure this transformer.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.extractURI = conf.getChild(EXTRACT_URI_NAME).getValue(EXTRACT_URI);
        this.extractElement = conf.getChild(EXTRACT_ELEMENT_NAME).getValue(EXTRACT_ELEMENT);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Extraction URI is " + this.extractURI);
            getLogger().debug("Extraction element is " + this.extractElement);
        }
    }

    /**
     * Set the current <code>ServiceManager</code> instance used by this
     * <code>Serviceable</code>.
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Recycle this component
     */
    public void recycle() {
        if (this.manager != null) {
            this.manager.release(serializer);
            this.serializer = null;
        }
        super.recycle();        
    }

    /**
     * Release all resources.
     */
    public void dispose() {
        recycle();
        this.manager = null;
    }

    /**
     * Setup the transformer.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        extractLevel = 0;
        fragmentID = 0;
        prefixMap = new HashMap();

        this.requestURI = ObjectModelHelper.getRequest(objectModel).getSitemapURI();
    }

    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return "1"
     */
    public java.io.Serializable getKey() {
        return "1";
    }

    /**
     * Generate the validity object.
     *
     * @return NOPValidity object
     *         - if the input is valid the output is valid as well.
     */
    public SourceValidity getValidity() {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (extractLevel == 0) {
            super.startPrefixMapping(prefix, uri);
            prefixMap.put(prefix, uri);
        } else {
            this.serializer.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (extractLevel == 0) {
            super.endPrefixMapping(prefix);
            prefixMap.remove(prefix);
        } else {
            this.serializer.endPrefixMapping(prefix);
        }
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if (this.extractURI.equals(uri) && this.extractElement.equals(loc)) {
            extractLevel++;
            fragmentID++;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("extractLevel now " + extractLevel + ".");
            }

            try {
                this.serializer = (XMLSerializer) this.manager.lookup(XMLSerializer.ROLE);
            } catch (ServiceException se) {
                throw new SAXException("Could not lookup for XMLSerializer.", se);
            }

            // Start the DOM document
            this.serializer.startDocument();

            Iterator itt = prefixMap.entrySet().iterator();
            while (itt.hasNext()) {
                Map.Entry entry = (Map.Entry)itt.next();
                this.serializer.startPrefixMapping(
                    (String)entry.getKey(),
                    (String)entry.getValue()
                );
            }
        }

        if (extractLevel == 0) {
            super.startElement(uri, loc, raw, a);
        } else {
            this.serializer.startElement(uri, loc, raw, a);
        }
    }


    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (extractLevel == 0) {
            super.endElement(uri, loc, raw);
        } else {
            this.serializer.endElement(uri, loc, raw);
            if (this.extractURI.equals(uri) && this.extractElement.equals(loc)) {
                extractLevel--;
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("extractLevel now " + extractLevel + ".");
                }

                if (extractLevel == 0) {
                    // finish building the fragment. remove existing prefix mappings.
                    Iterator itt = prefixMap.entrySet().iterator();
                    while (itt.hasNext()) {
                        Map.Entry entry = (Map.Entry) itt.next();
                        this.serializer.endPrefixMapping(
                            (String)entry.getKey()
                        );
                    }
                    this.serializer.endDocument();

                    Store store = null;
                    String id = Long.toHexString((hashCode()^HashUtil.hash(requestURI)) + fragmentID);
                    try {
                        store = (Store) this.manager.lookup(Store.TRANSIENT_STORE);
                        store.store(id, this.serializer.getSAXFragment());
                    } catch (ServiceException se) {
                        throw new SAXException("Could not lookup for transient store.", se);
                    } catch (IOException ioe) {
                        throw new SAXException("Could not store fragment.", ioe);
                    } finally {
                        this.manager.release(store);
                        this.manager.release(this.serializer);
                        this.serializer = null;
                    }

                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Stored document " + id + ".");
                    }

                    // Insert ref.
                    super.startPrefixMapping("fe", FE_URI);
                    AttributesImpl atts = new AttributesImpl();
                    atts.addAttribute("", "fragment-id", "fragment-id", "CDATA", id);
                    super.startElement(FE_URI, "fragment", "fe:fragment", atts);
                    super.endElement(FE_URI, "fragment", "fe:fragment");
                    super.endPrefixMapping("fe");
                }
            }
        }
    }

    /**
     * Receive notification of character data.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char c[], int start, int len)
    throws SAXException {
        if (extractLevel == 0) {
            super.characters(c, start, len);
        } else {
            this.serializer.characters(c, start, len);
        }
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char c[], int start, int len)
    throws SAXException {
        if (extractLevel == 0) {
            super.ignorableWhitespace(c, start, len);
        } else {
            this.serializer.ignorableWhitespace(c, start, len);
        }
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (extractLevel == 0) {
            super.processingInstruction(target, data);
        } else {
            this.serializer.processingInstruction(target, data);
        }
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
        if (extractLevel == 0) {
            super.skippedEntity(name);
        } else {
            this.serializer.skippedEntity(name);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        if (extractLevel == 0) {
            super.startDTD(name, publicId, systemId);
        } else {
            throw new SAXException(
                "Recieved startDTD after beginning fragment extraction process."
            );
        }
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        if (extractLevel == 0) {
            super.endDTD();
        } else {
            throw new SAXException(
                "Recieved endDTD after beginning fragment extraction process."
            );
        }
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name)
    throws SAXException {
        if (extractLevel == 0) {
            super.startEntity(name);
        } else {
            this.serializer.startEntity(name);
        }
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name)
    throws SAXException {
        if (extractLevel == 0) {
            super.endEntity(name);
        } else {
            this.serializer.endEntity(name);
        }
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if (extractLevel == 0) {
            super.startCDATA();
        } else {
            this.serializer.startCDATA();
        }
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if (extractLevel == 0) {
            super.endCDATA();
        } else {
            this.serializer.endCDATA();
        }
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (extractLevel == 0) {
            super.comment(ch, start, len);
        } else {
            this.serializer.comment(ch, start, len);
        }
    }
}
