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

package org.apache.cocoon.serialization;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A serializer that builds Zip archives by aggregating several sources.
 * <p>
 * The input document should describe entries of the archive by means of
 * their name (which can be a path) and their content either as URLs or
 * inline data :
 * <ul>
 * <li>URLs, given by the "src" attribute, are Cocoon sources and as such
 *     can use any of the protocols handled by Cocoon, including "cocoon:" to
 *     include dynamically generated content in the archive.</li>
 * <li>inline data is represented by an XML document that is serialized to the
 *     zip entry using the serializer identified by the "serializer" attribute.</li>
 * </ul>
 * <p>
 * Example :
 * <pre>
 *   &lt;zip:archive xmlns:zip="http://apache.org/cocoon/zip-archive/1.0"&gt;
 *     &lt;zip:entry name="foo.html" src="cocoon://dynFoo.html"/&gt;
 *     &lt;zip:entry name="images/bar.jpeg" src="bar.jpeg"/&gt;
 *     &lt;zip:entry name="index.html" serializer="html"&gt;
 *       &lt;html&gt;
 *         &lt;head&gt;
 *           &lt;title&gt;Index page&lt;/title&gt;
 *         &lt;/head&gt;
 *         &lt;body&gt;
 *           Please go &lt;a href="foo.html"&gt;there&lt;/a&gt;
 *         &lt;/body&lt;
 *       &lt;/html&gt;
 *     &lt;/zip:entry&gt;
 *   &lt;/zip:archive:zip&gt;
 * </pre>
 * 
 * @author <a href="http://www.apache.org/~sylvain">Sylvain Wallez</a>
 * @version CVS $Id: ZipArchiveSerializer.java,v 1.2 2003/05/08 05:05:22 vgritsenko Exp $
 */

// TODO (1) : handle more attributes on <archive> for properties of ZipOutputStream
//            such as comment or default compression method and level

// TODO (2) : handle more attributes on <entry> for properties of ZipEntry 
//            (compression method and level, time, comment, etc.)

public class ZipArchiveSerializer extends AbstractSerializer implements Composable {
    /**
     * The namespace for elements handled by this serializer, 
     * "http://apache.org/cocoon/zip-archive/1.0".
     */
    public static final String ZIP_NAMESPACE = "http://apache.org/cocoon/zip-archive/1.0";

    private static final int START_STATE = 0;
    private static final int IN_ZIP_STATE = 1;
    private static final int IN_CONTENT_STATE = 2;

    /** The component manager */
    protected ComponentManager manager;

    /** The serializer component selector */
    protected ComponentSelector selector;

    /** The Zip stream where entries will be written */
    protected ZipOutputStream zipOutput;

    /** The current state */
    protected int state = START_STATE;

    /** The resolver to get sources */
    protected SourceResolver resolver;

    /** Temporary byte buffer to read source data */
    protected byte[] buffer = new byte[1024];

    /** Serializer used when in IN_CONTENT state */
    protected Serializer serializer;

    /** Current depth of the serialized content */
    protected int contentDepth;

    /** Used to collect namespaces */
    private NamespaceSupport nsSupport = new NamespaceSupport();

    /**
     * Store exception
     */
    private SAXException exception = null;


    /**
     * @see org.apache.avalon.framework.component.Composable#compose(ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /**
     * Always return "application/x-zip" which is the default for Zip archives.
     */
    public String getMimeType() {
        return "application/x-zip";
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        this.state = START_STATE;
        this.zipOutput = new ZipOutputStream(this.output);
        this.resolver = CocoonComponentManager.getCurrentEnvironment();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (state == IN_CONTENT_STATE) {
            // Pass to the serializer
            super.startPrefixMapping(prefix, uri);

        } else {
            // Register it if it's not our own namespace (useless to content)
            if (!uri.equals(ZIP_NAMESPACE)) {
                this.nsSupport.declarePrefix(prefix, uri);
            }
        }
    }

