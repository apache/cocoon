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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.InspectableSource;
import org.apache.cocoon.components.source.helpers.SourceProperty;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.w3c.dom.Element;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer allows you to set and remove properties on an InspectableSource.
 * 
* <p>Input XML document example:</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;source:patch xmlns:source="http://apache.org/cocoon/propwrite/1.0"&gt;
 *     &lt;source:source&gt;webdav://localhost/webdav/step1/repo/contentA.xml&lt;/source:source&gt;
 *     &lt;source:set&gt;
 *       &lt;myns:author xmlns:myns="meta"&gt;me&lt;/myns:author&gt;
 *     &lt;/source:set&gt;
 *     &lt;source:remove&gt;
 *       &lt;myns:title xmlns:myns="meta"/&gt;
 *     &lt;/source:remove&gt;
 *   &lt;/source:patch&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 * 
 * @author <a href="mailto:gcasper@s-und-n.de">Guido Casper</a>
 * @version CVS $Id: SourcePropsWritingTransformer.java,v 1.5 2004/03/05 13:02:22 bdelacretaz Exp $
 */
public class SourcePropsWritingTransformer
    extends AbstractSAXTransformer {

    public static final String SPWT_URI = "http://apache.org/cocoon/propwrite/1.0";

    /** incoming elements */
    public static final String PATCH_ELEMENT = "patch";
    public static final String SOURCE_ELEMENT = "source";
    public static final String SET_ELEMENT = "set";
    public static final String REMOVE_ELEMENT = "remove";

    /** The current state */
    private static final int STATE_OUTSIDE   = 0;
    private static final int STATE_PATCH     = 1;
    private static final int STATE_SOURCE    = 2;
    private static final int STATE_SET       = 3;
    private static final int STATE_REMOVE    = 4;

    private int state;
    
    /**
     * Constructor
     * Set the namespace
     */
    public SourcePropsWritingTransformer() {
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
     * @param par Parameters for the transformer
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
        if (this.getLogger().isDebugEnabled()) {
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

        // Element: props
        } else if (this.state == STATE_PATCH && name.equals(SET_ELEMENT)) {
            this.state = STATE_SET;
            this.startRecording();
        } else if (this.state == STATE_PATCH && name.equals(REMOVE_ELEMENT)) {
            this.state = STATE_REMOVE;
            this.startRecording();
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
        if ((this.state == STATE_PATCH && name.equals(PATCH_ELEMENT))) {
            this.state = STATE_OUTSIDE;
            String sourceName = null;
            String tag = null;
            DocumentFragment setfrag = null;
            DocumentFragment removefrag = null;
            do {
                tag = (String)this.stack.pop();
                if (tag.equals(SOURCE_ELEMENT)) {
                    sourceName = (String)this.stack.pop();
                } else if (tag.equals(SET_ELEMENT)) {
                    setfrag = (DocumentFragment)this.stack.pop();
                } else if (tag.equals(REMOVE_ELEMENT)) {
                    removefrag = (DocumentFragment)this.stack.pop();
                }
            } while ( !tag.equals("END") );
            if (setfrag != null) {
                NodeList list = setfrag.getChildNodes();
                Node node = null;
                for (int i=0; i<list.getLength(); i++) {
                    node = list.item(i);
                    if (node instanceof Element) {
                        this.setProperty(sourceName, (Element) node);
                    }
                }
            }
            if (removefrag != null) {
                NodeList list = removefrag.getChildNodes();
                Node node = null;
                for (int i=0; i<list.getLength(); i++) {
                    node = list.item(i);
                    if (node instanceof Element) {
                        this.removeProperty(sourceName, (Element) node);
                    }
                }
            }

        // Element: source
        } else if (this.state == STATE_SOURCE && name.equals(SOURCE_ELEMENT)) {
            this.state = STATE_PATCH;
            String sourceName = this.endTextRecording();
            this.stack.push(sourceName);
            this.stack.push(SOURCE_ELEMENT);

        // Element: set
        } else if (this.state == STATE_SET && name.equals(SET_ELEMENT)) {
            this.state = STATE_PATCH;
            this.stack.push(this.endRecording());
            this.stack.push(SET_ELEMENT);

        // Element: remove
        } else if (this.state == STATE_REMOVE && name.equals(REMOVE_ELEMENT)) {
            this.state = STATE_PATCH;
            this.stack.push(this.endRecording());
            this.stack.push(REMOVE_ELEMENT);
            
        // default
        } else {
            super.endTransformingElement(uri, name, raw);
        }

        if (this.getLogger().isDebugEnabled() == true) {
            this.getLogger().debug("END endTransformingElement");
        }
    }

    private void setProperty(String src, Element element) 
    throws ProcessingException, IOException, SAXException {
        if (src != null && element != null) {
            try {
                Source source = this.resolver.resolveURI(src);
                if (source instanceof InspectableSource) {
                    SourceProperty property = new SourceProperty(element);
                    ((InspectableSource)source).setSourceProperty(property);

                } else {
                    this.getLogger().error("Cannot set properties on " + src +
                                           ": not an inspectable source");
                }
            } catch (Exception e) {
                throw new ProcessingException("Error setting properties on "+src, e);
            }
        } else {
            this.getLogger().error("Error setting properties on "+src);
        }
    }
    
    private void removeProperty(String src, Element element) 
    throws ProcessingException, IOException, SAXException { 
        
        if (src != null && element != null) {
            try {
                Source source = this.resolver.resolveURI(src);
                if (source instanceof InspectableSource) {
                    ((InspectableSource)source).removeSourceProperty(
                        element.getNamespaceURI(),element.getLocalName());

                } else {
                    this.getLogger().error("Cannot remove properties on " + src +
                                           ": not an inspectable source");
                }
            } catch (Exception e) {
                throw new ProcessingException("Error removing properties on "+src, e);
            }
        } else {
            this.getLogger().error("Error removing properties on "+src);
        }
    }
}
