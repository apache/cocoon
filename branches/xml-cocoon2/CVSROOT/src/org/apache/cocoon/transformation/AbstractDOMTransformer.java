package org.apache.cocoon.transformation;

import java.util.Map;
import java.io.IOException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ext.LexicalHandler;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.cocoon.xml.dom.DOMFactory;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.components.parser.Parser;
import org.apache.cocoon.Roles;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Parameters;

/**
 * An Abstract DOM Transformer, for use when a transformer needs a DOM-based
 * view of the document.
 * Subclass this interface and implement <code>transform(Document doc)</code>.
 * If you need a ComponentManager there is an instance variable
 * <code>manager</code> for use.
 * 
 * @author <A HREF="rossb@apache.org">Ross Burton</A>
 * @author <A HREF="brobertson@mta.ca">Bruce G. Robertson</A>
 * @version CVS $Revision $Date
 */
public abstract class AbstractDOMTransformer extends DOMBuilder
	implements Transformer, DOMBuilder.Listener, Composer {

	/** The SAX entity resolver */
	protected EntityResolver resolver;
	/** The request object model */
	protected Map objectModel;
	/** The URI requested */
	protected String source;
	/** Parameters in the sitemap */
	protected Parameters parameters;

	/**
	 * A <code>ComponentManager</code> which is available for use.
	 */
	protected ComponentManager manager;

	public AbstractDOMTransformer() {
		// Set the factory later, when we have a Component Manager to get a
		// Parser from
		super();
		super.listener = this;
	}

	/**
	 * Set the component manager.
	 */
	public void compose(ComponentManager manager) {
		this.manager = manager;
		// Get a parser and use it as a DOM factory
		try {
		    log.debug("Looking up " + Roles.PARSER);
		    Parser p = (Parser)manager.lookup(Roles.PARSER);
		    super.factory = (DOMFactory)p;
		} catch (Exception e) {
		    log.error("Could not find component", e);
		}
	}

    /**
     * Set the <code>EntityResolver</code>, objectModel <code>Map</code>,
     * the source and sitemap <code>Parameters</code> used to process the request.
	 *
	 * If you wish to process the parameters, override this method, call
	 * <code>super()</code> and then add your code.
     */
    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
		throws ProcessingException, SAXException, IOException {
		this.resolver = resolver;
		this.objectModel = objectModel;
		this.source = src;
		this.parameters = par;
	}

	/**
	 * This method is called when the Document is finished.
	 * @param doc The DOM Document object representing this SAX stream
	 * @see DOMBuilder.Listener
	 */
	public void notify(Document doc) throws SAXException {
		// Call the user's transform method
		Document newdoc = transform(doc);
		// Now we stream the DOM tree out
		DOMStreamer s = new DOMStreamer(contentHandler, lexicalHandler);
		s.stream(newdoc);
	}

	/**
	 * Transform the specified DOM, returning a new DOM to stream down the pipeline.
	 * @param doc The DOM Document representing the SAX stream
	 * @returns A DOM Document to stream down the pipeline
	 */
	protected abstract Document transform(Document doc) ;


    /** The <code>ContentHandler</code> receiving SAX events. */
    protected ContentHandler contentHandler;
    
    /** The <code>LexicalHandler</code> receiving SAX events. */
    protected LexicalHandler lexicalHandler;

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.contentHandler = consumer;
        this.lexicalHandler = consumer;
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code> or
     *                                  the <code>XMLConsumer</code> were
     *                                  already set.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }
}
