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
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.NOPValidity;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The encodeURL transformer emits encoded URLs.
 * <p>
 *   This transformer applies encodeURL method to URLs.
 *   You may want to use this transform to avoid doing the manually
 *   encodeURL() calls.
 * </p>
 * <p>
 *   Usually this transformer is appended as last transformer before
 *   the serialization process. In this case it is possible to encode
 *   URLs introduced in the generator, and xslt transformer phase.
 * </p>
 * <p>
 *   You can specify which attributes hold URL values in order to restrict
 *   URL rewriting to specific attributes only.
 * </p>
 * <p>
 * Usage in a sitemap:
 * </p>
 * <pre><tt>
 *   &lt;map:composition&gt;
 *   ...
 *     &lt;map:transformers&gt;
 *     ...
 *       &lt;map:transformer type=&quot;encodeURL&quot;
 *         src=&quot;org.apache.cocoon.optional.transformation.EncodeURLTransformer&quot;&gt;
 *         &lt;exclude-name&gt;img/@src&lt;/exclude-name&gt;
 *         &lt;include-name&gt;.&amp;asterik;/@href|.&amp;asterik;/@src|.&amp;asterik;/@action&lt;/include-name&gt;
 *       &lt;/map:transformer&gt;
 *   ...
 *   &lt;map:pipelines&gt;
 *     &lt;map:pipeline&gt;
 *       ...
 *       &lt;map:transform type=&quot;encodeURL&quot;/&gt;
 *       ...
 * </pre></tt>
 *
 * @author <a href="mailto:bh22351@i-one.at">Bernhard Huber</a>
 * @version CVS $Id: EncodeURLTransformer.java,v 1.11 2004/03/08 14:03:31 cziegeler Exp $
 */
