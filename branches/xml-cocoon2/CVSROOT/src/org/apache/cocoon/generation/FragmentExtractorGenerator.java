package org.apache.cocoon.generation;

import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.avalon.Parameters;
import org.apache.avalon.util.pool.Pool;
import org.apache.cocoon.PoolClient;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.rmi.server.UID;
import java.io.IOException;

/** The generation half of FragmentExtractor.
 * FragmentExtractor is a transformer-generator pair which is designed to allow
 * sitemap managers to extract certain nodes from a SAX stream and move them
 * into a separate pipeline. The main use for this is to extract inline SVG
 * images and serve them up through a separate pipeline, usually serializing
 * them to PNG or JPEG format first.
 *
 * This is by no means complete yet, but it should prove useful, particularly
 * for offline generation.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2001-02-19 21:13:31 $
 */
public class FragmentExtractorGenerator extends AbstractGenerator implements PoolClient {

    /** The fragment store. */
    private static Map fragmentStore;

    private Pool pool;

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public void returnToPool() {
        this.pool.put(this);
    }

    /** Construct a new <code>FragmentExtractorGenerator</code> and ensure that the
     * fragment store is initialized and threadsafe (since it is a global store, not
     * per-instance.
     */
    public FragmentExtractorGenerator() {
        synchronized(this) {
            if ( fragmentStore == null ) {
                fragmentStore = Collections.synchronizedMap(new HashMap());
            }
        }
    }

    public void setup(EntityResolver resolver, Map objectModel, String src, Parameters par)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver,objectModel,src,par);
        if ( fragmentStore.get(source) == null ) {
            throw new ResourceNotFoundException("Could not find fragment " + source + ".");
        }

    }

    public void generate() throws SAXException {
        // Obtain the fragmentID  (which is simply the filename portion of the source)
        getLogger().debug("FragmentExtractorGenerator retrieving document " + source + ".");
        Document doc = (Document) fragmentStore.get(source);
        DOMStreamer streamer = new DOMStreamer(this.contentHandler,this.lexicalHandler);

        streamer.stream(doc);
        fragmentStore.remove(source);
    }

    public static String store(Document doc) {
        String id = (new UID()).toString();
        fragmentStore.put(id,doc);
        return id;
    }
}

