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
package org.apache.cocoon.i18n;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.xml.ParamSaxBuffer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceNotFoundException;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Implementation of <code>Bundle</code> interface for XML resources. Represents a
 * single XML message bundle.
 * 
 * XML format for this resource bundle implementation is the following:
 * <pre>
 * &lt;catalogue xml:lang="en"&gt;
 *   &lt;message key="key1"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   &lt;message key="key2"&gt;Message &lt;br/&gt; Value 1&lt;/message&gt;
 *   ...
 * &lt;/catalogue&gt;
 * </pre>
 * 
 * Value can be any well formed XML snippet and it will be cached by the key specified
 * in the attrbute <code>key</code>. Objects returned by this {@link Bundle} implementation
 * are instances of the {@link ParamSaxBuffer} class.
 * 
 * @author <a href="mailto:dev@cocoon.apache.org">Apache Cocoon Team</a>
 * @version CVS $Id: XMLResourceBundle.java,v 1.4 2003/12/10 15:37:37 vgritsenko Exp $
 */
public class XMLResourceBundle extends AbstractLogEnabled
                               implements Bundle, Serviceable {

    /**
     * Namespace for the Bundle markup
     */
    public static final String NS = "http://apache.org/cocoon/i18n/2.0";
    
    /**
     * XML bundle root element name
     */
    public static final String EL_CATALOGUE = "catalogue";
    
    /**
     * XML bundle message element name
     */
    public static final String EL_MESSAGE = "message";
    
    /**
     * XML bundle message element's key attribute name 
     */
    public static final String AT_KEY = "key";
    
    
    /**
     * Bundle name
     */
    private String name;

    /**
     * Bundle validity
     */
    private SourceValidity validity;

    /**
     * Locale of the bundle
     */
    private Locale locale;

    /**
     * Parent of the current bundle
     */
    protected Bundle parent;

    /**
     * Objects stored in the bundle
     */
    protected HashMap values;
    
    /**
     * Service Manager
     */
    protected ServiceManager manager;

    /**
     * Processes XML bundle file and creates map of values
     */
    private class SAXContentHandler implements ContentHandler {
        private Map values;
        private int state;
        private String namespace;
        private ParamSaxBuffer buffer; 
        
        public SAXContentHandler(Map values) {
            this.values = values;
        }
        
        public void setDocumentLocator(Locator arg0) {
            // Ignore
        }

        public void startDocument() throws SAXException {
            // Ignore
        }

        public void endDocument() throws SAXException {
            // Ignore
        }

        public void processingInstruction(String arg0, String arg1) throws SAXException {
            // Ignore
        }

        public void skippedEntity(String arg0) throws SAXException {
            // Ignore
        }

        public void startElement(String ns, String localName, String qName, Attributes atts) throws SAXException {
            switch (state) {
                case 0:
                    // <i18n:catalogue>
                    if (!"".equals(ns) && !NS.equals(ns)) {
                        throw new SAXException("Root element <" + EL_CATALOGUE +
                                               "> must be non-namespaced or in i18n namespace.");
                    }
                    if (!EL_CATALOGUE.equals(localName)) {
                        throw new SAXException("Root element must be <" + EL_CATALOGUE + ">.");
                    }
                    this.namespace = ns;
                    state ++;
                    break;
                case 1:
                    // <i18n:message>
                    if (!EL_MESSAGE.equals(localName)) {
                        throw new SAXException("<" + EL_CATALOGUE + "> must contain <" +
                                               EL_MESSAGE + "> elements only.");
                    }
                    if (!this.namespace.equals(ns)) {
                        throw new SAXException("<" + EL_MESSAGE + "> element must be in '" +
                                               this.namespace + "' namespace.");
                    }
                    String key =  atts.getValue(AT_KEY);
                    if (key == null) {
                        throw new SAXException("<" + EL_MESSAGE + "> must have '" +
                                               AT_KEY + "' attribute.");
                    }
                    buffer = new ParamSaxBuffer();
                    values.put(key, buffer);
                    state ++;
                    break;
                case 2:
                    buffer.startElement(ns, localName, qName, atts);
                    break;
                default:
                    throw new SAXException("Internal error: Invalid state");
            }
        }

        public void endElement(String ns, String localName, String qName) throws SAXException {
            switch (state) {
                case 0:
                    break;
                case 1:
                    // </i18n:catalogue>
                    state --;
                    break;
                case 2:
                    if (this.namespace.equals(ns) && EL_MESSAGE.equals(localName)) {
                        // </i18n:message>
                        this.buffer = null;
                        state --;
                    } else {
                        buffer.endElement(ns, localName, qName);
                    }
                    break;
                default:
                    throw new SAXException("Internal error: Invalid state");
            }
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (buffer != null) {
                buffer.startPrefixMapping(prefix, uri);
            }
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            if (buffer != null) {
                buffer.endPrefixMapping(prefix);
            }
        }

        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            if (buffer != null) {
                buffer.ignorableWhitespace(ch, start, length);
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (buffer != null) {
                buffer.characters(ch, start, length);
            }
        }
    }
    
    /**
     * Compose this instance
     *
     * @param manager The <code>ServiceManager</code> instance
     * @throws ComponentException if XPath processor is not found
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Implements Disposable interface for this class.
     */
    public void dispose() {
        this.manager = null;
    }

    /**
     * Initalize the bundle
     *
     * @param name name of the bundle
     * @param sourceURL source URL of the XML bundle
     * @param locale locale
     * @param parent parent bundle of this bundle
     *
     * @throws IOException if an IO error occurs while reading the file
     * @throws ProcessingException if an error occurs while loading the bundle
     * @throws SAXException if an error occurs while loading the bundle
     */
    public void init(String name, String sourceURL, Locale locale, Bundle parent)
    throws IOException, ProcessingException, SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Loading XML bundle: " + name + ", locale: " + locale);
        }

        this.name = name;
        this.locale = locale;
        this.parent = parent;
        this.values = new HashMap();
        load(sourceURL);
    }

    /**
     * Load the XML bundle, based on the source URL.
     *
     * @param sourceURL source URL of the XML bundle
     * @return the DOM tree
     *
     * @exception IOException if an IO error occurs while reading the file
     * @exception ParserConfigurationException if no parser is configured
     * @exception SAXException if an error occurs while parsing the file
     */
    protected void load(String sourceURL)
    throws IOException, ProcessingException, SAXException {

        Source source = null;
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(sourceURL);
            HashMap values = new HashMap();
            SourceUtil.toSAX(source, new SAXContentHandler(values));
            this.validity = source.getValidity();
            this.values = values;
        } catch (ServiceException e) {
            throw new ProcessingException("Can't lookup source resolver", e);
        } catch (MalformedURLException e) {
            throw new SourceNotFoundException("Invalid resource URL: " + sourceURL, e);
        } finally {
            if (source != null) {
                resolver.release(source);
            }
            manager.release(resolver);
        }
    }

    /**
     * Gets the name of the bundle.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the validity of the bundle.
     *
     * @return the validity
     */
    public SourceValidity getValidity() {
        return this.validity;
    }

    /**
     * Gets the locale of the bundle.
     *
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Get Object value by key.
     *
     * @param key the key
     * @return the value
     */
    public Object getObject(String key) {
        if (key == null) {
            return null;
        }

        Object value = values.get(key);
        if (value == null && this.parent != null) {
            value = this.parent.getObject(key);
        }

        return value;
    }

    /**
     * Get String value by key.
     *
     * @param key the key
     * @return the value
     */
    public String getString(String key) {
        if (key == null) {
            return null;
        }

        Object value = values.get(key);
        if (value != null) {
            return value.toString();
        }
        
        if(this.parent != null) {
            return this.parent.getString(key);
        }

        return null;
    }

    /**
     * Return a set of keys.
     *
     * @return the enumeration of keys
     */
    public Set keySet() {
        return Collections.unmodifiableSet(values.keySet());
    }
}