public class EncodeURLTransformer
  extends AbstractTransformer
  implements Configurable, CacheableProcessingComponent {

    /**
     * Configuration name for specifying excluding patterns,
     * ie exclude-name.
     */
    public final static String EXCLUDE_NAME = "exclude-name";

    /**
     * Configuration name for specifying including patterns,
     * ie include-name.
     */
    public final static String INCLUDE_NAME = "include-name";

    /**
     * Configuration default exclude pattern,
     * ie img/@src
     */
    public final static String EXCLUDE_NAME_DEFAULT = "img/@src";

    /**
     * Configuration default exclude pattern,
     * ie .*\/@href|.*\/@action|frame/@src
     */
    public final static String INCLUDE_NAME_DEFAULT = ".*/@href|.*/@action|frame/@src";

    private String includeNameConfigure = INCLUDE_NAME_DEFAULT;
    private String excludeNameConfigure = EXCLUDE_NAME_DEFAULT;

    private ElementAttributeMatching elementAttributeMatching;
    private Response response;
    private boolean isEncodeURLNeeded;
    private Session session;

    /**
     * check if encoding of URLs is neccessary.
     *
     * This is true if session object exists, and session-id
     * was provided from URL, or session is new.
     * The result is stored in some instance variables
     */
    protected void checkForEncoding(Request request) {
        this.session = request.getSession(false);
        this.isEncodeURLNeeded = false;

        if ( null != this.session ) {
            // do encoding if session id is from URL, or the session is new,
            // fixes BUG #13855, due to paint007@mc.duke.edu
            if ( request.isRequestedSessionIdFromURL() || this.session.isNew()) {
                this.isEncodeURLNeeded = true;
            }
        }
    }

    /**
     * Setup the transformer.
     * <p>
     *   Setup include, and exclude patterns from the parameters
     * </p>
     *
     * @param resolver source resolver
     * @param objectModel sitemap objects
     * @param parameters request parameters
     *
     */
    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters)
    throws ProcessingException, SAXException, IOException {

        this.checkForEncoding(ObjectModelHelper.getRequest(objectModel));

        if (this.isEncodeURLNeeded) {
            this.response = ObjectModelHelper.getResponse(objectModel);

            // don't check if URL encoding is needed now, as
            // a generator might create a new session
            final String includeName = parameters.getParameter(INCLUDE_NAME,
                                                               this.includeNameConfigure);
            final String excludeName = parameters.getParameter(EXCLUDE_NAME,
                                                               this.excludeNameConfigure);
            try {
                this.elementAttributeMatching = new ElementAttributeMatching(includeName, excludeName);
            } catch (RESyntaxException reex) {
                final String message = "Cannot parse include-name: " + includeName + " " +
                    "or exclude-name: " + excludeName + "!";
                throw new ProcessingException(message, reex);
            }
        }
    }


    /**
     * BEGIN SitemapComponent methods
     *
     * @param  configuration               Description of Parameter
     * @exception  ConfigurationException  Description of Exception
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        Configuration child;

        child = configuration.getChild(INCLUDE_NAME);
        this.includeNameConfigure = child.getValue(INCLUDE_NAME_DEFAULT);

        child = configuration.getChild(EXCLUDE_NAME);
        this.excludeNameConfigure = child.getValue(EXCLUDE_NAME_DEFAULT);

        if (this.includeNameConfigure == null) {
            String message = "Configure " + INCLUDE_NAME + "!";
            throw new ConfigurationException(message);
        }
        if (this.excludeNameConfigure == null) {
            String message = "Configure " + EXCLUDE_NAME + "!";
            throw new ConfigurationException(message);
        }
    }


    /**
     * Recycle resources of this transformer
     */
    public void recycle() {
        super.recycle();
        this.response = null;
        this.session = null;
        this.elementAttributeMatching = null;
    }


    /**
     * Generate the unique key.
     * This key must be unique inside the space of this component.
     *
     * @return The generated key hashes the src
     */
    public java.io.Serializable getKey() {
        if (this.isEncodeURLNeeded) {
            return null;
        } else {
            return "1";
        }
    }

    /**
     * Generate the validity object.
     *
     * @return The generated validity object or <code>null</code> if the
     *         component is currently not cacheable.
     */
    public SourceValidity getValidity() {
        if (this.isEncodeURLNeeded) {
            return null;
        } else {
            return NOPValidity.SHARED_INSTANCE;
        }
    }

    /**
     * Start parsing an element
     *
     * @param  uri               of the element
     * @param  name              of the element
     * @param  raw               name of the element
     * @param  attributes        list
     * @exception  SAXException  Description of Exception
     */
    public void startElement(String uri, String name, String raw, Attributes attributes)
    throws SAXException {
        if (this.isEncodeURLNeeded && this.elementAttributeMatching != null) {
            String lname = name;
            if (attributes != null && attributes.getLength() > 0) {
                AttributesImpl new_attributes = new AttributesImpl(attributes);
                for (int i = 0; i < new_attributes.getLength(); i++) {
                    String attr_lname = new_attributes.getLocalName(i);

                    String value = new_attributes.getValue(i);

                    if (elementAttributeMatching.matchesElementAttribute(lname, attr_lname)) {
                        // don't use simply this.response.encodeURL(value)
                        // but be more smart about the url encoding
                        final String new_value = this.encodeURL(value);
                        if (getLogger().isDebugEnabled()) {
                            this.getLogger().debug("element/@attribute matches: " + name + "/@" + attr_lname);
                            this.getLogger().debug("encodeURL: " + value + " -> " + new_value);
                        }
                        new_attributes.setValue(i, new_value);
                    }
                }
                // parent handles element using encoded attribute values
                super.contentHandler.startElement(uri, name, raw, new_attributes);
                return;
            }
        }
        // no match, parent handles element as-is
        super.contentHandler.startElement(uri, name, raw, attributes);
    }

    /**
     * Do the URL rewriting.
     * <p>
     *   Check if <code>url</code> contains already the sessionid, some servlet-engines
     *   just appends the session-id without checking if the sessionid is already present.
     * </p>
     *
     * @param  url       the URL probably without sessionid.
     * @return           String the original url inclusive the sessionid
     */
    private String encodeURL(String url) {
        String encoded_url;
        if (this.response != null) {
            // As some servlet-engine does not check if url has been already rewritten
            if (this.session != null && url.indexOf(this.session.getId()) > -1) {
                // url contains already the session id encoded
                encoded_url = url;
            } else {
                // do encode the session id
                encoded_url = this.response.encodeURL(url);
            }
        } else {
            encoded_url = url;
        }
        return encoded_url;
    }

    /**
     * A helper class for matching element names, and attribute names.
     *
     * <p>
     *  For given include-name, exclude-name decide if element-attribute pair
     *  matches. This class defines the precedence and matching algorithm.
     * </p>
     *
     * @author     <a href="mailto:bh22351@i-one.at">Bernhard Huber</a>
     * @version    CVS $Id: EncodeURLTransformer.java,v 1.11 2004/03/08 14:03:31 cziegeler Exp $
     */
    public class ElementAttributeMatching {
        /**
         * Regular expression of including patterns
         *
         */
        protected RE includeNameRE;
        /**
         * Regular expression of excluding patterns
         *
         */
        protected RE excludeNameRE;


        /**
         *Constructor for the ElementAttributeMatching object
         *
         * @param  includeName            Description of Parameter
         * @param  excludeName            Description of Parameter
         * @exception  RESyntaxException  Description of Exception
         */
        public ElementAttributeMatching(String includeName, String excludeName) throws RESyntaxException {
            includeNameRE = new RE(includeName, RE.MATCH_CASEINDEPENDENT);
            excludeNameRE = new RE(excludeName, RE.MATCH_CASEINDEPENDENT);
        }


        /**
         * Return true iff element_name attr_name pair is not matched by exclude-name,
         * but is matched by include-name
         *
         * @param  element_name
         * @param  attr_name
         * @return               boolean true iff value of attribute_name should get rewritten, else
         *   false.
         */
        public boolean matchesElementAttribute(String element_name, String attr_name) {
            String element_attr_name = canonicalizeElementAttribute(element_name, attr_name);

            if (excludeNameRE != null && includeNameRE != null) {
                return !matchesExcludesElementAttribute(element_attr_name) &&
                        matchesIncludesElementAttribute(element_attr_name);
            } else {
                return false;
            }
        }


        /**
         * Build from elementname, and attribute name a single string.
         * <p>
         *   String concatenated <code>element name + "/@" + attribute name</code>
         *   is matched against the include and excluding patterns.
         * </p>
         *
         * @param  element_name  Description of Parameter
         * @param  attr_name     Description of Parameter
         * @return               Description of the Returned Value
         */
        private String canonicalizeElementAttribute(String element_name, String attr_name) {
            return element_name + "/@" + attr_name;
        }


        /**
         * Return true iff element_name attr_name pair is matched by exclude-name.
         *
         * @param  element_attr_name
         * @return                    boolean true iff exclude-name matches element_name, attr_name, else
         *   false.
         */
        private boolean matchesExcludesElementAttribute(String element_attr_name) {
            boolean match = excludeNameRE.match(element_attr_name);
            return match;
        }


        /**
         * Return true iff element_name attr_name pair is matched by include-name.
         *
         * @param  element_attr_name
         * @return                    boolean true iff include-name matches element_name, attr_name, else
         *   false.
         */
        private boolean matchesIncludesElementAttribute(String element_attr_name) {
            boolean match = includeNameRE.match(element_attr_name);
            return match;
        }
    }
}

