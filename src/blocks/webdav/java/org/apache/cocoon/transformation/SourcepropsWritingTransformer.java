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
import java.io.StringReader;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.xml.dom.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This transformer allows you to set properties on an InspectableSource.
 * 
* <p>Input XML document example:</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;source:patch xmlns:source="http://apache.org/cocoon/propwrite/1.0"&gt;
 *     &lt;source:source&gt;webdav://localhost/webdav/step1/repo/contentA.xml&lt;/source:source&gt;
 *     &lt;source:prop name="author" namespace="meta"&gt;me&lt;/source:prop&gt;
 *     &lt;source:prop name="category" namespace="meta"&gt;catA&lt;/source:prop&gt;
 *   &lt;/source:write&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 * 
 * @author <a href="mailto:gcasper@s-und-n.de">Guido Casper</a>
 * @version CVS $Id: SourcepropsWritingTransformer.java,v 1.1 2003/08/22 13:02:01 gcasper Exp $
 */
public class SourcepropsWritingTransformer
    extends AbstractSAXTransformer {

    public static final String SPWT_URI = "http://apache.org/cocoon/propwrite/1.0";

        /** incoming elements */
    public static final String PATCH_ELEMENT = "patch";
    public static final String SOURCE_ELEMENT = "source";
    public static final String PROP_ELEMENT = "prop";

    /** main tag attributes */
    public static final String NAME_ATTRIBUTE = "name";
    public static final String NAMESPACE_ATTRIBUTE = "namespace";

    /** The current state */
    private static final int STATE_OUTSIDE  = 0;
    private static final int STATE_PATCH    = 1;
    private static final int STATE_SOURCE   = 2;
    private static final int STATE_PROP     = 3;

    private int state;
    
    /**
     * Constructor
     * Set the namespace
     */
    public SourcepropsWritingTransformer() {
        this.namespaceURI = SPWT_URI;
    }

    public void recycle() {
        super.recycle();
        this.state = STATE_OUTSIDE;
    }

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
     *
     * @param resolver Source Resolver
     * @param objectModel Object model
     * @param src URI of the source attribute
     * @param parameters Parameters for the transformer
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.state = STATE_OUTSIDE;
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param name The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param attr The attributes attached to the element. If there are no
     *            attributes, it shall be an empty Attributes object.
     */
    public void startTransformingElement(String uri, String name, String raw, Attributes attr)
    throws SAXException, IOException, ProcessingException {
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN startTransformingElement uri=" + uri +
                              ", name=" + name + ", raw=" + raw + ", attr=" + attr);
        }
        // Element: patch
        if (this.state == STATE_OUTSIDE && name.equals(PATCH_ELEMENT)) {
            this.state = STATE_PATCH;
            this.stack.push("END");

        // Element: source
        } else if (this.state == STATE_PATCH && name.equals(SOURCE_ELEMENT)) {
            this.state = STATE_SOURCE;
            this.startTextRecording();

        // Element: prop
        } else if (this.state == STATE_PATCH && name.equals(PROP_ELEMENT)) {
            this.state = STATE_PROP;
            this.stack.push(attr.getValue(NAME_ATTRIBUTE));
            this.stack.push(attr.getValue(NAMESPACE_ATTRIBUTE));
            this.startTextRecording();

        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END startTransformingElement");
        }
    }


    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param name The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endTransformingElement(String uri, String name, String raw)
    throws SAXException, IOException, ProcessingException {
        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("BEGIN endTransformingElement uri=" + uri +
                              ", name=" + name +
                              ", raw=" + raw);
        }

        // Element: patch
        if ((name.equals(PATCH_ELEMENT) && this.state == STATE_PATCH)) {
            this.state = STATE_OUTSIDE;

            String sourceName = null;
            Vector props = new Vector();
            String tag;
            do {
                String[] prop = new String[3];
                tag = (String)this.stack.pop();
                if (tag.equals(SOURCE_ELEMENT)) {
                    sourceName = (String)this.stack.pop();
                } else if (tag.equals(PROP_ELEMENT)) {
                    prop[2] = (String)this.stack.pop();
                    prop[1] = (String)this.stack.pop();
                    prop[0] = (String)this.stack.pop();
                    props.addElement(prop);
                }
            } while ( !tag.equals("END") );
            
            String propName = null;
            String propNamespace = null;
            String propValue = null;
            String[] propsArray = new String[3];
            for (int i = 0; i<props.size(); i++) {
                propsArray = (String[]) props.elementAt(i);
                propName = propsArray[0]; 
                propNamespace = propsArray[1]; 
                propValue = propsArray[2]; 
                this.patchSource(sourceName, propName, propNamespace, propValue);
            }

        // Element: source
        } else if (name.equals(SOURCE_ELEMENT) && this.state == STATE_SOURCE) {
            this.state = STATE_PATCH;
            String sourceName = this.endTextRecording();
            this.stack.push(sourceName);
            this.stack.push(SOURCE_ELEMENT);

        // Element: prop
        } else if (name.equals(PROP_ELEMENT) == true && this.state == STATE_PROP) {
            this.state = STATE_PATCH;
            // FIXME TextRecorder only gets characters events
            String propValue = this.endTextRecording();
            this.stack.push(propValue);
            this.stack.push(PROP_ELEMENT);

        // default
        } else {
            super.endTransformingElement(uri, name, raw);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END endTransformingElement");
        }
    }

    private void patchSource(String src, String name, String namespace, String value)
    throws ProcessingException, IOException, SAXException {

        if (src != null && name != null && namespace != null && value != null) {

            DOMParser parser = null;
            final String quote = "\"";
            try {
                parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
                Source source = this.resolver.resolveURI(src);
                if (source instanceof InspectableSource) {
                    String pre = "<"+name+" xmlns="+quote+namespace+quote+">";
                    String post = "</"+name+">";
                    String xml = pre+value+post;
                    StringReader reader = new StringReader(xml);
                    Document doc = parser.parseDocument(new InputSource(reader));
                    SourceProperty property = new SourceProperty(doc.getDocumentElement());
                    ((InspectableSource)source).setSourceProperty(property);

                } else {
                    this.getLogger().error("Cannot set properties on " + src +
                                           ": not an inspectable source");
                }

            } catch (Exception e) {
                throw new ProcessingException("Error setting properties on "+src, e);
            } finally {
                this.manager.release((Component) parser);
            }
        } else {
            this.getLogger().error("Error setting properties on "+src);
        }
    }

}
