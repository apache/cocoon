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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.helpers.ParametersRecorder;
import org.apache.cocoon.transformation.helpers.TextRecorder;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.excalibur.source.SourceParameters;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is the basis for all transformers. It provides various useful
 * methods and hooks for implementing own custom transformers.
 *
 * <p>The basic behaviour of each transformer consists of the following four
 * parts:</p>
 * <ul>
 * <li>Listen for specific events with a given namespace</li>
 * <li>Collect information via these events</li>
 * <li>Process the information</li>
 * <li>Create new events from the processed information</li>
 * </ul>
 *
 * <p>For all these four purposes the AbstractSAXTransformer offers some
 * powerful methods and hooks:</p>
 *
 * <h3>Namespace handling</h3>
 * By setting the instance variable namespaceURI to the namespace the
 * events are filtered and only events with this namespace are send to
 * the two hooks: <code>startTransformingElement</code> and
 * <code>endTransformingElement</code>. It is possible to override the default
 * namespace for the transformer by specifying the parameter "namespaceURI"
 * in the pipeline. This avoids possible namespace collisions.
 *
 * <h3>Recording of information</h3>
 * There are several methods for recording information, e.g. startRecording(),
 * startTextRecording() etc. These methods collect information from the xml
 * stream for further processing.
 *
 * <h3>Creating new events</h3>
 * New events can be easily created with the <code>sendEvents()</code>
 * method, the <code>sendStartElementEvent()</code> methods, the
 * <code>sendEndElementEvent()</code> method or the
 * <code>sendTextEvent()</code> method.
 *
 * <h3>Initialization</h3>
 * Before the document is processed the <code>setupTransforming</code> hook
 * is invoked.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id$
