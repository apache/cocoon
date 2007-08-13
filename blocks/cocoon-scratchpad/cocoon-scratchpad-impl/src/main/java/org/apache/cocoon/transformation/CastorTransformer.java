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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.source.Source;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.UnmarshalHandler;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Castor transformer marshals a object from the Sitemap, Session, Request or
 * the Conext into a series of SAX events.
 *
 * <h3>Configuation</h3>
 * <p>The CastorTransformer needs to be configured with a
 * default mapping. This mapping is used as long as no other mapping
 * is specified as the element.</p>
 *
 *<pre>
 *  &ltmap:transformer name="CastorTransformer" src="org.apache.cocoon.transformation.CastorTransformer"&gt
 *    &ltmapping&gtcastor/xmapping.xml&lt/mapping&gt
 *  &lt/map:transformer&gt
 *</pre>
 *
 * <p>Sample usage:</p>
 * <pre>
 *   &lt;root xmlns:castor="http://apache.org/cocoon/castor/1.0"&gt;
 *     &lt;castor:marshal name="invoice"/&gt;
 *     &lt;castor:unmarshal name="product" scope="sitemap" mapping="castor/specicalmapping.xml"/&gt;
 *   &lt;/root&gt;
 * </pre>
 *
 * <p>The CastorTransfomer supports two elements
 * <code>castor:unmarshal</code> and <code>castor:marshal</code>.</p>
 *
 * <p>The marshal element is replaced with the marshaled object.
 * The Object given through the attrbute <code>name</code>
 * will be searched in the <code>sitemap, request, session</code> and at
 * least in <code>application</code> If the scope is explicitly given, e.g ,
 * the object will ge located only here. The Attribute <code>mapping</code>
 * specifys the mapping to be used. The attribute <code>command</code> specifies a class that
 * implements CastorMarshalCommand and will be called before and after marshaling.</p>
 *
 * <p>The elements within the unmarshal element will be sent to the castor unmarshaller
 * the resulting java object with be placed in the object specified by name and scope (see also marshal element).
 * The <code>command</code> attribute specifies the class that implements CastorUnmarshalCommand
 * and will be called before and after unmarshaling.</p>
 *
 * @version $Id$
 */
public class CastorTransformer extends AbstractTransformer implements Configurable {

    private static final String CASTOR_URI = "http://apache.org/cocoon/castor/1.0";

    private static final String CMD_UNMARSHAL = "unmarshal";
    private static final String CMD_MARSHAL = "marshal";
    private final static String ATTRIB_NAME = "name";
    private final static String ATTRIB_SCOPE = "scope";
    private final static String SCOPE_SITEMAP = "sitemap";
    private final static String SCOPE_SESSION = "session";
    private final static String SCOPE_REQUEST = "request";
    private final static String SCOPE_CONTEXT = "context";
    private final static String ATTRIB_MAPPING = "mapping";

    // FIXME Use Store for mappings cache instead of HashMap
    private static HashMap mappings;

    /** The map of namespace prefixes. */
    private Map prefixMap;

    private Unmarshaller unmarshaller;
    private UnmarshalHandler unmarshalHandler;
    private ContentHandler unmarshalContentHandler;
    private String beanName;
    private String beanScope;

    private Map objectModel;
    private SourceResolver resolver;

    private String defaultMappingName;
    private Mapping defaultMapping;

    private boolean in_castor_element;


    public void configure(Configuration conf) throws ConfigurationException {
        this.defaultMappingName = conf.getChild(ATTRIB_MAPPING).getValue();
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters params)
    throws ProcessingException, SAXException, IOException {
        this.objectModel = objectModel;
        this.resolver = resolver;
        this.prefixMap = new HashMap();
        this.in_castor_element = false;

        if (defaultMappingName != null) {
            try {
                defaultMapping = loadMapping(defaultMappingName);
            } catch (Exception e) {
                getLogger().warn("Unable to load mapping " + defaultMappingName, e);
            }
        }
    }

    public void recycle() {
        this.prefixMap = null;
        this.unmarshaller = null;
        this.unmarshalHandler = null;
        this.unmarshalContentHandler = null;
        this.beanName = null;
        this.beanScope = null;
        this.objectModel = null;
        this.resolver = null;
        this.defaultMappingName = null;
        this.defaultMapping = null;
        super.recycle();
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (CASTOR_URI.equals(uri)) {
            in_castor_element = false;
        } else if (unmarshalContentHandler != null) {
            // check if this marks the end of the unmarshaling
            if ((CASTOR_URI.equals(uri)) && (CMD_UNMARSHAL.equals(name))) {

                // End marshaling, remove prefixes
                Iterator itt = prefixMap.entrySet().iterator();
                while (itt.hasNext()) {
                   Map.Entry entry = (Map.Entry) itt.next();
                   unmarshalContentHandler.endPrefixMapping((String)entry.getKey());
                }

                // end document
                unmarshalContentHandler.endDocument();
                unmarshalContentHandler = null;

                // store the result of the unmarshaller
                Object root = unmarshalHandler.getObject();
                this.storeBean(objectModel, beanName, beanScope, root);
            } else {
                unmarshalContentHandler.endElement(uri, name, raw);
            }
        } else if (CASTOR_URI.equals(uri)) {
        } else {
            super.endElement(uri,  name,  raw);
        }
    }

