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
 * @version CVS $Id: CastorTransformer.java,v 1.2 2003/09/05 07:04:34 cziegeler Exp $
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
