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
import java.io.OutputStream;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.apache.excalibur.source.ModifiableSource;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.dom.DOMParser;
import org.apache.excalibur.xml.xpath.XPathProcessor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer allows you to output to a ModifiableSource.
 *
 * <p>Definition:</p>
 * <pre>
 * &lt;map:transformer     name="tofile"     src="org.apache.cocoon.transformation.SourceWritingTransformer"&gt;
 *   &lt;!-- 'xml' is the default Serializer (if your Source needs one, like for instance FileSource) --&gt;
 *   &lt;map:parameter name="serializer" value="xml"/&gt;
 * &lt;/map:transformer/&gt;
 * </pre>
 *
 * <p>Invocation:</p>
 * <pre>
 * &lt;map:transform type="tofile"&gt;
 *   &lt;map:parameter name="serializer" value="xml"/&gt;   &lt;!-- you can optionally override the serializer here --&gt;
 * &lt;/map:transform&gt;
 * </pre>
 *
 * <p>The Tags:</p>
 * <pre>
 * &lt;source:write create="[true]|false"&gt; - replaces the entire content of an existing asset, if @create is 'true' (default), a new asset will be created if one does not already exist.
 *     &lt;source:source&gt;The System ID of the asset to be written to&lt;/source:source&gt; - eg: "docs/blah.xml" or "context://blah.xml" etc.
 *     &lt;source:path&gt;[Optional] XPath to specify how your content is wrapped&lt;/source:path&gt; - eg: "doc" (your content is placed inside a &lt;doc/&gt; root tag). NOTE: if this value is omitted, your content MUST have only ONE top-level node.
 *     &lt;source:fragment&gt;The XML Fragment to be written&lt;/source:fragment&gt; - eg: "&lt;foo&gt;&lt;bar id="dogcow"/&gt;&lt;/foo&gt;" or "&lt;foo/&gt;&lt;bar&gt;&lt;dogcow/&gt;&lt;bar/&gt;" etc. NOTE: the second example type, can only be used when the &lt;source:path/&gt; tag has been specified.
 * &lt;source:write&gt;
 *
 * &lt;source:insert create="[true]|false" overwrite="[true]|false"&gt; - inserts content into an existing asset, if @create is 'true' (default), a new asset will be created if one does not already exist. If @overwrite is set to 'true' the data is only inserted if the node specified by the 'replacePath' does not exists.
 *     &lt;source:source&gt;The System ID of the asset to be written to&lt;/source:source&gt; - eg: "docs/blah.xml" or "context://blah.xml" etc.
 *     &lt;source:path&gt;XPath specifying the node into which the content is inserted&lt;/source:path&gt; - eg: "doc" (your content is appended as the last child of the &lt;doc/&gt; root tag), or "doc/section[3]". NOTE: this tag is required in &lt;source:insert/&gt; unlike &lt;source:write/&gt; where it is optional.
 *     &lt;source:replace&gt;[Optional] XPath (relative to &lt;source:path/&gt;) to the node that is replaced by your new content&lt;/source:replace&gt; - eg: "foo/bar/dogcow/@status='cut'" (is equivalent to this in XSLT: select="foo[bar/dogcow/@status='cut']").
 *     &lt;source:reinsert&gt;[Optional] The XPath (relative to &lt;source:replace/&gt;) to backup the overwritten node to&lt;/source:reinsert&gt; - eg: "foo/versions" or "/doc/versions/foo". NOTE: If specified and a node is replaced, all children of this replaced node will be reinserted at the given path.
 *     &lt;source:fragment&gt;The XML Fragment to be written&lt;/source:fragment&gt; - eg: "&lt;foo&gt;&lt;bar id="dogcow"/&gt;&lt;/foo&gt;" or "&lt;foo/&gt;&lt;bar&gt;&lt;dogcow/&gt;&lt;bar/&gt;" etc.
 * &lt;source:insert&gt;
 * 
 * &lt;source:delete &gt; - deletes an existing asset.
 *     &lt;source:source&gt;The System ID of the asset to be deleted&lt;/source:source&gt; - eg: "docs/blah.xml" or "context://blah.xml" etc.
 *     &lt;source:path&gt;[Ignored] XPath to specify how your content is wrapped&lt;/source:path&gt;
 *     &lt;source:fragment&gt;[Ignored]The XML Fragment to be written&lt;/source:fragment&gt; 
 * &lt;source:delete&gt;
 * </pre>
 *
 *
 * <p>Input XML document example (write):</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;source:write xmlns:source="http://apache.org/cocoon/source/1.0"&gt;
 *     &lt;source:source&gt;context://doc/editable/my.xml&lt;/source:source&gt;
 *     &lt;source:fragment&gt;&lt;page&gt;
 *       &lt;title&gt;Hello World&lt;/title&gt;
 *       &lt;content&gt;
 *         &lt;p&gt;This is my first paragraph.&lt;/p&gt;
 *       &lt;/content&gt;
 *     &lt;/page&gt;&lt;/source:fragment&gt;
 *   &lt;/source:write&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Input XML document example (insert at end):</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;source:insert xmlns:source="http://apache.org/cocoon/source/1.0"&gt;
 *     &lt;source:source&gt;context://doc/editable/my.xml&lt;/source:source&gt;
 *     &lt;source:path&gt;page/content&lt;/source:path&gt;
 *     &lt;source:fragment&gt;
 *       &lt;p&gt;This paragraph gets &lt;emp&gt;inserted&lt;/emp&gt;.&lt;/p&gt;
 *       &lt;p&gt;With this one, at the end of the content.&lt;/p&gt;
 *     &lt;/source:fragment&gt;
 *   &lt;/source:insert&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Input XML document example (insert at beginning):</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;source:insert&gt;
 *     &lt;source:source&gt;context://doc/editable/my.xml&lt;/source:source&gt;
 *     &lt;source:path&gt;page&lt;/source:path&gt;
 *     &lt;source:replace&gt;content&lt;/source:replace&gt;
 *     &lt;source:reinsert&gt;content&lt;/source:reinsert&gt;
 *     &lt;source:fragment&gt;
 *       &lt;content&gt;
 *         &lt;p&gt;This new paragraph gets inserted &lt;emp&gt;before&lt;/emp&gt; the other ones.&lt;/p&gt;
 *       &lt;/content&gt;
 *     &lt;/source:fragment&gt;
 *    &lt;source:insert&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Input XML document example (replace):</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;source:insert xmlns:source="http://apache.org/cocoon/source/1.0"&gt;
 *     &lt;source:source&gt;context://doc/editable/my.xml"&lt;/source:source&gt;
 *     &lt;source:path&gt;page/content&lt;/source:path&gt;
 *     &lt;source:replace&gt;p[1]&lt;/source:replace&gt;
 *     &lt;source:fragment&gt;
 *       &lt;p&gt;This paragraph &lt;emp&gt;replaces&lt;/emp&gt; the first paragraph.&lt;/p&gt;
 *     &lt;/source:fragment&gt;
 *   &lt;/source:insert&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>Output XML document example:</p>
 * <pre>
 * &lt;page&gt;
 *   ...
 *   &lt;sourceResult xmlns:source="http://apache.org/cocoon/source/1.0"&gt;
 *     &lt;action&gt;new|overwritten|none&lt;/action&gt;
 *     &lt;behaviour&gt;write|insert&lt;behaviour&gt;
 *     &lt;execution&gt;success|failure&lt;/execution&gt;
 *     &lt;serializer&gt;xml&lt;/serializer&gt;
 *     &lt;source&gt;file:/source/specific/path/to/context/doc/editable/my.xml&lt;/source&gt;
 *   &lt;/sourceResult&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 *
 * The XPath specification is very complicated. So here is an example for the sitemap:
 * <pre>
 * &lt;page xmlns:source="http://apache.org/cocoon/source/1.0"&gt;
 *   ...
 * &lt;source:insert&gt;
 *   &lt;source:source&gt;sitemap.xmap&lt;/source:source&gt;
 *   &lt;source:path&gt;/*[namespace-uri()="http://apache.org/cocoon/sitemap/1.0" and local-name()="sitemap"]/*[namespace-uri()="http://apache.org/cocoon/sitemap/1.0" and local-name()="components"]/*[namespace-uri()="http://apache.org/cocoon/sitemap/1.0" and local-name()="generators"]&lt;/source:path&gt;
 *   &lt;source:fragment&gt;
 *	  	&lt;generator name="file" xmln="http://apache.org/cocoon/sitemap/1.0"&gt;
 *			&lt;test/&gt;
 *		&lt;/generator&gt;
 *   &lt;/source:fragment&gt;
 *   &lt;source:replace&gt;*[namespace-uri()="http://apache.org/cocoon/sitemap/1.0" and local-name()="generator" and attribute::name="file"]&lt;/source:replace&gt;
 * &lt;/source:insert&gt;
 *   ...
 * &lt;/page&gt;
 * </pre>
 *
 * <p>This insert replaces (if it exists) the file generator definition with a new one.
 * As the sitemap uses namespaces the XPath for the generator is rather complicated.
 * Due to this it is necessary that the node specified by path exists if namespaces
 * are used! Otherwise a node with the name * would be created...</p>
 *
 *  <p>The create attribute of insert. If this is set
 *  to true (default is true), the file is created if it does not exists.
 *  If it is set to false, it is not created, making insert a real insert.
 *  create is only usable for files!</p>
 *  <p>In addition the overwrite attribute is used to check if replacing is allowed.
 *  If overwrite is true (the default) the node is replaced. If it is false
 *  the node is not inserted if the replace node is available.</p>
 *
 *  <p>[JQ] - the way I understand this, looking at the code:
 *  <pre>
 *   if 'replace' is not specified, your 'fragment' is appended as a child of 'path'.
 *   if 'replace' is specified and it exists and 'overwrite' is true, your 'fragment' is inserted in 'path', before 'replace' and then 'replace' is deleted.
 *   if 'replace' is specified and it exists and 'overwrite' is false, no action occurs.
 *   if 'replace' is specified and it does not exist and 'overwrite' is true, your 'fragment' is appended as a child of 'path'.
 *   if 'replace' is specified and it does not exist and 'overwrite' is false, your 'fragment' is appended as a child of 'path'.
 *   if 'reinsert' is specified and it does not exist, no action occurs.
 *  </pre></p>
 *
 * The &lt;source:reinsert&gt; option can be used to
 * reinsert a replaced node at a given path in the new fragment.
 *
 * <b>
 * TODO: Use the serializer instead of the XMLUtils for inserting of fragments
 * TODO: Add a &lt;source:before/&gt; tag.
 * </b>
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:jeremy@apache.org">Jeremy Quinn</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @version CVS $Id: SourceWritingTransformer.java,v 1.11 2004/03/17 12:09:52 cziegeler Exp $
 */