    public void startElement(String uri, String name, String raw, Attributes attr)
    throws SAXException {
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
        if (command.equals(CMD_MARSHAL)) {
            String scope = attr.getValue(ATTRIB_SCOPE);
            String name = attr.getValue(ATTRIB_NAME);
            String mapping = attr.getValue(ATTRIB_MAPPING);

            if (name == null) {
                getLogger().error("Attribute to insert not set");
            } else {
                marshal(objectModel, name, scope, mapping);
            }
        } else if (command.equals(CMD_UNMARSHAL)) {
            beanScope = attr.getValue(ATTRIB_SCOPE);
            beanName = attr.getValue(ATTRIB_NAME);

            if (beanScope == null) {
              getLogger().error("Destination for unmarshaled bean not set");
              return;
            }

            if (beanName == null) {
              getLogger().error("Name of unmarshaled bean not set");
              return;
            }
            String mappingpath = attr.getValue(ATTRIB_MAPPING);

            // Create the unmarshaller
            unmarshaller = new Unmarshaller((Class) null);
            // Only set a mapping if one is specified
            if (mappingpath != null) {
                Mapping mapping;

                try {
                    mapping = loadMapping(mappingpath);
                    unmarshaller.setMapping(mapping);
                } catch (MappingException e) {
                    getLogger().error("Could not load mapping file " + mappingpath, e);
                } catch (IOException e) {
                    getLogger().error("Could not load mapping file " + mappingpath, e);
                }
            }

//            unmarshalCommand = null;
/*            if (commandclass != null) {
    try {
      unmarshalCommand = (CastorUnmarshalCommand)Class.forName(commandclass).newInstance();
      unmarshalCommand.enableLogging(this.getLogger());

      unmarshalCommand.pre(unmarshaller, xmlConsumer, objectModel, params);
    } catch (InstantiationException e) {
      getLogger().error("Could not instantiate class " + commandclass ,e);
    } catch (IllegalAccessException e) {
      getLogger().error("Could not access class " + commandclass ,e);
    } catch (ClassNotFoundException e) {
      getLogger().error("Could not instantiate class " + commandclass ,e);
    }
    }*/

            // Create the unmarshalhandler and wrap it with a SAX2 contentHandler
            unmarshalHandler = unmarshaller.createHandler();

            try {
                unmarshalContentHandler = Unmarshaller.getContentHandler(
                                                  unmarshalHandler);
                unmarshalContentHandler.startDocument();

                Iterator itt = prefixMap.entrySet().iterator();
                while ( itt.hasNext() ) {
                   Map.Entry entry = (Map.Entry)itt.next();
                   unmarshalContentHandler.startPrefixMapping((String)entry.getKey(), (String)entry.getValue());
                }

            } catch (SAXException e) {
                getLogger().error("Could not get contenthandler from unmarshaller", e);
            }
        } else {
            throw new SAXException("Unknown command: " + command);
        }
    }

    private void marshal(Map objectModel, String name, String scope, String mappingpath) {
        try {
            Marshaller marshaller = new Marshaller(new IncludeXMLConsumer(super.contentHandler));
            try {
                Mapping mapping = null;
                if (mappingpath != null) {
                    mapping = loadMapping(mappingpath);
                } else {
                    mapping = defaultMapping;
                }
                marshaller.setMapping(mapping);
            } catch (Exception e) {
                getLogger().warn("Unable to load mapping " + mappingpath, e);
            }

            Object bean = findBean(objectModel, name, scope);

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

    /**
     * Find the bean that should be marshaled by the transformer
     *
     * @param objectModel The Cocoon objectmodel
     * @param name The name of the bean
     * @param scope The source specification of the bean REQUEST/SESSION etc.
     * @return The bean that was found
     */
    private Object findBean(Map objectModel, String name, String scope) {
        Object bean = null;
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (scope == null) {
            // Search for bean in (1) objectModel, (2) request, (3) session, and (4) context.
            bean = objectModel.get(name);
            if (bean == null) {
                bean = request.getAttribute(name);
            }
            if (bean == null) {
                HttpSession session = request.getSession(false);
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
        } else if (SCOPE_SITEMAP.equals(scope)) {
            bean = objectModel.get(name);
        } else if (SCOPE_REQUEST.equals(scope)) {
            bean = request.getAttribute(name);
        } if (SCOPE_SESSION.equals(scope)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                bean = session.getAttribute(name);
            }
        } else if (SCOPE_CONTEXT.equals(scope)) {
            Context context = ObjectModelHelper.getContext(objectModel);
            if (context != null) {
                bean = context.getAttribute(name);
            }
        }

        return bean;
    }

    private void storeBean(Map objectModel, String name, String scope, Object bean) {
        Request request = ObjectModelHelper.getRequest(objectModel);

        if (SCOPE_SITEMAP.equals(scope)) {
            objectModel.put(name, bean);
        } else if (SCOPE_REQUEST.equals(scope)) {
            request.setAttribute(name, bean);
        } else if (SCOPE_SESSION.equals(scope)) {
            request.getSession(true).setAttribute(name, bean);
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
