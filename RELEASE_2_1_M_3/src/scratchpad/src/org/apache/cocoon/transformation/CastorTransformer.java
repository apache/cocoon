package org.apache.cocoon.transformation;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.ProcessingException;
import org.apache.excalibur.source.Source;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;

/**
 * Castor transformer marshals a object from the Sitemap, Session, Request or
 * the Conext into a series of SAX events.
 *
 * Configuation: The CastorTransformer needs to be configured with a
 * default mapping. This mapping is used as long as no other mapping
 * is specified as the element.
 *
 *<pre>
 *  &ltmap:transformer name="CastorTransformer" src="org.apache.cocoon.transformation.CastorTransformer"&gt
 *    &ltmapping&gtcastor/xmapping.xml&lt/mapping&gt
 *  &lt/map:transformer&gt
 *</pre>
 *
 * A sample for the use:
 * <pre>
 *   &ltroot xmlns:castor="http://castor.exolab.org/cocoontransfomer"&gt
 *	&ltcastor:InsertBean name="invoice"/&gt
 *	&ltcastor:InsertBean name="product" scope="sitemap" mapping="castor/specicalmapping.xml"/&gt
 *  &lt/root&gt
 * </pre>
 * The CastorTransfomer support only one Element <code>castor:InsertBean</code>. This
 * element is replaced with the marshalled object. The Object given through the
 * attrbute <code>name</code> will be searched in the <code>sitemap, request,
 * session</code> and at least in <code>application</code>
 * If the scope is explicitly given, e.g , the object will ge located only here
 * The Attribut <code>mapping</code> specifys the mapping to be used. If not given
 * the default mapping is used
 * <pre/>
 *
 * @author <a href="mailto:mauch@imkenberg.de">Thorsten Mauch</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: CastorTransformer.java,v 1.2 2003/03/16 18:03:55 vgritsenko Exp $
 */
public class CastorTransformer extends AbstractTransformer implements Configurable {
    private static final String CASTOR_URI = "http://castor.exolab.org/cocoontransfomer";

    private final static String CMD_INSERT_BEAN = "InsertBean";
    private final static String ATTRIB_NAME = "name";
    private final static String ATTRIB_SCOPE = "scope";
    private final static String SCOPE_SITEMAP = "sitemap";
    private final static String SCOPE_SESSION = "session";
    private final static String SCOPE_REQUEST = "request";
    private final static String SCOPE_CONTEXT = "context";
    private final static String ATTRIB_MAPPING = "mapping";

    // Stores all used mappings in the static cache
    private static HashMap mappings;

    private Map objectModel;
    private SourceResolver resolver;

    private String defaultMappingName;
    private Mapping defaultMapping;

    private boolean in_castor_element = false;


    public void configure(Configuration conf) throws ConfigurationException {
        this.defaultMappingName = conf.getChild(ATTRIB_MAPPING).getValue();
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters params)
            throws ProcessingException, SAXException, IOException {
        this.objectModel = objectModel;
        this.resolver = resolver;

        if (defaultMappingName != null) {
            try {
                defaultMapping = loadMapping(defaultMappingName);
            } catch (Exception e) {
                getLogger().warn("Unable to load mapping " + defaultMappingName, e);
            }
        }
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (CASTOR_URI.equals(uri)) {
            in_castor_element = false;
        } else {
            super.endElement(uri,  name,  raw);
        }
    }

    public void startElement(String uri, String name, String raw, Attributes attr) throws SAXException {
        if (CASTOR_URI.equals(uri)) {
            in_castor_element= true;
            process (name, attr);
        } else {
            super.startElement(uri,  name,  raw,  attr);
        }
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
        if (!in_castor_element) {
            super.characters(ch,start, len);
        }
    }

    private void process (String command, Attributes attr) throws SAXException {
        if (CMD_INSERT_BEAN.equals(command)) {
            final String scope = attr.getValue(ATTRIB_SCOPE);
            final String name = attr.getValue(ATTRIB_NAME);
            final String mapping = attr.getValue(ATTRIB_MAPPING);
            if (name == null){
                throw new SAXException("Required attribute name is missing on element " + CMD_INSERT_BEAN);
            }

            Request request = ObjectModelHelper.getRequest(objectModel);

            Object bean = null;
            if (scope == null) {
                // Search for bean in (1) objectModel, (2) request, (3) session, and (4) context.
                bean = request.getAttribute(name);
                if (bean == null) {
                    Session session = request.getSession(false);
                    if (session != null) {
                        bean = session.getAttribute(name);
                    }
                }
                if (bean == null) {
                    Context context = ObjectModelHelper.getContext(objectModel);
                    if (context != null) {
                        bean = context.getAttribute(name);
                    }
                }
                if (bean == null) {
                    bean = objectModel.get(name);
                }
            } else if (SCOPE_SITEMAP.equals(scope)) {
                bean = objectModel.get(name);
            } else if (SCOPE_REQUEST.equals(scope)) {
                bean = request.getAttribute(name);
            } if (SCOPE_SESSION.equals(scope)) {
                Session session = request.getSession(false);
                if (session != null) {
                    bean = session.getAttribute(name);
                }
            } if (SCOPE_CONTEXT.equals(scope)) {
                Context context = ObjectModelHelper.getContext(objectModel);
                if(context != null){
                    bean=context.getAttribute(name);
                }
            }

            if (bean != null) {
                insertBean(name, bean, mapping);
            } else {
                getLogger().warn("Bean " +name + " could not be found");
            }
        } else {
            throw new SAXException("Unknown command: " + command);
        }
    }

    private void insertBean (String name, Object bean, String map) {
        try {
            Marshaller marshaller = new Marshaller(new IncludeXMLConsumer(super.contentHandler));
            try {
                Mapping mapping = null;
                if (map != null) {
                    mapping = loadMapping(map);
                } else {
                    mapping = defaultMapping;
                }
                marshaller.setMapping(mapping);
            } catch (Exception e) {
                getLogger().warn("Unable to load mapping " + map, e);
            }

            if (bean instanceof Collection) {
                Iterator i = ((Collection)bean).iterator();
                while (i.hasNext()) {
                    marshaller.marshal(i.next());
                }
            } else {
                marshaller.marshal(bean);
            }
        } catch (Exception e) {
            getLogger().warn("Failed to marshal bean " + name, e);
        }
    }

    private Mapping loadMapping (String file) throws MappingException, IOException {
        synchronized (CastorTransformer.class) {
            // Create cache (once) if does not exist
            if (mappings == null) {
                mappings = new HashMap();
            }

            Mapping mapping;
            Source source = resolver.resolveURI(file);
            try {
                mapping = (Mapping) mappings.get(source.getURI());
                if (mapping == null) {
                    // mapping not found in cache or the cache is new
                    mapping = new Mapping();
                    InputSource in = new InputSource(source.getInputStream());
                    mapping.loadMapping(in);
                    mappings.put (source.getURI(), mapping);
                }
            } finally {
                resolver.release(source);
            }

            return mapping;
        }
    }
}
