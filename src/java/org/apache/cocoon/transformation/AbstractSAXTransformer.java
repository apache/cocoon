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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.helpers.ParametersRecorder;
import org.apache.cocoon.transformation.helpers.TextRecorder;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.SourceParameters;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.*;

/**
 *  This class is the basis for all transformers. It provides various useful
 *  methods and hooks for implementing own custom transformers.<p>
 *  <p>
 *  The basic behaviour of each transformer consists of the following four parts:
 *  <ul>
 *  <li>Listen for specific events with a given namespace</li>
 *  <li>Collect information via these events</li>
 *  <li>Process the information</li>
 *  <li>Create new events from the processed information</li>
 *  </ul><p>
 *  For all these four purposes the AbstractSAXTransformer offers some
 *  powerful methods and hooks:
 *  <p>
 *  Namespace handling<p>
 *  By setting the instance variable namespaceURI to the namespace the
 *  events are filtered and only events with this namespace are send to
 *  the two hooks startTransformingElement() and endTransformingElement().<p>
 *  It is possible to override the default
 *  namespace for the transformer by specifying the parameter "namespaceURI"
 *  in the pipeline. This avoids possible namespace collisions.<p>
 *
 *  Recording of information<p>
 *  There are several methods for recording information, e.g. startRecording(),
 *  startTextRecording() etc. These methods collect information from the xml
 *  stream for further processing.<p>
 *
 *  Creating new events<p>
 *  New events can be easily created with the <code>sendEvents()</code>
 *  method, the <code>sendStartElementEvent()</code> methods, the <code>sendEndElementEvent()</code>
 *  method or the <code>sendTextEvent()</code> method.<p>
 *
 *  Initialization<p>
 *  Before the document is processed the setupTransforming() hook is invoked.
 *
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractSAXTransformer.java,v 1.4 2003/06/19 11:19:25 jefft Exp $
*/
public abstract class AbstractSAXTransformer
extends AbstractTransformer
implements Composable, Configurable, Recyclable {

    /** Controlls SAX event handling.
     *  If set to true all whitespace events are ignored.
     */
    protected boolean ignoreWhitespaces;
    /** Controlls SAX event handling
     *  If set to true all characters events containing only whitespaces
     *  are ignored.
     */
    protected boolean ignoreEmptyCharacters;

    /** Controlls SAX event handling
     * If this is incremented all events are not forwarded to the next
     * pipeline component, but the hooks are still called.
     */
    protected int ignoreEventsCount;

    /** Controlls SAX event handling
     * If this is greater than zero, the hooks are not called. Attention,
     * make sure, that you decrement this counter properly as your hooks are
     * not called anymore!
     */
    protected int ignoreHooksCount;

    /**
     *  The used namespace for the SAX filtering.
     *  This is either the defaultNamespaceURI or the value
     *  set by the "namespaceURI" parameter for the pipeline.
     */
    protected String  namespaceURI;

    /**
     * This is the default namespace used by the transformer.
     * It should be set in the constructor.
     */
    protected String  defaultNamespaceURI;

    /** A stack for collecting information.
     *  The stack is important for collection information especially when
     *  the tags can be nested.
     */
    protected Stack   stack = new Stack();
    /** The stack of current used recorders */
    protected Stack   recorderStack = new Stack();

    /** The current Request object */
    protected Request            request;
    /** The current Response object */
    protected Response           response;
    /** The current Context object */
    protected Context            context;
    /** The current objectModel of the environment */
    protected Map                objectModel;
    /** The parameters specified in the sitemap */
    protected Parameters         parameters;
    /** The source attribute specified in the sitemap */
    protected String             source;
    /** The Avalon ComponentManager for getting Components */
    protected ComponentManager   manager;
    /** The SourceResolver for this request */
    protected SourceResolver     resolver;

    /** Are we already initialized for the current request? */
    private boolean isInitialized;

    /** Empty attributes (for performance). This can be used
     *  do create own attributes, but make sure to clean them
     *  afterwords.
     */
    protected AttributesImpl emptyAttributes = new AttributesImpl();

    /** The namespaces */
    private List namespaces = new ArrayList(5);

    /**
     * Avalon Configurable Interface
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
    }

    /**
     * Setup the next round.
     * The instance variables are initialised.
     * @param resolver The current SourceResolver
     * @param objectModel The objectModel of the environment.
     * @param src The value of the src attribute in the sitemap.
     * @param par The parameters from the sitemap.
     */
    public void setup(SourceResolver resolver,
                      Map            objectModel,
                      String         src,
                      Parameters     par)
    throws ProcessingException,
           SAXException,
           IOException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN setup resolver="+resolver+
                                   ", objectModel="+objectModel+
                                   ", src="+src+
                                   ", parameters="+par);
        }

        if (this.defaultNamespaceURI == null) {
            this.defaultNamespaceURI = this.namespaceURI;
        }
        this.objectModel = objectModel;

        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);
        this.context = ObjectModelHelper.getContext(objectModel);
        this.resolver = resolver;
        this.parameters = par;
        this.source = src;
        this.isInitialized = false;

        // get the current namespace
        this.namespaceURI = this.parameters.getParameter("namespaceURI", this.defaultNamespaceURI);

        this.ignoreHooksCount = 0;
        this.ignoreEventsCount = 0;
        this.ignoreWhitespaces = true;
        this.ignoreEmptyCharacters = false;

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END setup");
        }
    }

    /**
     *  Recycle this component.
     */
    public void recycle() {
        super.recycle();
        this.objectModel = null;
        this.request = null;
        this.response = null;
        this.context = null;
        this.resolver = null;
        this.stack.clear();
        this.recorderStack.clear();
        this.parameters = null;
        this.source = null;
        this.namespaces.clear();
    }

    /**
     * Avalon Composable Interface
     * @param manager The Avalon Component Manager
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    /**
     *  Process the SAX event. A new document is processed. The hook (method)
     *  <code>setupTransforming()</code> is invoked.
     *  @see org.xml.sax.ContentHandler#startDocument
     */
    public void startDocument()
    throws SAXException {
        if ( !this.isInitialized ) {
            try {
                this.setupTransforming();
            } catch (ProcessingException local) {
                throw new SAXException("ProcessingException: " + local, local);
            } catch (IOException ioe) {
                throw new SAXException("IOException: " + ioe, ioe);
            }
            this.isInitialized = true;
        }
        if (this.ignoreEventsCount == 0) super.startDocument();
    }

    /**
     *  Process the SAX event. The processing of the document is finished.
     *  @see org.xml.sax.ContentHandler#endDocument
     */
    public void endDocument()
    throws SAXException {
        if (this.ignoreEventsCount == 0) super.endDocument();
    }


    /**
     * Process the SAX event.
     * The namespace of the event is checked. If it is the defined namespace
     * for this transformer the startTransformingElement() hook is called.
     */
    public void startElement(String uri,
                             String name,
                             String raw,
                             Attributes attr)
    throws SAXException {
        if (uri != null
            && namespaceURI != null
            && uri.equals(namespaceURI) == true
            && this.ignoreHooksCount == 0) {

            // this is our namespace:
            try {
                this.startTransformingElement(uri, name, raw, attr);
            } catch (ProcessingException pException) {
                throw new SAXException("ProcessingException: " + pException, pException);
            } catch (IOException ioe) {
                throw new SAXException("Exception occured during processing: " + ioe, ioe);
            }
        } else {
            if (ignoreEventsCount == 0) super.startElement(uri, name, raw, attr);
        }
    }


    /**
     * Process the SAX event.
     * The namespace of the event is checked. If it is the defined namespace
     * for this transformer the endTransformingElement() hook is called.
     */
    public void endElement(String uri, String name, String raw) throws SAXException {
        if (uri != null
            && namespaceURI != null
            && uri.equals(namespaceURI) == true
            && this.ignoreHooksCount == 0) {

            // this is our namespace:
            try {
                this.endTransformingElement(uri, name, raw);
            } catch (ProcessingException pException) {
                throw new SAXException("ProcessingException: " + pException,
                    pException);
            } catch (IOException ioe) {
                throw new SAXException("Exception occured during processing: " + ioe, ioe);
            }
        } else {
            if (ignoreEventsCount == 0) super.endElement(uri, name, raw);
        }
    }

    /**
     * Process the SAX event.
     */
    public void characters(char[] p0, int p1, int p2)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            if (this.ignoreEmptyCharacters == true) {
                String value = new String(p0, p1, p2);
                if (value.trim().length() > 0) {
                    super.characters(p0, p1, p2);
                }
            } else {
                super.characters(p0, p1, p2);
            }
        }
    }

    /**
     * Process the SAX event.
     */
    public void ignorableWhitespace(char[] p0, int p1, int p2)
    throws SAXException {
        if (ignoreWhitespaces == false && ignoreEventsCount == 0) super.ignorableWhitespace(p0, p1, p2);
    }

    /*
     * Recording of events.
     * With this method all events are not forwarded to the next component in the pipeline.
     * They are recorded to create a document fragment.
     */
    private LexicalHandler   originalLexicalHandler;
    private ContentHandler   originalContentHandler;

    /**
     * Add a new recorder to the recording chain.
     * Do not invoke this method directly.
     */
    protected void addRecorder(XMLConsumer recorder) {
        if (this.recorderStack.empty() == true) {
            // redirect if first (top) recorder
            this.originalLexicalHandler = this.lexicalHandler;
            this.originalContentHandler = this.contentHandler;
        }
        this.setContentHandler(recorder);
        this.setLexicalHandler(recorder);
        this.recorderStack.push(recorder);
    }

    /**
     * Remove a recorder from the recording chain.
     * Do not invoke this method directly.
     */
    protected Object removeRecorder() {
        Object recorder = this.recorderStack.pop();
        if (this.recorderStack.empty() == true) {
            // undo redirect if no recorder any more
            this.setContentHandler(originalContentHandler);
            this.setLexicalHandler(originalLexicalHandler);
            this.originalLexicalHandler = null;
            this.originalContentHandler = null;
        } else {
            XMLConsumer next = (XMLConsumer)recorderStack.peek();
            this.setContentHandler(next);
            this.setLexicalHandler(next);
        }
        return recorder;
    }

    /**
     * Start DocumentFragment recording.
     * All invoking events are recorded and not forwarded. The resulting
     * DocumentFragment can be obtained by the matching endRecording() call.
     */
    public void startRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN startRecording");
        }
        DOMBuilder builder = new DOMBuilder();
        this.addRecorder(builder);
        builder.startDocument();
        builder.startElement("", "cocoon", "cocoon", new AttributesImpl());

        this.sendStartPrefixMapping();

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END startRecording");
        }
    }

    /**
     * Stop DocumentFragment recording.
     * All invoking events are recorded and not forwarded. This method returns
     * the resulting DocumentFragment.
     */
    public DocumentFragment endRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endRecording");
        }

        this.sendEndPrefixMapping();

        DOMBuilder builder = (DOMBuilder)this.removeRecorder();
        builder.endElement("", "cocoon", "cocoon");
        builder.endDocument();

        // Create Document Fragment
        final Document doc = builder.getDocument();
        final DocumentFragment recordedDocFrag = doc.createDocumentFragment();
        final Node root = doc.getDocumentElement();
        root.normalize();
        Node child;
        boolean appendedNode = false;
        while (root.hasChildNodes() == true) {
            child = root.getFirstChild();
            root.removeChild(child);
            // Leave out empty text nodes before any other node
            if (appendedNode == true
                || child.getNodeType() != Node.TEXT_NODE
                || child.getNodeValue().trim().length() > 0) {
                recordedDocFrag.appendChild(child);
                appendedNode = true;
            }
        }

        if (this.getLogger().isDebugEnabled()) {
            Object serializedXML = null;
            try {
                serializedXML = (recordedDocFrag == null ? "null" : XMLUtils.serializeNodeToXML(recordedDocFrag));
            } catch (ProcessingException ignore) {
                serializedXML = recordedDocFrag;
            }
            this.getLogger().debug("END endRecording fragment=" + serializedXML);
        }
        return recordedDocFrag;
    }

    /**
     * Start recording of text.
     * All events are not forwarded and the characters events
     * are merged to a string
     */
    public void startTextRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN startTextRecording");
        }
        XMLConsumer recorder = new TextRecorder();
        this.addRecorder(recorder);

        this.sendStartPrefixMapping();

        if (this.getLogger().isDebugEnabled()) {
           this.getLogger().debug("END startTextRecording");
        }
    }

    /**
     * Stop recording of text and return the recorded information.
     * @return The String.
     */
    public String endTextRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endTextRecording");
        }

        this.sendEndPrefixMapping();

        TextRecorder recorder = (TextRecorder)this.removeRecorder();
        String text = recorder.getText();

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END endTextRecording text="+text);
        }
        return text;
    }

    /**
     * Start recording of serialized xml
     * All events are converted to an xml string which can be retrieved by
     * endSerializedXMLRecording.
     * @param format The format for the serialized output. If <CODE>null</CODE>
     *               is specified, the default format is used.
     */
    public void startSerializedXMLRecording(Properties format)
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN startSerializedXMLRecording format="+format);
        }
        this.stack.push((format == null ? XMLUtils.defaultSerializeToXMLFormat() : format));
        this.startRecording();
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END startSerializedXMLRecording");
        }
    }

    /**
     * Return the serialized xml string.
     * @return A string containing the recorded xml information, formatted by
     * the properties passed to the corresponding startSerializedXMLRecording().
     */
    public String endSerializedXMLRecording()
    throws SAXException, ProcessingException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endSerializedXMLRecording");
        }
        DocumentFragment fragment = this.endRecording();
        String text = XMLUtils.serializeNode(fragment, (Properties)this.stack.pop());
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END endSerializedXMLRecording xml="+text);
        }
        return text;
    }

    /**
     * Start recording of parameters.
     * All events are not forwarded and the incoming xml is converted to
     * parameters. Each toplevel node is a parameter and its text subnodes
     * form the value.
     * The Parameters can eiter be retrieved by endParametersRecording().
     */
    public void startParametersRecording()
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN startParametersRecording");
        }
        XMLConsumer recorder = new ParametersRecorder();
        this.addRecorder(recorder);

        this.sendStartPrefixMapping();

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END startParametersRecording");
        }
    }

    /**
     * End recording of parameters
     * If source is null a new parameters object is created, otherwise
     * the parameters are added to this object.
     * @param source An optional parameters object.
     * @return The object containing all parameters.
     */
    public SourceParameters endParametersRecording(Parameters source)
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endParametersRecording source="+source);
        }
        this.sendEndPrefixMapping();
        ParametersRecorder recorder = (ParametersRecorder)this.removeRecorder();
        SourceParameters pars = recorder.getParameters(source);

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END endParametersRecording parameters="+pars);
        }
        return pars;
    }

    /**
     * End recording of parameters
     * If source is null a new parameters object is created, otherwise
     * the parameters are added to this object.
     * @param source An optional parameters object.
     * @return The object containing all parameters.
     */
    public SourceParameters endParametersRecording(SourceParameters source)
    throws SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN endParametersRecording source="+source);
        }
        this.sendEndPrefixMapping();
        ParametersRecorder recorder = (ParametersRecorder)this.removeRecorder();
        SourceParameters pars = recorder.getParameters(source);

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END endParametersRecording parameters="+pars);
        }
        return pars;
    }

    // ************
    // Hooks
    // ************

    /**
     * Setup the transformation of an xml document.
     * This method is called just before the transformation (sending of sax events)
     * starts. It should be used to initialize setup parameter depending on the
     * object modell.
     */
    public void setupTransforming()
    throws IOException, ProcessingException, SAXException {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("BEGIN setupTransforming");
        }
        this.stack.clear();
        this.recorderStack.clear();
        this.ignoreWhitespaces = true;
        this.ignoreEmptyCharacters = false;
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("END setupTransforming");
        }
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     * @param attr The attributes of the element.
     */
    public void startTransformingElement(String uri,
                                         String name,
                                         String raw,
                                         Attributes attr)
    throws ProcessingException, IOException, SAXException {
        //if (this.getLogger().isDebugEnabled()) {
        //    this.getLogger().debug("BEGIN startTransformingElement uri=" + uri + ", name=" + name + ", raw=" + raw + ", attr=" + attr + ")");
        //}
        if (this.ignoreEventsCount == 0) super.startElement(uri, name, raw, attr);
        //if (this.getLogger().isDebugEnabled()) {
        //    this.getLogger().debug("END startTransformingElement");
        //}
    }

    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     */
    public void endTransformingElement(String uri,
                                       String name,
                                       String raw)
    throws ProcessingException, IOException, SAXException {
        //if (this.getLogger().isDebugEnabled()) {
        //    this.getLogger().debug("BEGIN endTransformingElement uri=" + uri + ", name=" + name + ", raw=" + raw + ")");
        //}
        if (this.ignoreEventsCount == 0) super.endElement(uri, name, raw);
        //if (this.getLogger().isDebugEnabled()) {
        //    this.getLogger().debug("END endTransformingElement");
        //}
    }

    /**
     * Send SAX events to the next pipeline component.
     * The characters event for the given text is send to the next
     * component in the current pipeline.
     * @param text The string containing the information.
     */
    public void sendTextEvent(String text)
    throws SAXException {
        this.characters(text.toCharArray(), 0, text.length());
    }

    /**
     * Send SAX events to the next pipeline component.
     * The startElement event for the given element is send
     * to the next component in the current pipeline.
     * The element has no namespace and no attributes
     * @param localname The name of the event.
     */
    public void sendStartElementEvent(String localname)
    throws SAXException {
        this.startElement("", localname, localname, emptyAttributes);
    }

    /**
     * Send SAX events to the next pipeline component.
     * The startElement event for the given element is send
     * to the next component in the current pipeline.
     * The element has no namespace.
     * @param localname The name of the event.
     * @param attr The Attributes of the element
     */
    public void sendStartElementEvent(String localname, Attributes attr)
    throws SAXException {
        this.startElement("", localname, localname, attr);
    }

    /**
     * Send SAX events to the next pipeline component.
     * The endElement event for the given element is send
     * to the next component in the current pipeline.
     * The element has no namespace.
     * @param localname The name of the event.
     */
    public void sendEndElementEvent(String localname)
    throws SAXException {
        this.endElement("", localname, localname);
    }

    /**
     * Send SAX events to the next pipeline component.
     * The node is parsed and the events are send to
     * the next component in the pipeline.
     * @param node The tree to be included.
     */
    public void sendEvents(Node node)
    throws SAXException {
        IncludeXMLConsumer.includeNode(node, this, this);
    }

    /**
     * Send SAX events for the <code>SourceParameters</code>.
     * For each parametername/value pair an element is
     * created with the name of the parameter and the content
     * of this element is the value.
     */
    public void sendParametersEvents(SourceParameters pars)
    throws SAXException {
        if (pars != null) {
            Iterator names = pars.getParameterNames();
            String currentName;
            String currentValue;
            Iterator values;
            while (names.hasNext() == true) {
                currentName = (String)names.next();
                values = pars.getParameterValues(currentName);
                while (values.hasNext() == true) {
                    currentValue = (String)values.next();
                    this.sendStartElementEvent(currentName);
                    this.sendTextEvent(currentValue);
                    this.sendEndElementEvent(currentName);
                }
            }
        }
    }

    /**
     * SAX Event handling
     */
    public void startEntity (String name)
    throws SAXException {
        if (this.ignoreEventsCount == 0) super.startEntity(name);
    }

    /**
     * SAX Event handling
     */
    public void endEntity (String name)
    throws SAXException {
        if (this.ignoreEventsCount == 0) super.endEntity(name);
    }

    /**
     * Send all start prefix mapping events to the current content handler
     */
    protected void sendStartPrefixMapping()
    throws SAXException {
        int i,l;
        l = this.namespaces.size();
        String[] prefixAndUri;
        for(i=0; i<l; i++) {
           prefixAndUri = (String[])this.namespaces.get(i);
           super.contentHandler.startPrefixMapping(prefixAndUri[0], prefixAndUri[1]);
        }
    }

    /**
     * Send all end prefix mapping events to the current content handler
     */
    protected void sendEndPrefixMapping()
    throws SAXException {
        int i,l;
        l = this.namespaces.size();
        String[] prefixAndUri;
        for(i=0; i<l; i++) {
           prefixAndUri = (String[])this.namespaces.get(i);
           super.contentHandler.endPrefixMapping(prefixAndUri[0]);
        }
    }

    /**
     * SAX Event handling
     */
    public void setDocumentLocator(Locator locator) {
        if (this.ignoreEventsCount == 0) super.setDocumentLocator(locator);
    }

    /**
     * SAX Event handling
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (prefix != null) this.namespaces.add(new String[] {prefix, uri});
        if (this.ignoreEventsCount == 0) super.startPrefixMapping(prefix, uri);
    }

    /**
     * SAX Event handling
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (prefix != null) {
            // search the namespace prefix
            boolean found = false;
            int l = this.namespaces.size();
            int i = l-1;
            String currentPrefix;
            while (found == false && i >= 0) {
                currentPrefix = ((String[])this.namespaces.get(i))[0];
                if (currentPrefix.equals(prefix) == true) {
                    found = true;
                } else {
                    i--;
                }
            }
            if (found == false) {
                throw new SAXException("Namespace for prefix '"+ prefix + "' not found.");
            }
            this.namespaces.remove(i);
        }
        if (this.ignoreEventsCount == 0) super.endPrefixMapping(prefix);
    }

    /**
     * SAX Event handling
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (this.ignoreEventsCount == 0) super.processingInstruction(target, data);
    }

    /**
     * SAX Event handling
     */
    public void skippedEntity(String name)
            throws SAXException {
        if (this.ignoreEventsCount == 0) super.skippedEntity(name);
    }

    /**
     * SAX Event handling
     */
    public void startDTD(String name, String public_id, String system_id)
            throws SAXException {
        if (this.ignoreEventsCount == 0) super.startDTD(name, public_id, system_id);
    }

    /**
     * SAX Event handling
     */
    public void endDTD() throws SAXException {
        if (this.ignoreEventsCount == 0) super.endDTD();
    }

    /**
     * SAX Event handling
     */
    public void startCDATA() throws SAXException {
        if (this.ignoreEventsCount == 0) super.startCDATA();
    }

    /**
     * SAX Event handling
     */
    public void endCDATA() throws SAXException {
        if (this.ignoreEventsCount == 0) super.endCDATA();
    }


    /**
     * SAX Event handling
     */
    public void comment(char ary[], int start, int length)
    throws SAXException {
        if (this.ignoreEventsCount == 0) super.comment(ary, start, length);
    }

}
