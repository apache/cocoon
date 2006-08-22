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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;

/**
 * Cleanup transformer: Removes excess whitespace while adding some where needed
 *  for legibility. Strips unwanted namespace declarations.
 *
 * <p>The cleanup transformer can be used for basically any document as-is or customized by
 *  schema (inline vs. block elements) for easier reading.</p>
 *
 * <p>Transformer declaration:
 *  &lt;map:components&gt;
 *   &lt;map:transformers&gt;
 *    &lt;map:transformer name="htmlcleanup"
 *            src="org.apache.cocoon.transformation.CleanupTransformer"&gt;
 *     &lt;preserve-uri&gt;*&lt;/preserve-uri&gt;
 *    &lt;/map:transformer&gt;
 *
 *    &lt;map:transformer name="xhtmlcleanup"
 *           src="org.apache.cocoon.transformation.CleanupTransformer"&gt;
 *     &lt;inline-elements&gt;a,abbr,acronym,b,br,font,i,u,img&lt;/inline-elements&gt;
 *     &lt;preserve-uri&gt;http://www.w3.org/1999/xhtml&lt;/preserve-uri&gt;
 *    &lt;/map:transformer&gt;
 *   &lt;/map:transformers&gt;
 *  &lt;/map:components&gt;
 * </p>
 *
 * <p>The "inline-elements" configuration element refers to a list of element names that are
 *  <strong>not</strong> to be indented.  The "preserve-uri" configuration element specifies a
 *  namespace uri mapping that is meant for output.  All other namespace declarations are
 *  stripped from the output.  The "preserve-uri" element may appear more than once.  If
 *  "preserve-uri" is omitted, all namespaces/prefixes are removed from the output.</p>
 *
 * <p>Transformer usage:
 *  &lt;transform type="xhtmlcleanup"&gt;
 *   &lt;map:parameter name="indent-size" value="4"/&gt;
 *  &lt;/transform&gt;
 * </p>
 *
 * <p>The optional parameter "indent-size" specifies the number of additional space characters
 *  appearing at each level of the output document.  The default value is 2.</p>
 *
 * <p>Bugs: Nested namespace declarations with the same namespace prefix will break the code.</p>
 *
 * @author Miles Elam
 */
public class CleanupTransformer
extends AbstractSAXTransformer
implements CacheableProcessingComponent {

    private static final char[] INDENT = ("\n" +
        "                                                                                " +
        "                                                                                "
        ).toCharArray();
    private static final int MAX_INDENT = CleanupTransformer.INDENT.length - 1;

    private boolean allowAllURIs = false;
    private Set allowedURIs = new HashSet();
    private Set inlineElements = new HashSet();
    private LinkedList uriPrefixes = new LinkedList();
    private int indentSize = 2;
    private int numIndents = 0;
    private String lastElement;

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        StringTokenizer st;

        Configuration inlineEltChild = conf.getChild("inline-elements");
        st = new StringTokenizer(inlineEltChild.getValue(""), ",");
        this.inlineElements.clear();
        while (st.hasMoreTokens()) {
            String nextElement = st.nextToken().trim();
            if (nextElement.length() > 0) {
                this.inlineElements.add(nextElement);
            }
        }

        this.allowAllURIs = false;
        Configuration[] uriChildren = conf.getChildren("preserve-uri");
        for (int i=0; i<uriChildren.length; ++i) {
            String nextChild = uriChildren[i].getValue("").trim();
            if (nextChild.length() == 0) {
                continue;
            } else if (nextChild.equals("*")) {
                this.allowAllURIs = true;
                break;
            }
            this.allowedURIs.add(nextChild);
        }
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup (SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.indentSize = par.getParameterAsInteger("indent-size", 2);
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#recycle()
     */
    public void recycle () {
        super.recycle();
        this.numIndents = 0;
        this.lastElement = null;
        this.uriPrefixes.clear();
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
     */
    public Serializable getKey () {
        return Integer.toString(this.indentSize);
    }

    /**
     * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
     */
    public SourceValidity getValidity () {
        return NOPValidity.SHARED_INSTANCE;
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping (String prefix, String uri)
    throws SAXException {
        if (this.allowAllURIs) {
            this.contentHandler.startPrefixMapping(prefix, uri);
        } else if (this.allowedURIs.contains(uri)) {
            this.contentHandler.startPrefixMapping(prefix, uri);
            uriPrefixes.add(prefix);
        }
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping (String prefix)
    throws SAXException {
        if (this.allowAllURIs) {
            this.contentHandler.endPrefixMapping(prefix);
        } else if (!uriPrefixes.isEmpty()) {
            if (uriPrefixes.getLast().toString().equals(prefix)) {
                this.contentHandler.endPrefixMapping(prefix);
                uriPrefixes.removeLast();
            }
        }
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement (String uri, String qName, String lName, Attributes attrs)
    throws SAXException {
        if (!inlineElements.contains(qName)) {
            int indentSize = (this.indentSize * this.numIndents) % MAX_INDENT;
            this.contentHandler.ignorableWhitespace(INDENT, 0, indentSize + 1);
            ++this.numIndents;
            this.lastElement = qName;
        }
        this.contentHandler.startElement(uri, qName, lName, attrs);
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement (String uri, String qName, String lName)
    throws SAXException {
        if (!inlineElements.contains(qName)) {
            --this.numIndents;
            if (this.lastElement == null || !this.lastElement.equals(qName)) {
                int indentSize = (this.indentSize * this.numIndents) % MAX_INDENT;
                this.contentHandler.ignorableWhitespace(INDENT, 0, indentSize + 1);
            }
            this.lastElement = null;
        }
        this.contentHandler.endElement(uri, qName, lName);
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#characters(char[], int, int)
     */
    public void characters (char[] ch, int start, int length)
    throws SAXException {
        int end = start + length;
        for (int i=start; i<end; ++i) {
            if (!Character.isWhitespace(ch[i])) {
                this.contentHandler.characters(ch, start, length);
                return;
            }
        }
        this.contentHandler.characters(INDENT, 1, 1);
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace (char[] ch, int start, int length)
    throws SAXException {
        // Do nothing
    }
}