*/
public abstract class AbstractSAXTransformer
        extends AbstractTransformer
        implements Serviceable, Configurable, Recyclable {

    /**
     * Controlls SAX event handling.
     * If set to true all whitespace events are ignored.
     */
    protected boolean ignoreWhitespaces;

    /**
     * Controlls SAX event handling.
     * If set to true all characters events containing only whitespaces
     * are ignored.
     */
    protected boolean ignoreEmptyCharacters;

    /**
     * Controlls SAX event handling.
     * If this is incremented all events are not forwarded to the next
     * pipeline component, but the hooks are still called.
     */
    protected int ignoreEventsCount;

    /**
     * Controlls SAX event handling.
     * If this is greater than zero, the hooks are not called. Attention,
     * make sure, that you decrement this counter properly as your hooks are
     * not called anymore!
     */
    protected int ignoreHooksCount;

    /**
     * The namespace used by the transformer for the SAX events filtering.
     * This either equals to the {@link #defaultNamespaceURI} or to the value
     * set by the <code>namespaceURI</code> sitemap parameter for the pipeline.
     * Must never be null.
     */
    protected String namespaceURI;

    /**
     * This is the default namespace used by the transformer.
     * Implementations should set its value in the constructor.
     * Must never be null.
     */
    protected String defaultNamespaceURI;

    /**
     * A stack for collecting information.
     * The stack is important for collection information especially when
     * the tags can be nested.
     */
    protected Stack stack = new Stack();

    /**
     * The stack of current used recorders
     */
    protected Stack recorderStack = new Stack();

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
    /** The Avalon ServiceManager for getting Components */
    protected ServiceManager   manager;
    /** The SourceResolver for this request */
    protected SourceResolver     resolver;

    /** Are we already initialized for the current request? */
    private boolean isInitialized;

    /**
     * Empty attributes (for performance). This can be used
     * do create own attributes, but make sure to clean them
     * afterwords.
     */
    protected AttributesImpl emptyAttributes = new AttributesImpl();

    /** The namespaces and their prefixes */
    private List namespaces = new ArrayList(5);

    /** The current prefix for our namespace */
    private String ourPrefix;


    /* (non-Javadoc)
     * @see Configurable#configure(Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(SourceResolver, Map, String, Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map            objectModel,
                      String         src,
                      Parameters     params)
    throws ProcessingException, SAXException, IOException {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Setup resolver=" + resolver +
                              ", objectModel=" + objectModel +
                              ", src=" + src +
                              ", parameters=" + params);
        }

        // defaultNamespaceURI should never be null
        if (this.defaultNamespaceURI == null) {
            this.defaultNamespaceURI = "";
        }
        this.objectModel = objectModel;

        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);
        this.context = ObjectModelHelper.getContext(objectModel);
        this.resolver = resolver;
        this.parameters = params;
        this.source = src;
        this.isInitialized = false;

        // get the current namespace
        this.namespaceURI = params.getParameter("namespaceURI",
                                                this.defaultNamespaceURI);

        this.ignoreHooksCount = 0;
        this.ignoreEventsCount = 0;
        this.ignoreWhitespaces = true;
        this.ignoreEmptyCharacters = false;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        this.namespaceURI = null;
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
        this.ourPrefix = null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     *  Process the SAX event. A new document is processed. The hook (method)
     *  <code>setupTransforming()</code> is invoked.
     *
     *  @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument()
    throws SAXException {
        if (!this.isInitialized) {
            try {
                this.setupTransforming();
            } catch (ProcessingException local) {
                throw new SAXException("ProcessingException: " + local, local);
            } catch (IOException ioe) {
                throw new SAXException("IOException: " + ioe, ioe);
            }
            this.isInitialized = true;
        }
        if (this.ignoreEventsCount == 0) {
            super.startDocument();
        }
    }

    /**
     *  Process the SAX event. The processing of the document is finished.
     *  @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument()
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.endDocument();
        }
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
        if (namespaceURI.equals(uri) && ignoreHooksCount == 0) {
            // this is our namespace:
            try {
                startTransformingElement(uri, name, raw, attr);
            } catch (ProcessingException e) {
                throw new SAXException("ProcessingException: " + e, e);
            } catch (IOException e) {
                throw new SAXException("IOException occured during processing: " + e, e);
            }
        } else {
            if (ignoreEventsCount == 0) {
                super.startElement(uri, name, raw, attr);
            }
        }
    }

    /**
     * Process the SAX event.
     * The namespace of the event is checked. If it is the defined namespace
     * for this transformer the endTransformingElement() hook is called.
     */
    public void endElement(String uri, String name, String raw)
    throws SAXException {
        if (namespaceURI.equals(uri) && this.ignoreHooksCount == 0) {
            // this is our namespace:
            try {
                endTransformingElement(uri, name, raw);
            } catch (ProcessingException e) {
                throw new SAXException("ProcessingException: " + e, e);
            } catch (IOException e) {
                throw new SAXException("IOException occured during processing: " + e, e);
            }
        } else {
            if (ignoreEventsCount == 0) {
                super.endElement(uri, name, raw);
            }
        }
    }

    /**
     * Process the SAX event.
     */
    public void characters(char[] p0, int p1, int p2)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            if (this.ignoreEmptyCharacters) {
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
        if (ignoreWhitespaces == false && ignoreEventsCount == 0) {
            super.ignorableWhitespace(p0, p1, p2);
        }
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
        if (this.recorderStack.empty()) {
            // redirect if first (top) recorder
            this.originalLexicalHandler = this.lexicalHandler;
            this.originalContentHandler = this.contentHandler;
        }
        setContentHandler(recorder);
        setLexicalHandler(recorder);
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
            setContentHandler(originalContentHandler);
            setLexicalHandler(originalLexicalHandler);
            this.originalLexicalHandler = null;
            this.originalContentHandler = null;
        } else {
            XMLConsumer next = (XMLConsumer) recorderStack.peek();
            setContentHandler(next);
            setLexicalHandler(next);
        }

        return recorder;
    }

    /**
     * Start recording of SAX events
     * All incomming events are recorded and not forwarded. The result
     * can be obtained by the matching endSAXRecording() call.
     * @since 2.1.5
     */
    public void startSAXRecording()
    throws SAXException {
        addRecorder(new SaxBuffer());
    }

    /**
     * Stop DocumentFragment recording.
     * All incomming events are recorded and not forwarded. This method returns
     * the resulting DocumentFragment.
     * @since 2.1.5
     */
    public XMLizable endSAXRecording()
    throws SAXException {
        return (XMLizable) this.removeRecorder();
    }

    /**
     * Start recording of a text.
     * No events forwarded, and all characters events
     * are collected into a string.
     */
    public void startTextRecording()
    throws SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Start text recording");
        }
        addRecorder(new TextRecorder());

        sendStartPrefixMapping();
    }

    /**
     * Stop recording of text and return the recorded information.
     * @return The String.
     */
    public String endTextRecording()
    throws SAXException {
        sendEndPrefixMapping();

        TextRecorder recorder = (TextRecorder) removeRecorder();
        String text = recorder.getText();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("End text recording. Text=" + text);
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Start serialized XML recording. Format=" + format);
        }
        this.stack.push(format == null? XMLUtils.createPropertiesForXML(false): format);
        startRecording();
    }

    /**
     * Return the serialized xml string.
     * @return A string containing the recorded xml information, formatted by
     * the properties passed to the corresponding startSerializedXMLRecording().
     */
    public String endSerializedXMLRecording()
    throws SAXException, ProcessingException {
        DocumentFragment fragment = endRecording();
        String text = XMLUtils.serializeNode(fragment, (Properties) this.stack.pop());
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("End serialized XML recording. XML=" + text);
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Start parameters recording");
        }
        addRecorder(new ParametersRecorder());

        sendStartPrefixMapping();
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
        sendEndPrefixMapping();

        ParametersRecorder recorder = (ParametersRecorder) this.removeRecorder();
        SourceParameters parameters = recorder.getParameters(source);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("End parameters recording. Parameters=" + parameters);
        }
        return parameters;
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
        sendEndPrefixMapping();

        ParametersRecorder recorder = (ParametersRecorder) removeRecorder();
        SourceParameters parameters = recorder.getParameters(source);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("End parameters recording. Parameters=" + parameters);
        }
        return parameters;
    }

    /**
     * Start DocumentFragment recording.
     * All incomming events are recorded and not forwarded. The resulting
     * DocumentFragment can be obtained by the matching endRecording() call.
     */
    public void startRecording()
    throws SAXException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Start recording");
        }
        DOMBuilder builder = new DOMBuilder();
        addRecorder(builder);
        builder.startDocument();
        builder.startElement("", "cocoon", "cocoon", new AttributesImpl());

        sendStartPrefixMapping();
    }

    /**
     * Stop DocumentFragment recording.
     * All incomming events are recorded and not forwarded. This method returns
     * the resulting DocumentFragment.
     */
    public DocumentFragment endRecording()
    throws SAXException {
        sendEndPrefixMapping();

        DOMBuilder builder = (DOMBuilder)removeRecorder();
        builder.endElement("", "cocoon", "cocoon");
        builder.endDocument();

        // Create Document Fragment
        final Document doc = builder.getDocument();
        final DocumentFragment recordedDocFrag = doc.createDocumentFragment();
        final Node root = doc.getDocumentElement();
        root.normalize();

        boolean appendedNode = false;
        while (root.hasChildNodes() == true) {
            Node child = root.getFirstChild();
            root.removeChild(child);
            // Leave out empty text nodes before any other node
            if (appendedNode == true
                || child.getNodeType() != Node.TEXT_NODE
                || child.getNodeValue().trim().length() > 0) {
                recordedDocFrag.appendChild(child);
                appendedNode = true;
            }
        }

        if (getLogger().isDebugEnabled()) {
            Object serializedXML = null;
            try {
                serializedXML = recordedDocFrag == null? "null": XMLUtils.serializeNode(recordedDocFrag, XMLUtils.createPropertiesForXML(false));
            } catch (ProcessingException ignore) {
                serializedXML = recordedDocFrag;
            }
            getLogger().debug("End recording. Fragment=" + serializedXML);
        }
        return recordedDocFrag;
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
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("setupTransforming");
        }
        this.stack.clear();
        this.recorderStack.clear();
        this.ignoreWhitespaces = true;
        this.ignoreEmptyCharacters = false;
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
        if (this.ignoreEventsCount == 0) {
            super.startElement(uri, name, raw, attr);
        }
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
        if (this.ignoreEventsCount == 0) {
            super.endElement(uri, name, raw);
        }
    }

    /**
     * Send SAX events to the next pipeline component.
     * The characters event for the given text is send to the next
     * component in the current pipeline.
     * @param text The string containing the information.
     */
    public void sendTextEvent(String text)
    throws SAXException {
        characters(text.toCharArray(), 0, text.length());
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
        startElement("", localname, localname, emptyAttributes);
    }

    /**
     * Send SAX events to the next pipeline component.
     * The startElement event for the given element is send
     * to the next component in the current pipeline.
     * The element has the namespace of the transformer,
     * but not attributes
     * @param localname The name of the event.
     */
    public void sendStartElementEventNS(String localname)
    throws SAXException {
        startElement(this.namespaceURI,
                     localname, this.ourPrefix + ':' + localname, emptyAttributes);
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
        startElement("", localname, localname, attr);
    }

    /**
     * Send SAX events to the next pipeline component.
     * The startElement event for the given element is send
     * to the next component in the current pipeline.
     * The element has the namespace of the transformer.
     * @param localname The name of the event.
     * @param attr The Attributes of the element
     */
    public void sendStartElementEventNS(String localname, Attributes attr)
    throws SAXException {
        startElement(this.namespaceURI,
                     localname, this.ourPrefix + ':' + localname, attr);
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
        endElement("", localname, localname);
    }

    /**
     * Send SAX events to the next pipeline component.
     * The endElement event for the given element is send
     * to the next component in the current pipeline.
     * The element has the namespace of the transformer.
     * @param localname The name of the event.
     */
    public void sendEndElementEventNS(String localname)
    throws SAXException {
        endElement(this.namespaceURI,
                   localname, this.ourPrefix + ':' + localname);
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
            while (names.hasNext()) {
                final String currentName = (String)names.next();
                Iterator values = pars.getParameterValues(currentName);
                while (values.hasNext()) {
                    final String currentValue = (String)values.next();
                    sendStartElementEvent(currentName);
                    sendTextEvent(currentValue);
                    sendEndElementEvent(currentName);
                }
            }
        }
    }

    /**
     * SAX Event handling
     */
    public void startEntity (String name)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.startEntity(name);
        }
    }

    /**
     * SAX Event handling
     */
    public void endEntity (String name)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.endEntity(name);
        }
    }

    /**
     * Send all start prefix mapping events to the current content handler
     */
    protected void sendStartPrefixMapping()
    throws SAXException {

        final int l = this.namespaces.size();
        for(int i = 0; i < l; i++) {
           String[] prefixAndUri = (String[])this.namespaces.get(i);
           super.contentHandler.startPrefixMapping(prefixAndUri[0], prefixAndUri[1]);
        }
    }

    /**
     * Send all end prefix mapping events to the current content handler
     */
    protected void sendEndPrefixMapping()
    throws SAXException {

        final int l = this.namespaces.size();
        for(int i=0; i<l; i++) {
           String[] prefixAndUri = (String[])this.namespaces.get(i);
           super.contentHandler.endPrefixMapping(prefixAndUri[0]);
        }
    }

    /**
     * SAX Event handling
     */
    public void setDocumentLocator(Locator locator) {
        if (this.ignoreEventsCount == 0) {
            super.setDocumentLocator(locator);
        }
    }

    /**
     * SAX Event handling
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (prefix != null) {
            this.namespaces.add(new String[] {prefix, uri});
        }
        if (namespaceURI.equals(uri)) {
            this.ourPrefix = prefix;
        }
        if (this.ignoreEventsCount == 0) {
            super.startPrefixMapping(prefix, uri);
        }
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
            while (!found && i >= 0) {
                String currentPrefix = ((String[])this.namespaces.get(i))[0];
                if (currentPrefix.equals(prefix)) {
                    found = true;
                } else {
                    i--;
                }
            }
            if (!found) {
                throw new SAXException("Namespace for prefix '"+ prefix + "' not found.");
            }

            this.namespaces.remove(i);
            if (prefix.equals(this.ourPrefix)) {
                this.ourPrefix = null;
                // now search if we have a different prefix for our namespace
                found = false;
                l = this.namespaces.size();
                i = l-1;
                while (!found && i >= 0) {
                    String currentNS = ((String[])this.namespaces.get(i))[1];
                    if (namespaceURI.equals(currentNS)) {
                        found = true;
                    } else {
                        i--;
                    }
                }
                if (found) {
                    this.ourPrefix = ((String[])this.namespaces.get(i))[0];
                }
            }
        }
        if (this.ignoreEventsCount == 0) {
            super.endPrefixMapping(prefix);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.processingInstruction(target, data);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.skippedEntity(name);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     */
    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.startDTD(name, public_id, system_id);
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.endDTD();
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.startCDATA();
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.endCDATA();
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char ary[], int start, int length)
    throws SAXException {
        if (this.ignoreEventsCount == 0) {
            super.comment(ary, start, length);
        }
    }
}