public class SourceWritingTransformer
    extends AbstractSAXTransformer
    implements Disposable {

    public static final String SWT_URI = "http://apache.org/cocoon/source/1.0";
    public static final String DEFAULT_SERIALIZER = "xml";

    /** incoming elements */
    public static final String WRITE_ELEMENT = "write";
    public static final String INSERT_ELEMENT = "insert";
    public static final String PATH_ELEMENT = "path";
    public static final String FRAGMENT_ELEMENT = "fragment";
    public static final String REPLACE_ELEMENT = "replace";
    public static final String DELETE_ELEMENT = "delete";
    public static final String SOURCE_ELEMENT = "source";
    public static final String REINSERT_ELEMENT = "reinsert";
    /** outgoing elements */
    public static final String RESULT_ELEMENT = "sourceResult";
    public static final String EXECUTION_ELEMENT = "execution";
    public static final String BEHAVIOUR_ELEMENT = "behaviour";
    public static final String ACTION_ELEMENT = "action";
    public static final String MESSAGE_ELEMENT = "message";
    public static final String SERIALIZER_ELEMENT = "serializer";
    /** main (write or insert) tag attributes */
    public static final String SERIALIZER_ATTRIBUTE = "serializer";
    public static final String CREATE_ATTRIBUTE = "create";
    public static final String OVERWRITE_ATTRIBUTE = "overwrite";
    /** results */
    public static final String RESULT_FAILED = "failed";
    public static final String RESULT_SUCCESS = "success";
    public static final String ACTION_NONE = "none";
    public static final String ACTION_NEW = "new";
    public static final String ACTION_OVER = "overwritten";
    public static final String ACTION_DELETE = "deleted";
    /** The current state */
    private static final int STATE_OUTSIDE  = 0;
    private static final int STATE_INSERT   = 1;
    private static final int STATE_PATH     = 3;
    private static final int STATE_FRAGMENT = 4;
    private static final int STATE_REPLACE  = 5;
    private static final int STATE_FILE     = 6;
    private static final int STATE_REINSERT = 7;
    private static final int STATE_WRITE    = 8;
    private static final int STATE_DELETE = 9;
    private int state;
    private int parent_state;

    /** The configured serializer name */
    protected String configuredSerializerName;

    /** The XPath processor */
    protected XPathProcessor xpathProcessor;
    
    /**
     * Constructor.
     * Sets the namespace.
     */
    public SourceWritingTransformer() {
        this.namespaceURI = SWT_URI;
    }

    /**
     * Get the current <code>Configuration</code> instance used by this
     * <code>Configurable</code>.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        super.configure( configuration );
        this.configuredSerializerName = configuration.getChild(SERIALIZER_ATTRIBUTE).getValue(DEFAULT_SERIALIZER);
    }

    /**
     * Get the <code>Parameter</code> called "serializer" from the
     * <code>Transformer</code> invocation.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);
        this.configuredSerializerName = par.getParameter(SERIALIZER_ATTRIBUTE, this.configuredSerializerName);
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("BEGIN startTransformingElement uri=" + uri +
                              ", name=" + name + ", raw=" + raw + ", attr=" + attr);
        }
        // Element: insert
        if (this.state == STATE_OUTSIDE 
            && (name.equals(INSERT_ELEMENT) || name.equals(WRITE_ELEMENT))) {

            this.state = (name.equals(INSERT_ELEMENT) ? STATE_INSERT : STATE_WRITE);
            this.parent_state = this.state;
            if (attr.getValue(CREATE_ATTRIBUTE) != null
                && attr.getValue(CREATE_ATTRIBUTE).equals("false")) {
                this.stack.push("false");
            } else {
                this.stack.push("true"); // default value
            }
            if (attr.getValue(OVERWRITE_ATTRIBUTE) != null
                && attr.getValue(OVERWRITE_ATTRIBUTE).equals("false")) {
                this.stack.push("false");
            } else {
                this.stack.push("true"); // default value
            }
            this.stack.push(attr.getValue(SERIALIZER_ATTRIBUTE));
            this.stack.push("END");

        // Element: delete
        } else if (this.state == STATE_OUTSIDE && name.equals(DELETE_ELEMENT)) {
            this.state = STATE_DELETE;
            this.parent_state = state;
            this.stack.push("END");
        // Element: file
        } else if (name.equals(SOURCE_ELEMENT)
                   && (this.state == STATE_INSERT || this.state == STATE_WRITE || this.state == STATE_DELETE)) {
            this.state = STATE_FILE;
            this.startTextRecording();

        // Element: path
        } else if (name.equals(PATH_ELEMENT)
                   && (this.state == STATE_INSERT || this.state == STATE_WRITE || this.state == STATE_DELETE)) {
            this.state = STATE_PATH;
            this.startTextRecording();

        // Element: replace
        } else if (name.equals(REPLACE_ELEMENT)
                   && this.state == STATE_INSERT) {
            this.state = STATE_REPLACE;
            this.startTextRecording();

        // Element: fragment
        } else if (name.equals(FRAGMENT_ELEMENT)
                   &&  (this.state == STATE_INSERT || this.state == STATE_WRITE || this.state == STATE_DELETE)) {
            this.state = STATE_FRAGMENT;
            this.startRecording();

        // Element: reinsert
        } else if (name.equals(REINSERT_ELEMENT)
                   && this.state == STATE_INSERT) {
            this.state = STATE_REINSERT;
            this.startTextRecording();

        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("END startTransformingElement");
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("BEGIN endTransformingElement uri=" + uri +
                              ", name=" + name +
                              ", raw=" + raw);
        }
        if ((name.equals(INSERT_ELEMENT) && this.state == STATE_INSERT)
            || (name.equals(WRITE_ELEMENT) && this.state == STATE_WRITE)) {

            // get the information from the stack
            DocumentFragment fragment  = null;
            String tag;
            String sourceName    = null;
            String path        = (this.state == STATE_INSERT ? null : "/");
                                 // source:write's path can be empty
            String replacePath = null;
            String reinsert    = null;
            do {
                tag = (String)this.stack.pop();
                if (tag.equals("PATH")) {
                    path = (String)this.stack.pop();
                } else if (tag.equals("FILE")) {
                    sourceName = (String)this.stack.pop();
                } else if (tag.equals("FRAGMENT")) {
                    fragment = (DocumentFragment)this.stack.pop();
                } else if (tag.equals("REPLACE")) {
                    replacePath = (String)this.stack.pop();
                } else if (tag.equals("REINSERT")) {
                    reinsert = (String)this.stack.pop();
                }
            } while ( !tag.equals("END") );

            final String localSerializer = (String)this.stack.pop();
            final boolean overwrite = this.stack.pop().equals("true");
            final boolean create = this.stack.pop().equals("true");

            this.insertFragment(sourceName,
                                    path,
                                    fragment,
                                    replacePath,
                                    create,
                                    overwrite,
                                    reinsert,
                                    localSerializer,
                                    name);

            this.state = STATE_OUTSIDE;

        // Element: delete
        } else if (name.equals(DELETE_ELEMENT) && this.state == STATE_DELETE) {
            String sourceName = null;
            String tag;
            do {
                tag = (String)this.stack.pop();
                if (tag.equals("FILE")) {
                    sourceName = (String)this.stack.pop();
                } else if (tag.equals("FRAGMENT")) {
                    //Get rid of it
                    this.stack.pop();
                }
            } while ( !tag.equals("END"));
            
            this.deleteSource(sourceName);
            this.state = STATE_OUTSIDE;                       
        // Element: file
        } else if (name.equals(SOURCE_ELEMENT) && this.state == STATE_FILE) {
            this.state = this.parent_state;
            this.stack.push(this.endTextRecording());
            this.stack.push("FILE");

        // Element: path
        } else if (name.equals(PATH_ELEMENT) && this.state == STATE_PATH) {
            this.state = this.parent_state;
            this.stack.push(this.endTextRecording());
            this.stack.push("PATH");

        // Element: replace
        } else if (name.equals(REPLACE_ELEMENT) && this.state == STATE_REPLACE) {
            this.state = this.parent_state;
            this.stack.push(this.endTextRecording());
            this.stack.push("REPLACE");

        // Element: fragment
        } else if (name.equals(FRAGMENT_ELEMENT) && this.state == STATE_FRAGMENT) {
            this.state = this.parent_state;
            this.stack.push(this.endRecording());
            this.stack.push("FRAGMENT");

        // Element: reinsert
        } else if (name.equals(REINSERT_ELEMENT) && this.state == STATE_REINSERT) {
            this.state = this.parent_state;
            this.stack.push(this.endTextRecording());
            this.stack.push("REINSERT");

        // default
        } else {
            super.endTransformingElement(uri, name, raw);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("END endTransformingElement");
        }
    }

    /**
     * Deletes a source
     * @param systemID
     */
    private void deleteSource(String systemID) throws ProcessingException, IOException, SAXException {
        Source source = null;
        try {
            source = resolver.resolveURI(systemID);
            if (!(source instanceof ModifiableSource)) {
                throw new ProcessingException("Source '" + systemID + "' is not writeable.");
            }

            ((ModifiableSource)source).delete();
            reportResult("none",
                         "delete",
                         "source deleted successfully",
                         systemID,
                         RESULT_SUCCESS,
                         ACTION_DELETE);
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("FAIL exception: " + se, se);
            }
            reportResult("none",
                         "delete",
                         "unable to delete source: " + se.getMessage(),
                         systemID,
                         RESULT_FAILED,
                         ACTION_DELETE);
        } finally {
            resolver.release(source);
        }
    }

    /**
     * Insert a fragment into a file.
     * The file is loaded by the resource connector.
     *
     * @param systemID The name of the xml file.
     * @param path   The XPath specifying the node under which the data is inserted
     * @param fragment The data to be inserted.
     * @param replacePath Optional XPath relative to <CODE>path</CODE>. This path
     *                    can specify a node which will be removed if it exists.
     *                    So insertFragment can be used as a replace utility.
     * @param create      If the file does not exists and this is set to
     *                    <CODE>false</CODE> nothing is inserted. If it is set
     *                    to <CODE>true</CODE> the file is created and the data
     *                    is inserted.
     * @param overwrite   If this is set to <CODE>true</CODE> the data is only
     *                    inserted if the node specified by the <CODE>replacePath</CODE>
     *                    does not exists.
     * @param reinsertPath If specified and a node is replaced , all children of
     *                     this replaced node will be reinserted at the given path.
     * @param localSerializer  The serializer used to serialize the XML
     * @param tagname     The name of the tag that triggered me 'insert' or 'write'
     */
    protected void insertFragment(String systemID,
                                  String path,
                                  DocumentFragment fragment,
                                  String replacePath,
                                  boolean create,
                                  boolean overwrite,
                                  String  reinsertPath,
                                  String  localSerializer,
                                  String  tagname)
    throws SAXException, IOException, ProcessingException {
        // no sync req
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("BEGIN insertFragment systemID="+systemID+
                              ", path="+path+
                              ", replace="+replacePath+
                              ", create="+create+
                              ", overwrite="+overwrite+
                              ", reinsert="+reinsertPath+
                              ", fragment="+(fragment == null ? "null" : XMLUtils.serializeNode(fragment, XMLUtils.createPropertiesForXML(false))));
        }
        // test parameter
        if (systemID == null) {
            throw new ProcessingException("insertFragment: systemID is required.");
        }
        if (path == null) {
            throw new ProcessingException("insertFragment: path is required.");
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (fragment == null) {
            throw new ProcessingException("insertFragment: fragment is required.");
        }

        // first: read the source as a DOM
        Source source = null;
        Document resource = null;
        boolean failed = true;
        boolean exists = false;
        String message = "";
        String target = systemID;
        try {
            source = this.resolver.resolveURI( systemID );
            if ( ! (source instanceof ModifiableSource)) {
                throw new ProcessingException("Source '"+systemID+"' is not writeable.");
            }
            ModifiableSource ws = (ModifiableSource)source;
            exists = ws.exists();
            target = source.getURI();
            if ( exists && this.state == STATE_INSERT ) {
                                message = "content inserted at: " + path;
                resource = SourceUtil.toDOM( source );
                // import the fragment
                Node importNode = resource.importNode(fragment, true);
                // get the node
                Node parent = DOMUtil.selectSingleNode(resource, path, this.xpathProcessor);

                // replace?
                if (replacePath != null) {
                    try {
                        Node replaceNode = DOMUtil.getSingleNode(parent, replacePath, this.xpathProcessor);
                        // now get the parent of this node until it is the parent node for insertion
                        while (replaceNode != null && replaceNode.getParentNode().equals(parent) == false) {
                           replaceNode = replaceNode.getParentNode();
                        }
                        if (replaceNode != null) {
                            if (overwrite) {
                                if (parent.getNodeType() == Node.DOCUMENT_NODE) {
                                    // replacing of the document element is not allowed
                                    DOMParser parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
                                    try {
                                        resource = parser.createDocument();
                                    } finally {
                                        this.manager.release( parser );
                                    }

                                    resource.appendChild(resource.importNode(importNode, true));
                                    parent = resource;
                                    replaceNode = resource.importNode(replaceNode, true);
                                } else {
                                    parent.replaceChild(importNode, replaceNode);
                                }
                                message += ", replacing: " + replacePath;
                                if (reinsertPath != null) {
                                    Node insertAt = DOMUtil.getSingleNode(parent, reinsertPath, this.xpathProcessor);
                                    if (insertAt != null) {
                                        while (replaceNode.hasChildNodes()) {
                                            insertAt.appendChild(replaceNode.getFirstChild());
                                        }
                                    } else { // reinsert point null
                                        message = "replace failed, could not find your reinsert path: " + reinsertPath;
                                        resource = null;
                                    }
                                }
                            } else { // overwrite was false
                                message = "replace failed, no overwrite allowed.";
                                resource = null;
                            }
                        } else { // specified replaceNode was not found
                            parent.appendChild(importNode);
                        }
                    } catch (javax.xml.transform.TransformerException sax) {
                        throw new ProcessingException("TransformerException: " + sax, sax);
                    }
                } else { // no replace path, just do an insert at end
                    parent.appendChild(importNode);
                }
            } else if (create) {
                DOMParser parser = (DOMParser)this.manager.lookup(DOMParser.ROLE);
                try {
                    resource = parser.createDocument();
                } finally {
                    this.manager.release( parser );
                }
                // import the fragment
                Node importNode = resource.importNode(fragment, true);
                if ( path.equals("") ) {  // this is allowed in write
                    resource.appendChild(importNode.getFirstChild());
                    message = "entire source overwritten";

                } else {
                    // get the node
                    Node parent = DOMUtil.selectSingleNode(resource, path, this.xpathProcessor);
                    // add fragment
                    parent.appendChild(importNode);
                    message = "content appended to: " + path;
                }
            } else {
                message = "create not allowed";
                resource = null;/**/
            }

            // write source
            if ( resource != null) {
                resource.normalize();
                // use serializer
                if (localSerializer == null) localSerializer = this.configuredSerializerName;
                if (localSerializer != null) {
                    // Lookup the Serializer
                    ServiceSelector selector = null;
                    Serializer serializer = null;
                    OutputStream oStream = null;
                    try {
                        selector = (ServiceSelector)manager.lookup(Serializer.ROLE + "Selector");
                        serializer = (Serializer)selector.select(localSerializer);
                        oStream = ws.getOutputStream();
                        serializer.setOutputStream(oStream);
                        DOMStreamer streamer = new DOMStreamer(serializer);
                        streamer.stream(resource);
                    } finally {
                        if (oStream != null) {
                            oStream.flush();
                            try {
                                oStream.close();
                                failed = false;
                            } catch (Throwable t) {
                                if (getLogger().isDebugEnabled()) {
                                    getLogger().debug("FAIL (oStream.close) exception"+t, t);
                                }
                                throw new ProcessingException("Could not process your document.", t);
                            } finally {
                                if ( selector != null ) {
                                        selector.release( serializer );
                                        this.manager.release( selector );
                                }
                            }
                        }
                    }
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("ERROR no serializer");
                    }
                    //throw new ProcessingException("No serializer specified for writing to source " + systemID);
                    message = "That source requires a serializer, please add the appropirate tag to your code.";
                }
            }
        } catch (DOMException de) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("FAIL exception: "+de, de);
            }
            message = "There was a problem manipulating your document: " + de;
        } catch (ServiceException ce) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("FAIL exception: "+ce, ce);
            }
            message = "There was a problem looking up a component: " + ce;
        } catch (SourceException se) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("FAIL exception: "+se, se);
            }
            message = "There was a problem resolving that source: [" + systemID + "] : " + se;
        } finally {
            this.resolver.release( source );
        }

        // Report result
        String result = (failed) ? RESULT_FAILED : RESULT_SUCCESS;
        String action = ACTION_NONE;
        if (!failed) { action = (exists) ? ACTION_OVER : ACTION_NEW; }

        this.reportResult(localSerializer, tagname, message, target, result, action);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("END insertFragment");
        }
    }

    private void reportResult(String localSerializer, 
                                String tagname, 
                                String message, 
                                String target, 
                                String result, 
                                String action) throws SAXException {
        sendStartElementEvent(RESULT_ELEMENT);
            sendStartElementEvent(EXECUTION_ELEMENT);
                sendTextEvent(result);
            sendEndElementEvent(EXECUTION_ELEMENT);
            sendStartElementEvent(MESSAGE_ELEMENT);
                sendTextEvent(message);
            sendEndElementEvent(MESSAGE_ELEMENT);
            sendStartElementEvent(BEHAVIOUR_ELEMENT);
                sendTextEvent(tagname);
            sendEndElementEvent(BEHAVIOUR_ELEMENT);
            sendStartElementEvent(ACTION_ELEMENT);
                sendTextEvent(action);
            sendEndElementEvent(ACTION_ELEMENT);
            sendStartElementEvent(SOURCE_ELEMENT);
                sendTextEvent(target);
            sendEndElementEvent(SOURCE_ELEMENT);
            if (localSerializer != null) {
                sendStartElementEvent(SERIALIZER_ELEMENT);
                    sendTextEvent(localSerializer);
                sendEndElementEvent(SERIALIZER_ELEMENT);
            }
        sendEndElementEvent(RESULT_ELEMENT);
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.xpathProcessor = (XPathProcessor)this.manager.lookup(XPathProcessor.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.xpathProcessor);
            this.xpathProcessor = null;
        }
    }

}