    // Note : no need to implement endPrefixMapping() as we just need to pass it through if there
    // is a serializer, which is what the superclass does.

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
        throws SAXException {

        // Damage control. Sometimes one exception is just not enough...
        if (this.exception != null) {
            throw this.exception;
        }

        switch (state) {
            case START_STATE:
                // expecting "zip" as the first element
                if (namespaceURI.equals(ZIP_NAMESPACE) && localName.equals("archive")) {
                    this.nsSupport.pushContext();
                    this.state = IN_ZIP_STATE;
                } else {
                    throw this.exception =
                        new SAXException("Expecting 'archive' root element (got '" + localName + "')");
                }
                break;

            case IN_ZIP_STATE:
                // expecting "entry" element
                if (namespaceURI.equals(ZIP_NAMESPACE) && localName.equals("entry")) {
                    this.nsSupport.pushContext();
                    // Get the source
                    addEntry(atts);
                } else {
                    throw this.exception =
                        new SAXException("Expecting 'entry' element (got '" + localName + "')");
                }
                break;

            case IN_CONTENT_STATE:
                this.contentDepth++;
                super.startElement(namespaceURI, localName, qName, atts);
                break;
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] buffer, int offset, int length) throws SAXException {
        // Propagate text to the serializer only if we have encountered the content's top-level
        // element. Otherwhise, the serializer may be confused by some characters occuring between
        // startDocument() and the first startElement() (e.g. Batik fails hard in that case)
        if (this.state == IN_CONTENT_STATE && this.contentDepth > 0) {
            super.characters(buffer, offset, length);
        }
    }

    /**
     * Add an entry in the archive.
     * @param atts the attributes that describe the entry
     */
    protected void addEntry(Attributes atts) throws SAXException {
        String name = atts.getValue("name");
        if (name == null) {
            throw this.exception =
                new SAXException("No name given to the Zip entry");
        }

        String src = atts.getValue("src");
        String serializerType = atts.getValue("serializer");

        if (src == null && serializerType == null) {
            throw this.exception =
                new SAXException("No source nor serializer given for the Zip entry '" + name + "'");
        }

        if (src != null && serializerType != null) {
            throw this.exception =
                new SAXException("Cannot specify both 'src' and 'serializer' on a Zip entry '" + name + "'");
        }

        Source source = null;
        try {
            // Create a new Zip entry
            ZipEntry entry = new ZipEntry(name);
            this.zipOutput.putNextEntry(entry);

            if (src != null) {
                // Get the source and its data
                source = resolver.resolveURI(src);
                InputStream sourceInput = source.getInputStream();

                // Copy the source to the zip
                int len;
                while ((len = sourceInput.read(this.buffer)) > 0) {
                    this.zipOutput.write(this.buffer, 0, len);
                }

                // and close the entry
                this.zipOutput.closeEntry();

            } else {
                // Serialize content
                if (this.selector == null) {
                    this.selector =
                        (ComponentSelector) this.manager.lookup(Serializer.ROLE + "Selector");
                }

                // Get the serializer
                this.serializer = (Serializer) this.selector.select(serializerType);

                // Direct its output to the zip file, filtering calls to close()
                // (we don't want the archive to be closed by the serializer)
                this.serializer.setOutputStream(new FilterOutputStream(this.zipOutput) {
                    public void close() { /*nothing*/
                    }
                });

                // Set it as the current XMLConsumer
                setConsumer(serializer);

                // start its document
                this.serializer.startDocument();

                // and give it any namespaces already declared
                Enumeration prefixes = this.nsSupport.getPrefixes();
                while (prefixes.hasMoreElements()) {
                    String prefix = (String) prefixes.nextElement();
                    super.startPrefixMapping(prefix, this.nsSupport.getURI(prefix));
                }

                this.state = IN_CONTENT_STATE;
                this.contentDepth = 0;
            }

        } catch (RuntimeException re) {
            throw re;
        } catch (SAXException se) {
            throw this.exception = se;
        } catch (Exception e) {
            throw this.exception = new SAXException(e);
        } finally {
            this.resolver.release( source );
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {

        // Damage control. Sometimes one exception is just not enough...
        if (this.exception != null) {
            throw this.exception;
        }

        if (state == IN_CONTENT_STATE) {
            super.endElement(namespaceURI, localName, qName);
            this.contentDepth--;

            if (this.contentDepth == 0) {
                // End of this entry

                // close all declared namespaces.
                Enumeration prefixes = this.nsSupport.getPrefixes();
                while (prefixes.hasMoreElements()) {
                    String prefix = (String) prefixes.nextElement();
                    super.endPrefixMapping(prefix);
                }

                super.endDocument();

                try {
                    this.zipOutput.closeEntry();
                } catch (IOException ioe) {
                    throw this.exception = new SAXException(ioe);
                }

                super.setConsumer(null);
                this.selector.release(this.serializer);
                this.serializer = null;

                // Go back to listening for entries
                this.state = IN_ZIP_STATE;
            }
        } else {
            this.nsSupport.popContext();
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        try {
            // Close the zip archive
            this.zipOutput.finish();

        } catch (IOException ioe) {
            throw new SAXException(ioe);
        }
    }

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.exception = null;
        if (this.serializer != null) {
            this.selector.release(this.serializer);
        }
        if (this.selector != null) {
            this.manager.release(this.selector);
        }
        super.recycle();
    }
}
