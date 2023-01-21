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
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The <code>LDAPTransformer</code> can be plugged into a pipeline to transform
 * the SAX events into queries and responses to/from a LDAP interface.
 * <br>
 * The file will be specified in a parameter tag in the sitemap pipeline to the
 * transformer as follows:
 * <p>
 * <code>
 * &lt;map:transform type="ldap"/&gt;<br>
 * </code>
 * </p>
 * <br>
 *
 * The following DTD is valid:<br>
 * &lt;!ELEMENT execute-query (attribute+ | show-attribute? | scope? | initializer? | initial-context? | authentication? | error-element? | sax-error?  doc-element? | row-element? | version? | serverurl? | rootdn? | password? | deref-link? | count-limit? | searchbase, filter)&gt;<br>
 * &lt;!ELEMENT execute-increment (attribute | show-attribute? | scope? | initializer? | initial-context? | authentication? | error-element? | sax-error? | doc-element? | row-element? | version? | serverurl? | rootdn? | password? | deref-link? | count-limit? | searchbase, filter)&gt;<br>
 * increments (+1) an integer attribute on a directory-server (ldap)<br>
 * &lt;!ELEMENT execute-replace (attribute | show-attribute? | scope? | initializer? | initial-context? | authentication? | error-element? | sax-error? | doc-element? | row-element? | version? | serverurl? | rootdn? | password? | deref-link? | count-limit? | searchbase, filter)&gt;<br>
 * replace attribute on a directory-server (ldap)<br>
 * &lt;!ELEMENT execute-add (attribute | show-attribute? | scope? | initializer? | initial-context? | authentication? | error-element? | sax-error? | doc-element? | row-element? | version? | serverurl? | rootdn? | password? | deref-link? | count-limit? | searchbase, filter)&gt;<br>
 * add attribute on a directory-server (ldap)<br>
 * <br>
 * &lt;!ELEMENT initializer (#PCDATA)&gt;+ (default: "com.sun.jndi.ldap.LdapCtxFactory")<br>
 * &lt;!ELEMENT initial-context (#EMPTY)&gt;<br>
 * &lt;!ATTLIST initial-context name CDATA #REQUIRED
                                value CDATA #REQUIRED &gt;<br>
 * &lt;!ELEMENT authentication (#PCDATA)&gt;+ (default: "simple")<br>
 * &lt;!ELEMENT version (#PCDATA)&gt;+ (default: "2")<br>
 * &lt;!ELEMENT serverurl (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT port (#PCDATA)&gt;+ (default: 389)<br>
 * &lt;!ELEMENT rootdn (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT password (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT scope (ONELEVEL_SCOPE | SUBTREE_SCOPE | OBJECT_SCOPE)&gt;+ (default: ONELEVEL_SCOPE)<br>
 * &lt;!ELEMENT searchbase (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT doc-element (#PCDATA)&gt;+ (default: "doc-element")<br>
 * &lt;!ELEMENT row-element (#PCDATA)&gt;+ (default: "row-element")<br>
 * &lt;!ELEMENT dn-attribute (#PCDATA)&gt;+ (default: "" meaning no DN attribute)<br>
 * &lt;!ELEMENT error-element (#PCDATA)&gt;+ (default: "ldap-error") (in case of error returned error tag)<br>
 * &lt;!ELEMENT sax_error (TRUE  | FALSE)&gt+; (default: FALSE) (throws SAX-Exception instead of error tag)<br>
 * &lt;!ELEMENT attribute (#PCDATA)&gt;<br>
 * &lt;!ATTLIST attribute name	CDATA	#IMPLIED 
                          mode (append|replace) 'replace' #IMPLIED &gt; (in case execute-replace or execute-add elements using) <br>

 * &lt;!ELEMENT show-attribute (TRUE | FALSE)&gt; (default: TRUE)<br>
 * &lt;!ELEMENT filter (#PCDATA | execute-query)+&gt;<br>
 * &lt;!ELEMENT deref-link (TRUE | FALSE)&gt; (default: FALSE)<br>
 * &lt;!ELEMENT count-limit (#PCDATA)&gt; (integer default: 0 -&gt; no limit)<br>
 * &lt;!ELEMENT time-limit (#PCDATA)&gt; (integer default: 0 -&gt; infinite)<br>
 * &lt;!ELEMENT debug (TRUE  | FALSE)&gt+; (default: FALSE)<br>
 * <br>
 * + can also be defined as parameter in the sitemap.
 * <br>
 *
 * @version $Id$
 */
public class LDAPTransformer extends AbstractTransformer {

    /** The LDAP namespace ("http://apache.org/cocoon/LDAP/1.0")*/
    public static final String my_uri = "http://apache.org/cocoon/LDAP/1.0";
    public static final String my_name = "LDAPTransformer";

    /** The LDAP namespace element names */
    public static final String MAGIC_ATTRIBUTE_ELEMENT = "attribute";
    public static final String MAGIC_ATTRIBUTE_ELEMENT_ATTRIBUTE = "name";
    public static final String MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE = "mode";
    public static final String MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_DEFAULT = "replace";
    public static final String MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_VALUE_A = "append";
    public static final String MAGIC_AUTHENTICATION_ELEMENT = "authentication";
    public static final String MAGIC_COUNT_LIMIT_ELEMENT = "count-limit";
    public static final String MAGIC_DEBUG_ELEMENT = "debug";
    public static final String MAGIC_DEREF_LINK_ELEMENT = "deref-link";
    public static final String MAGIC_DOC_ELEMENT = "doc-element";
    public static final String MAGIC_ENCODING_ELEMENT = "encoding";
    public static final String MAGIC_ERROR_ELEMENT = "error-element";
    public static final String MAGIC_EXECUTE_ADD = "execute-add";
    public static final String MAGIC_EXECUTE_INCREMENT = "execute-increment";
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
    public static final String MAGIC_EXECUTE_REPLACE = "execute-replace";
    public static final String MAGIC_FILTER_ELEMENT = "filter";
    public static final String MAGIC_INITIALIZER_ELEMENT = "initializer";
    public static final String MAGIC_INITIAL_CONTEXT_ELEMENT = "initial-context";
    public static final String MAGIC_INITIAL_CONTEXT_NAME_ATTRIBUTE = "name";
    public static final String MAGIC_INITIAL_CONTEXT_VALUE_ATTRIBUTE = "value";
    public static final String MAGIC_PASSWORD_ELEMENT = "password";
    public static final String MAGIC_PORT_ELEMENT = "port";
    public static final String MAGIC_ROOT_DN_ELEMENT = "rootdn";
    public static final String MAGIC_ROW_ELEMENT = "row-element";
    public static final String MAGIC_DN_ATTRIBUTE = "dn-attribute";
    public static final String MAGIC_SAX_ERROR = "sax-error";
    public static final String MAGIC_SCOPE_ELEMENT = "scope";
    public static final String MAGIC_SEARCHBASE_ELEMENT = "searchbase";
    public static final String MAGIC_SERVERURL_ELEMENT = "serverurl";
    public static final String MAGIC_SHOW_ATTRIBUTE_ELEMENT = "show-attribute";
    public static final String MAGIC_TIME_LIMIT_ELEMENT = "time-limit";
    public static final String MAGIC_VERSION_ELEMENT = "version";

    /** The states we are allowed to be in */
    public static final int STATE_OUTSIDE = 0;
    public static final int STATE_INSIDE_EXECUTE_QUERY = 1;
    public static final int STATE_INSIDE_EXECUTE_INCREMENT = 2;
    public static final int STATE_INSIDE_EXECUTE_ELEMENT = 3;
    public static final int STATE_INSIDE_INITIALIZER_ELEMENT = 4;
    public static final int STATE_INSIDE_SERVERURL_ELEMENT = 5;
    public static final int STATE_INSIDE_PORT_ELEMENT = 6;
    public static final int STATE_INSIDE_SCOPE_ELEMENT = 7;
    public static final int STATE_INSIDE_VERSION_ELEMENT = 8;
    public static final int STATE_INSIDE_AUTHENTICATION_ELEMENT = 9;
    public static final int STATE_INSIDE_ROOT_DN_ELEMENT = 10;
    public static final int STATE_INSIDE_PASSWORD_ELEMENT = 11;
    public static final int STATE_INSIDE_SEARCHBASE_ELEMENT = 12;
    public static final int STATE_INSIDE_DOC_ELEMENT = 13;
    public static final int STATE_INSIDE_ROW_ELEMENT = 14;
    public static final int STATE_INSIDE_ATTRIBUTE_ELEMENT = 15;
    public static final int STATE_INSIDE_SHOW_ATTRIBUTE_ELEMENT = 16;
    public static final int STATE_INSIDE_ERROR_ELEMENT = 17;
    public static final int STATE_INSIDE_FILTER_ELEMENT = 18;
    public static final int STATE_INSIDE_DEREF_LINK_ELEMENT = 19;
    public static final int STATE_INSIDE_COUNT_LIMIT_ELEMENT = 20;
    public static final int STATE_INSIDE_TIME_LIMIT_ELEMENT = 21;
    public static final int STATE_INSIDE_DEBUG_ELEMENT = 22;
    public static final int STATE_INSIDE_SAX_ERROR_ELEMENT = 23;
    public static final int STATE_INSIDE_EXECUTE_REPLACE = 24;
    public static final int STATE_INSIDE_EXECUTE_ADD = 25;
    public static final int STATE_INSIDE_DN_ATTRIBUTE = 26;
    public static final int STATE_INSIDE_INITIAL_CONTEXT_ELEMENT = 27;

    /** Default parameters that might apply to all queries */
    protected HashMap default_properties = new HashMap();

    /** The name of the value element we're currently receiving */
    protected String current_name;

    /** The current state of the event receiving FSM */
    protected int current_state = STATE_OUTSIDE;

    /** The value of the value element we're currently receiving */
    protected StringBuffer current_value = new StringBuffer();

    /** The list of queries that we're currently working on */
    protected Vector queries = new Vector();

    /** The offset of the current query in the queries list */
    protected int current_query_index = -1;

    /** SAX producing state information */
    protected XMLConsumer xml_consumer;
    protected LexicalHandler lexical_handler;

    /** SitemapComponent methods */

    public void setup(SourceResolver resolver, Map objectModel, String source, Parameters parameters)
        throws ProcessingException, SAXException, IOException {
        current_state = STATE_OUTSIDE;

        setDefaultProperty(parameters, MAGIC_INITIALIZER_ELEMENT);    // Check the initializer
        setDefaultProperty(parameters, MAGIC_VERSION_ELEMENT);        // Check the version
        setDefaultProperty(parameters, MAGIC_AUTHENTICATION_ELEMENT); // Check the authentication
        setDefaultProperty(parameters, MAGIC_SCOPE_ELEMENT);          // Check the scope
        setDefaultProperty(parameters, MAGIC_SERVERURL_ELEMENT);      // Check the serverurl
        setDefaultProperty(parameters, MAGIC_ROOT_DN_ELEMENT);        // Check the ldap-root_dn
        setDefaultProperty(parameters, MAGIC_PASSWORD_ELEMENT);       // Check the ldap-pwd
        setDefaultProperty(parameters, MAGIC_PORT_ELEMENT);           // Check the port
        setDefaultProperty(parameters, MAGIC_SEARCHBASE_ELEMENT);     // Check the searchbase
        setDefaultProperty(parameters, MAGIC_DOC_ELEMENT);            // Check the doc-element
        setDefaultProperty(parameters, MAGIC_ROW_ELEMENT);            // Check the row-element
        setDefaultProperty(parameters, MAGIC_DN_ATTRIBUTE);           // Check the dn-attribute
        setDefaultProperty(parameters, MAGIC_ERROR_ELEMENT);          // Check the error-element
        setDefaultProperty(parameters, MAGIC_SAX_ERROR);              // Check the sax-error
        setDefaultProperty(parameters, MAGIC_DEREF_LINK_ELEMENT);     // Check the deref-link-element
        setDefaultProperty(parameters, MAGIC_COUNT_LIMIT_ELEMENT);    // Check the count-limit-element
        setDefaultProperty(parameters, MAGIC_TIME_LIMIT_ELEMENT);     // Check the time-limit-element
        setDefaultProperty(parameters, MAGIC_DEBUG_ELEMENT);          // Check the debug-element
        setDefaultProperty(parameters, MAGIC_ENCODING_ELEMENT);       // Check the encoding
        setDefaultProperty(parameters, MAGIC_FILTER_ELEMENT);         // Check the filter-element
    }

    /** My very own methods */

    /* Helper to set default properties */
    protected void setDefaultProperty(Parameters parameters, String propertyName) {
        String parameter = parameters.getParameter(propertyName, null);
        if (parameter != null) {
            default_properties.put(propertyName, parameter);
        }
    }
    
    /* Helper class to store a new Query in the stack */
    protected void storeQuery(int nextState) {
        current_state = nextState;
        current_query_index = queries.size();
        LDAPQuery query = new LDAPQuery(this);
        queries.addElement(query);
        getCurrentQuery().toDo = nextState;
        getCurrentQuery().query_index = current_query_index;
    }

    /**
     * This will be the meat of LDAPTransformer, where the query is run.
     */
    protected void executeQuery(int index) throws SAXException {
        this.contentHandler.startPrefixMapping("", LDAPTransformer.my_uri);
        LDAPQuery query = (LDAPQuery)queries.elementAt(index);
        try {
            query.execute();
        } catch (NamingException e) {
            getLogger().error(e.toString());
            throw new SAXException(e);
        } catch (Exception e) {
            getLogger().error(e.toString());
            throw new SAXException(e);
        }
        this.contentHandler.endPrefixMapping("");
    }

    protected static void throwIllegalStateException(String message) {
        throw new IllegalStateException(my_name + ": " + message);
    }

    protected void startExecuteElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_OUTSIDE :
            case LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT :
                storeQuery(LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY);
                break;
            default :
                throwIllegalStateException("Not expecting a start execute-query element");
        }
    }

    protected void startExecuteElement(int state, String name) {
        switch (current_state) {
            case LDAPTransformer.STATE_OUTSIDE :
            case LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY :
                storeQuery(state);
                break;
            default :
                throwIllegalStateException("Not expecting a start " + name + " element");
        }
    }

    protected void endExecuteElement(int state, String name) throws SAXException {
        if (current_state == state) {
            executeQuery(current_query_index);
            queries.remove(current_query_index);
            --current_query_index;
            if (current_query_index > -1) {
                current_state = getCurrentQuery().toDo;
            } else {
                queries.removeAllElements();
                current_state = LDAPTransformer.STATE_OUTSIDE;
            }
        } else {
            throwIllegalStateException("Not expecting a end " + name + " element");
        }
    }

    protected void startQueryParameterElement(int state, String name) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
            case STATE_INSIDE_EXECUTE_INCREMENT :
            case STATE_INSIDE_EXECUTE_REPLACE :
            case STATE_INSIDE_EXECUTE_ADD :
                current_value.setLength(0);
                current_state = state;
                getCurrentQuery().current_state = state;
                break;
            default :
                throwIllegalStateException("Not expecting a start " + name + " element");
        }
    }

    protected void startParameterElement(int state, String name) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
            case STATE_INSIDE_EXECUTE_INCREMENT :
            case STATE_INSIDE_EXECUTE_REPLACE :
            case STATE_INSIDE_EXECUTE_ADD :
                current_value.setLength(0);
                current_state = state;
                break;
            default :
                throwIllegalStateException("Not expecting a start " + name + " serverurl element");
        }
    }

    protected void endInitializerElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT :
                getCurrentQuery().initializer = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end initializer element");
        }
    }

    protected void endScopeElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT :
                getCurrentQuery().scope = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end scope element");
        }
    }

    protected void endAuthenticationElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT :
                getCurrentQuery().authentication = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end authentication element");
        }
    }

    protected void endServerurlElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_SERVERURL_ELEMENT :
                getCurrentQuery().serverurl = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end serverurl element");
        }
    }

    protected void endPortElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_PORT_ELEMENT :
                getCurrentQuery().port = Integer.parseInt(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end server element");
        }
    }

    protected void startShowAttributeElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
            case STATE_INSIDE_EXECUTE_INCREMENT :
          //case STATE_INSIDE_EXECUTE_REPLACE:
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SHOW_ATTRIBUTE_ELEMENT;
                break;
            default :
                throwIllegalStateException("Not expecting a start show-attribute element");
        }
    }

    protected void endShowAttributeElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_SHOW_ATTRIBUTE_ELEMENT :
                if (!BooleanUtils.toBoolean(current_value.toString())) {
                    getCurrentQuery().showAttribute = false;
                }
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end show-attribute element");
        }
    }

    protected void endSearchbaseElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_SEARCHBASE_ELEMENT :
                getCurrentQuery().searchbase = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end searchbase element");
        }
    }

    protected void endDocElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_DOC_ELEMENT :
                getCurrentQuery().doc_element = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end doc-element element");
        }
    }

    protected void endRowElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_ROW_ELEMENT :
                getCurrentQuery().row_element = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end row-element element");
        }
    }

    protected void endDnAttribute() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_DN_ATTRIBUTE :
                getCurrentQuery().dn_attribute = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end dn-attribute element");
        }
    }

    protected void endErrorElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_ERROR_ELEMENT :
                getCurrentQuery().error_element = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end error-element element");
        }
    }

    protected void endSaxError() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_SAX_ERROR_ELEMENT :
                if (current_value.toString().toUpperCase().equals("TRUE")) {
                    getCurrentQuery().sax_error = true;
                }
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end sax-error element");
        }
    }

    protected void endRootDnElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_ROOT_DN_ELEMENT :
                getCurrentQuery().root_dn = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end root-dn element");
        }
    }

    protected void endPasswordElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_PASSWORD_ELEMENT :
                getCurrentQuery().password = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end password element");
        }
    }

    protected void startAttributeElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_state = LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT;
                current_value.setLength(0);
                break;
            case STATE_INSIDE_EXECUTE_REPLACE :
                boolean is_name_present = false;
                String mode = MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_DEFAULT;
                if (attributes != null && attributes.getLength() > 0) {
                    AttributesImpl new_attributes = new AttributesImpl(attributes);
                    for (int i = 0; i < new_attributes.getLength(); i++) {
                        String attr_name = new_attributes.getLocalName(i);
                        if (attr_name.equals(MAGIC_ATTRIBUTE_ELEMENT_ATTRIBUTE)) {
                            String value = new_attributes.getValue(i);
                            getCurrentQuery().addAttrList(value);
                            is_name_present = true;
                        } else if (attr_name.equals(MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE)) {
                            if (new_attributes.getValue(i).equals(MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_VALUE_A))
                                mode = MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_VALUE_A;
                        } else {
                            this.getLogger().debug("Invalid attribute match: " + attr_name);
                            throwIllegalStateException("Invalid attribute match in start attribute element");
                        }
                    }
                }
                if (!is_name_present) {
                    this.getLogger().debug("Do not match 'value' attribute");
                    throwIllegalStateException("Do not match 'value' attribute in start attribute element");
                }
                getCurrentQuery().addAttrModeVal(mode);
                current_state = LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT;
                current_value.setLength(0);
                break;
            case STATE_INSIDE_EXECUTE_ADD :
                if (attributes != null && attributes.getLength() > 0) {
                    AttributesImpl new_attributes = new AttributesImpl(attributes);
                    for (int i = 0; i < new_attributes.getLength(); i++) {
                        String attr_name = new_attributes.getLocalName(i);
                        if (attr_name.equals(MAGIC_ATTRIBUTE_ELEMENT_ATTRIBUTE)) {
                            String value = new_attributes.getValue(i);
                            getCurrentQuery().addAttrList(value);
                        } else {
                            this.getLogger().debug("Invalid attribute match: " + attr_name);
                            throwIllegalStateException("Invalid attribute match in start attribute element");
                        }
                    }
                } else {
                    this.getLogger().debug("Do not match 'value' attribute");
                    throwIllegalStateException("Do not match 'value' attribute in start attribute element");
                }
                current_state = LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT;
                current_value.setLength(0);
                break;
            default :
                throwIllegalStateException("Not expecting a start attribute element");
        }
    }

    protected void endAttributeElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT :
                if ((getCurrentQuery().toDo == STATE_INSIDE_EXECUTE_REPLACE) || (getCurrentQuery().toDo == STATE_INSIDE_EXECUTE_ADD)) {
                    getCurrentQuery().addAttrVal(current_value.toString());
                    current_state = getCurrentQuery().toDo;
                    break;
                }
                getCurrentQuery().addAttrList(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end attribute element");
        }
    }

    protected void startInitialContextElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_state = LDAPTransformer.STATE_INSIDE_INITIAL_CONTEXT_ELEMENT;
                current_value.setLength(0);
                break;
            case STATE_INSIDE_EXECUTE_QUERY :
            case STATE_INSIDE_EXECUTE_ADD :
            case STATE_INSIDE_EXECUTE_REPLACE :
                String name = null, value = null;

                if (attributes != null && attributes.getLength() > 0) {
                		name = attributes.getValue("name");
                		value = attributes.getValue("value");

                		if (name != null && value != null)
                			getCurrentQuery().addInitialContextValue(name, value);
                }
                if (name == null) {
                    this.getLogger().debug("Could not find 'name' attribute");
                    throwIllegalStateException("Could not find 'name' attribute in initial-context element");
                }
                if (value == null) {
                    this.getLogger().debug("Could not find 'value' attribute");
                    throwIllegalStateException("Could not find 'value' attribute in initial-context element");
                }

                current_state = LDAPTransformer.STATE_INSIDE_INITIAL_CONTEXT_ELEMENT;
                current_value.setLength(0);
                break;
            default :
                throwIllegalStateException("Not expecting a start initial-context element");
        }
    }

    protected void endInitialContextElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_INITIAL_CONTEXT_ELEMENT :
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end initial-context element");
        }
    }

    protected void endVersionElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_VERSION_ELEMENT :
                getCurrentQuery().version = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end version element");
        }
    }

    protected void startFilterElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
            case STATE_INSIDE_EXECUTE_INCREMENT :
            case STATE_INSIDE_EXECUTE_REPLACE :
            case STATE_INSIDE_EXECUTE_ADD :
                current_state = LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT;
                current_value.setLength(0);
                break;
            default :
                throwIllegalStateException("Not expecting a start filter element");
        }
    }

    protected void endFilterElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT :
                getCurrentQuery().filter = current_value.toString();
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end filter element");
        }
    }

    protected void endDerefLinkElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT :
                getCurrentQuery().deref_link = BooleanUtils.toBoolean(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end deref-link element");
        }
    }

    protected void endCountLimitElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_COUNT_LIMIT_ELEMENT :
                getCurrentQuery().count_limit = Integer.parseInt(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end count-limit element");
        }
    }

    protected void endTimeLimitElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_TIME_LIMIT_ELEMENT :
                getCurrentQuery().time_limit = Integer.parseInt(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end time-limit element");
        }
    }

    protected void endDebugElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT :
                getCurrentQuery().debug = BooleanUtils.toBoolean(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;
            default :
                throwIllegalStateException("Not expecting a end debug element");
        }
    }

    protected LDAPQuery getCurrentQuery() {
        return (LDAPQuery)queries.elementAt(current_query_index);
    }

    protected LDAPQuery getQuery(int i) {
        return (LDAPQuery)queries.elementAt(i);
    }

    /** END my very own methods */

    /** BEGIN SAX ContentHandler handlers */

    public void setDocumentLocator(Locator locator) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("PUBLIC ID: " + locator.getPublicId());
            getLogger().debug("SYSTEM ID: " + locator.getSystemId());
        }
        if (super.contentHandler != null)
            super.contentHandler.setDocumentLocator(locator);
    }

    public void startElement(String uri, String name, String raw, Attributes attributes) throws SAXException {
        if (uri == null || !uri.equals(my_uri)) {
            super.startElement(uri, name, raw, attributes);
            return;
        }
        getLogger().debug("RECEIVED START ELEMENT " + name + "(" + uri + ")");

        if (name.equals(LDAPTransformer.MAGIC_EXECUTE_QUERY)) {
            startExecuteElement();
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_INCREMENT)) {
            startExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_INITIALIZER_ELEMENT)) {
            startQueryParameterElement(LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_INITIAL_CONTEXT_ELEMENT)) {
            startInitialContextElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_AUTHENTICATION_ELEMENT)) {
            startQueryParameterElement(LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_SCOPE_ELEMENT)) {
            startQueryParameterElement(LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_VERSION_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_VERSION_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_SERVERURL_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_SERVERURL_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_PORT_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_PORT_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_DOC_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_DOC_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_ROW_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_ROW_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_DN_ATTRIBUTE)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_DN_ATTRIBUTE, name);
        } else if (name.equals(LDAPTransformer.MAGIC_ERROR_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_ERROR_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_SAX_ERROR)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_SAX_ERROR_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_ROOT_DN_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_ROOT_DN_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_PASSWORD_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_PASSWORD_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_ATTRIBUTE_ELEMENT)) {
            startAttributeElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SHOW_ATTRIBUTE_ELEMENT)) {
            startShowAttributeElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SEARCHBASE_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_SEARCHBASE_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_FILTER_ELEMENT)) {
            startFilterElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_DEREF_LINK_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_COUNT_LIMIT_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_COUNT_LIMIT_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_TIME_LIMIT_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_TIME_LIMIT_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_DEBUG_ELEMENT)) {
            startParameterElement(LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT, name);
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_REPLACE)) {
            startExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_REPLACE, LDAPTransformer.MAGIC_EXECUTE_REPLACE);
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_ADD)) {
            startExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_ADD, LDAPTransformer.MAGIC_EXECUTE_ADD);
        }
    }

    public void endElement(String uri, String name, String raw) throws SAXException {
        if (!uri.equals(my_uri)) {
            super.endElement(uri, name, raw);
            return;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("RECEIVED END ELEMENT " + name + "(" + uri + ")");
        }

        if (name.equals(LDAPTransformer.MAGIC_EXECUTE_QUERY)) {
            endExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY, LDAPTransformer.MAGIC_EXECUTE_QUERY);
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_INCREMENT)) {
            endExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT, LDAPTransformer.MAGIC_EXECUTE_INCREMENT);
        } else if (name.equals(LDAPTransformer.MAGIC_INITIALIZER_ELEMENT)) {
            endInitializerElement();
        } else if (name.equals(LDAPTransformer.MAGIC_INITIAL_CONTEXT_ELEMENT)) {
            endInitialContextElement();
        } else if (name.equals(LDAPTransformer.MAGIC_AUTHENTICATION_ELEMENT)) {
            endAuthenticationElement();
        } else if (name.equals(LDAPTransformer.MAGIC_SCOPE_ELEMENT)) {
            endScopeElement();
        } else if (name.equals(LDAPTransformer.MAGIC_VERSION_ELEMENT)) {
            endVersionElement();
        } else if (name.equals(LDAPTransformer.MAGIC_SERVERURL_ELEMENT)) {
            endServerurlElement();
        } else if (name.equals(LDAPTransformer.MAGIC_PORT_ELEMENT)) {
            endPortElement();
        } else if (name.equals(LDAPTransformer.MAGIC_DOC_ELEMENT)) {
            endDocElement();
        } else if (name.equals(LDAPTransformer.MAGIC_ROW_ELEMENT)) {
            endRowElement();
        } else if (name.equals(LDAPTransformer.MAGIC_DN_ATTRIBUTE)) {
            endDnAttribute();
        } else if (name.equals(LDAPTransformer.MAGIC_ERROR_ELEMENT)) {
            endErrorElement();
        } else if (name.equals(LDAPTransformer.MAGIC_SAX_ERROR)) {
            endSaxError();
        } else if (name.equals(LDAPTransformer.MAGIC_ROOT_DN_ELEMENT)) {
            endRootDnElement();
        } else if (name.equals(LDAPTransformer.MAGIC_PASSWORD_ELEMENT)) {
            endPasswordElement();
        } else if (name.equals(LDAPTransformer.MAGIC_ATTRIBUTE_ELEMENT)) {
            endAttributeElement();
        } else if (name.equals(LDAPTransformer.MAGIC_SHOW_ATTRIBUTE_ELEMENT)) {
            endShowAttributeElement();
        } else if (name.equals(LDAPTransformer.MAGIC_SEARCHBASE_ELEMENT)) {
            endSearchbaseElement();
        } else if (name.equals(LDAPTransformer.MAGIC_FILTER_ELEMENT)) {
            endFilterElement();
        } else if (name.equals(LDAPTransformer.MAGIC_DEREF_LINK_ELEMENT)) {
            endDerefLinkElement();
        } else if (name.equals(LDAPTransformer.MAGIC_COUNT_LIMIT_ELEMENT)) {
            endCountLimitElement();
        } else if (name.equals(LDAPTransformer.MAGIC_TIME_LIMIT_ELEMENT)) {
            endTimeLimitElement();
        } else if (name.equals(LDAPTransformer.MAGIC_DEBUG_ELEMENT)) {
            endDebugElement();
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_REPLACE)) {
            endExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_REPLACE, LDAPTransformer.MAGIC_EXECUTE_REPLACE);
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_ADD)) {
            endExecuteElement(LDAPTransformer.STATE_INSIDE_EXECUTE_ADD, LDAPTransformer.MAGIC_EXECUTE_ADD);
        }
    }

    public void characters(char ary[], int start, int length) throws SAXException {
        if (current_state != LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_INITIAL_CONTEXT_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_VERSION_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_SERVERURL_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_PORT_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_DOC_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_ROW_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_DN_ATTRIBUTE
            && current_state != LDAPTransformer.STATE_INSIDE_ERROR_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_SAX_ERROR_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_ROOT_DN_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_PASSWORD_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_SHOW_ATTRIBUTE_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_COUNT_LIMIT_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_TIME_LIMIT_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT
            && current_state != LDAPTransformer.STATE_INSIDE_SEARCHBASE_ELEMENT) {
            super.characters(ary, start, length);
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("RECEIVED CHARACTERS: " + new String(ary, start, length));
        }
        current_value.append(ary, start, length);
    }

    protected void start(String name, AttributesImpl attr) throws SAXException {
        super.contentHandler.startElement("", name, name, attr);
        attr.clear();
    }

    protected void end(String name) throws SAXException {
        super.contentHandler.endElement("", name, name);
    }

    protected void data(String data) throws SAXException {
        if (data != null) {
            super.contentHandler.characters(data.toCharArray(), 0, data.length());
        }
    }

    protected static String getStringValue(Object object) {
        return ObjectUtils.toString(object);
    }


    static class LDAPQuery {

        /** What index are you in daddy's queries list */
        protected int query_index;

        /** The current state of the event receiving FSM */
        protected int current_state;

        /** Who's your daddy? */
        protected LDAPTransformer transformer;

        /** LDAP configuration information */
        protected String initializer;
        protected String serverurl;
        protected int port;
        protected String root_dn;
        protected String password;
        protected String version;
        protected String scope;
        protected String authentication;
        private final static String LDAP_ENCODING = "ISO-8859-1"; 
        protected String encoding;

        /** LDAP environment information */
        protected Properties env = new Properties();
        protected DirContext ctx;

        /** LDAP Query */
        protected int toDo;
        protected String searchbase;
        protected List attrModeVal = new LinkedList();
        protected List attrListe = new LinkedList();
        protected List attrVale = new LinkedList();
        protected String REPLACE_MODE_DEFAULT = StringUtils.defaultString(LDAPTransformer.MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_DEFAULT);
        protected String REPLACE_MODE_APPEND = StringUtils.defaultString(LDAPTransformer.MAGIC_ATTRIBUTE_ELEMENT_MODE_ATTRIBUTE_VALUE_A);
        protected Map initialContextValues = new HashMap();
        protected boolean showAttribute;
        protected String filter;
        protected String doc_element;
        protected String exec_element = "exec-element";
        protected String row_element;
        protected String dn_attribute;
        protected String error_element;
        protected boolean sax_error;
        protected boolean deref_link; // Dereference: true -> dereference the link during search
        protected long count_limit;   // Maximum number of entries to return: 0 -> no limit
        protected int time_limit;     // Number of milliseconds to wait before return: 0 -> infinite
        protected boolean debug; 

        protected LDAPQuery(LDAPTransformer transformer) {
            this.transformer = transformer;
            initializer = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_INITIALIZER_ELEMENT, "com.sun.jndi.ldap.LdapCtxFactory");
            serverurl = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_SERVERURL_ELEMENT, "localhost");
            port = MapUtils.getIntValue(transformer.default_properties, LDAPTransformer.MAGIC_PORT_ELEMENT, 389);
            root_dn = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_ROOT_DN_ELEMENT, "");
            password = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_PASSWORD_ELEMENT, "");
            version = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_VERSION_ELEMENT, "2");
            scope = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_SCOPE_ELEMENT, "ONELEVEL_SCOPE");
            authentication = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_AUTHENTICATION_ELEMENT, "simple");
            searchbase = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_SEARCHBASE_ELEMENT, "");
            showAttribute = MapUtils.getBooleanValue(transformer.default_properties, LDAPTransformer.MAGIC_SHOW_ATTRIBUTE_ELEMENT, true);
            doc_element = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_DOC_ELEMENT, "doc-element");
            row_element = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_ROW_ELEMENT, "row-element");
            dn_attribute = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_DN_ATTRIBUTE, "");
            error_element = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_ERROR_ELEMENT, "ldap-error");
            sax_error = MapUtils.getBooleanValue(transformer.default_properties, LDAPTransformer.MAGIC_SAX_ERROR);
            deref_link = MapUtils.getBooleanValue(transformer.default_properties, LDAPTransformer.MAGIC_DEREF_LINK_ELEMENT);
            count_limit = MapUtils.getLongValue(transformer.default_properties, LDAPTransformer.MAGIC_DEREF_LINK_ELEMENT);
            time_limit = MapUtils.getIntValue(transformer.default_properties, LDAPTransformer.MAGIC_TIME_LIMIT_ELEMENT);
            debug = MapUtils.getBooleanValue(transformer.default_properties, LDAPTransformer.MAGIC_DEBUG_ELEMENT);
            encoding = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_ENCODING_ELEMENT, LDAP_ENCODING);
            filter = MapUtils.getString(transformer.default_properties, LDAPTransformer.MAGIC_FILTER_ELEMENT, "");
        }

        /** shared constraints initialization */
        protected void initConstraints(SearchControls constraints) {
            if (scope.equals("OBJECT_SCOPE")) {
                constraints.setSearchScope(SearchControls.OBJECT_SCOPE);
            } else if (scope.equals("SUBTREE_SCOPE")) {
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            } else {
                constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            }
            constraints.setTimeLimit(time_limit);
            constraints.setDerefLinkFlag(deref_link);
            constraints.setCountLimit(count_limit);
        }

        protected void execute() throws Exception {
            String[] attrList = new String[attrListe.size()];

            AttributesImpl attr = new AttributesImpl();
            if (debug) {
                debugPrint();
            }
            SearchControls constraints = new SearchControls();
            attrListe.toArray(attrList);
            attrListe.clear();
            try {
                connect();
                switch (toDo) {
                    case LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY :
                        try {
                            initConstraints(constraints);
                            if (attrList.length > 0) {
                                constraints.setReturningAttributes(attrList);
                            }

                            if (!filter.equals("")) {
                                //filter is present
                                if (!doc_element.equals("")) {
                                    transformer.start(doc_element, attr);
                                }
                                NamingEnumeration ldapresults = ctx.search(searchbase, filter, constraints);

                                while (ldapresults != null && ldapresults.hasMore()) {
                                    SearchResult si = (SearchResult) ldapresults.next();
                                    if (!row_element.equals("")) {
                                        if(!"".equals(dn_attribute)) {
                                            String dn;
                                            if(!"".equals(searchbase))
                                                dn = si.getName() + ',' + searchbase;
                                            else
                                                dn = si.getName();
                                            attr.addAttribute(null, dn_attribute, dn_attribute, "CDATA", dn);
                                        }
                                        transformer.start(row_element, attr);
                                    }
                                    javax.naming.directory.Attributes attrs = si.getAttributes();
                                    if (attrs != null) {
                                        NamingEnumeration ae = attrs.getAll();
                                        while (ae.hasMoreElements()) {
                                            Attribute at = (Attribute) ae.next();
                                            Enumeration vals = at.getAll();
                                            String attrID = at.getID();
                                            while (vals.hasMoreElements()) {
                                                if (showAttribute) {
                                                    transformer.start(attrID, attr);
                                                }
                                                String attrVal = recodeFromLDAPEncoding((String) vals.nextElement());
                                                if (query_index > 0) {
                                                    switch (transformer.getQuery(query_index - 1).current_state) {
                                                        case LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT :
                                                            StringBuffer temp =  new StringBuffer(transformer.getQuery(query_index - 1).filter);
                                                            if (temp.length() > 0) {
                                                                temp.append(", ");
                                                            }
                                                            temp.append(attrID).append("=").append(attrVal);
                                                            transformer.getQuery(query_index - 1).filter = temp.toString();
                                                            break;
                                                        default :
                                                            transformer.start(attrID, attr);
                                                    }
                                                } else {
                                                    transformer.data(String.valueOf(attrVal));
                                                }
                                                if (showAttribute) {
                                                    transformer.end(attrID);
                                                }
                                            }
                                        }
                                    }
                                    if (!row_element.equals("")) {
                                        transformer.end(row_element);
                                    }
                                }
                                if (!doc_element.equals("")) {
                                    transformer.end(doc_element);
                                }
                            } else {
                                //filter not present, get the values from absolete path
                                javax.naming.directory.Attributes attrs = ctx.getAttributes(searchbase, attrList);
                                if (!doc_element.equals("")) {
                                    transformer.start(doc_element, attr);
                                }
                                if (!row_element.equals("")) {
                                    if(!"".equals(dn_attribute)) {
                                        attr.addAttribute(null, dn_attribute, dn_attribute, "CDATA", searchbase);
                                    }
                                    transformer.start(row_element, attr);
                                }
                                if (attrs != null) {
                                    NamingEnumeration ae = attrs.getAll();
                                    while (ae.hasMoreElements()) {
                                        Attribute at = (Attribute) ae.next();
                                        Enumeration vals = at.getAll();
                                        String attrID = at.getID();
                                        while (vals.hasMoreElements()) {
                                            if (showAttribute) {
                                                transformer.start(attrID, attr);
                                            }
                                            String attrVal = recodeFromLDAPEncoding((String)vals.nextElement());

                                            if (query_index > 0) {
                                                switch (transformer.getQuery(query_index - 1).current_state) {
                                                    case LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT :
                                                        StringBuffer temp = new StringBuffer(transformer.getQuery(query_index - 1).filter);
                                                        if (temp.length() > 0) {
                                                            temp.append(", ");
                                                        }
                                                        temp.append(attrID).append("=").append(attrVal);
                                                        transformer.getQuery(query_index - 1).filter = temp.toString();
                                                        break;
                                                    default :
                                                        transformer.start(attrID, attr);
                                                }
                                            } else {
                                                transformer.data(String.valueOf(attrVal));
                                            }
                                            if (showAttribute) {
                                                transformer.end(attrID);
                                            }
                                        }
                                    }
                                }
                                if (!row_element.equals("")) {
                                    transformer.end(row_element);
                                }
                                if (!doc_element.equals("")) {
                                    transformer.end(doc_element);
                                }
                            }
                        } catch (Exception e) {
                            if (sax_error) {
                                throw new Exception("[LDAPTransformer] Error in LDAP-Query: " + e.toString());
                            } else {
                                transformer.start(error_element, attr);
                                transformer.data("[LDAPTransformer] Error in LDAP-Query: " + e);
                                transformer.end(error_element);
                                transformer.getLogger().error("[LDAPTransformer] Exception: " + e.toString());
                            }
                        }
                        break;
                    case LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT :
                        try {
                            initConstraints(constraints);
                            if (attrList.length != 1) {
                                transformer.start(error_element, attr);
                                transformer.data("Increment must reference exactly 1 attribute.");
                                transformer.end(error_element);
                            } else {
                                constraints.setReturningAttributes(attrList);
                                NamingEnumeration ldapresults = ctx.search(searchbase, filter, constraints);
                                int attrVal = 0;
                                String attrID = "";
                                SearchResult si = null;
                                while (ldapresults != null && ldapresults.hasMore()) {
                                    si = (SearchResult) ldapresults.next();
                                    javax.naming.directory.Attributes attrs = si.getAttributes();
                                    if (attrs != null) {
                                        NamingEnumeration ae = attrs.getAll();
                                        while (ae.hasMoreElements()) {
                                            Attribute at = (Attribute) ae.next();
                                            Enumeration vals = at.getAll();
                                            attrID = at.getID();
                                            attrVal = Integer.parseInt((String) vals.nextElement());
                                        }
                                    }
                                }
                                ++attrVal;
                                // Specify the changes to make
                                ModificationItem[] mods = new ModificationItem[1];
                                // Replace the "mail" attribute with a new value
                                mods[0] =
                                    new ModificationItem(
                                        DirContext.REPLACE_ATTRIBUTE,
                                        new BasicAttribute(attrID, Integer.toString(attrVal)));
                                // Perform the requested modifications on the named object
                                ctx.modifyAttributes(
                                    new StringBuffer(si.toString().substring(0, si.toString().indexOf(":")))
                                        .append(",")
                                        .append(searchbase)
                                        .toString(),
                                    mods);
                            }
                        } catch (Exception e) {
                            if (sax_error) {
                                throw new Exception("[LDAPTransformer] Error incrementing an attribute: " + e.toString());
                            } else {
                                transformer.start(error_element, attr);
                                transformer.data("[LDAPTransformer] Error incrementing an attribute: " + e.toString());
                                transformer.end(error_element);
                                transformer.getLogger().error("[LDAPTransformer] Error incrementing an attribute: " + e.toString());
                            }
                        }
                        break;
                    /* execute modes */
                    case LDAPTransformer.STATE_INSIDE_EXECUTE_REPLACE :
                        try {
                            String[] attrVal = new String[attrVale.size()];
                            String[] attrMode = new String[attrModeVal.size()];
                            String replaceMode;
                            attrVale.toArray(attrVal);
                            attrVale.clear();
                            attrModeVal.toArray(attrMode);
                            attrModeVal.clear();

                            if (attrVal.length != attrList.length) {
                                transformer.start(error_element, attr);
                                transformer.data("Attribute values must have the some number as a names");
                                transformer.end(error_element);
                                break;
                            }
                            HashMap attrMap = new HashMap(attrVal.length);
                            HashMap attrModeMap = new HashMap(attrMode.length);

                            for (int i = 0; i < attrVal.length; i++) {
                                attrMap.put(attrList[i], attrVal[i]);
                                attrModeMap.put(attrList[i], attrMode[i]);
                            }

                            initConstraints(constraints);
                            if (attrList.length < 1) {
                                transformer.start(error_element, attr);
                                transformer.data("Modify must reference 1 or more attribute.");
                                transformer.end(error_element);
                            } else {
                                if (!filter.equals("")) {
                                    constraints.setReturningAttributes(attrList);
                                    NamingEnumeration ldapresults = ctx.search(searchbase, filter, constraints);
                                    SearchResult si;
                                    /* start indicate element of executing query */
                                    if (!exec_element.equals("")) {
                                        transformer.start(exec_element, attr);
                                    }
                                    while (ldapresults != null && ldapresults.hasMore()) {
                                        if (!row_element.equals("")) {
                                            transformer.start(row_element, attr);
                                        }

                                        si = (SearchResult) ldapresults.next();
                                        javax.naming.directory.Attributes attrs = si.getAttributes();
                                        if (attrs != null) {
                                            NamingEnumeration ae = attrs.getAll();
                                            while (ae.hasMoreElements()) {
                                                Attribute at = (Attribute) ae.next();
                                                Enumeration vals = at.getAll();
                                                String attrID = at.getID();
                                                ModificationItem[] mods = new ModificationItem[1];
                                                replaceMode = (String) attrModeMap.get(attrID);

                                                String attrValue = recodeFromLDAPEncoding((String) vals.nextElement());
                                                String newAttrValue = "";
                                                /* Check the replacing method */
                                                if (replaceMode.equals(REPLACE_MODE_DEFAULT)) {
                                                    newAttrValue = (String)attrMap.get(attrID);
                                                } else if (replaceMode.equals(REPLACE_MODE_APPEND)) {
                                                    newAttrValue = attrValue + attrMap.get(attrID);
                                                }
                                                newAttrValue = recodeToLDAPEncoding(newAttrValue);

                                                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                                                               new BasicAttribute(attrID,newAttrValue));

                                                // Perform the requested modifications on the named object
                                                ctx.modifyAttributes(
                                                        si.toString().substring(0, si.toString().indexOf(":")) + "," + searchbase,
                                                        mods);

                                                /* confirm of success */
                                                transformer.start(attrID, attr);
                                                transformer.data("replaced");
                                                transformer.end(attrID);
                                            }
                                        }

                                        if (!row_element.equals("")) {
                                            transformer.end(row_element);
                                        }

                                    }
                                    if (!exec_element.equals("")) {
                                        transformer.end(exec_element);
                                    }
                                } else {
                                    //filter is not present
                                    javax.naming.directory.Attributes attrs = ctx.getAttributes(searchbase, attrList);
                                    /* start indicate element of executing query */
                                    if (!exec_element.equals("")) {
                                        transformer.start(exec_element, attr);
                                    }
                                    if (!row_element.equals("")) {
                                        transformer.start(row_element, attr);
                                    }
                                    if (attrs != null) {
                                        NamingEnumeration ae = attrs.getAll();
                                        while (ae.hasMoreElements()) {
                                            Attribute at = (Attribute) ae.next();
                                            Enumeration vals = at.getAll();
                                            String attrID = at.getID();
                                            ModificationItem[] mods = new ModificationItem[1];
                                            replaceMode = (String) attrModeMap.get(attrID);

                                            String attrValue = recodeFromLDAPEncoding((String) vals.nextElement());

                                            String newAttrValue = "";
                                            /* Check the replacing method */
                                            if (replaceMode.equals(REPLACE_MODE_DEFAULT)) {
                                                newAttrValue = (String) attrMap.get(attrID);
                                            } else if (replaceMode.equals(REPLACE_MODE_APPEND)) {
                                                newAttrValue = attrValue + attrMap.get(attrID);
                                            }
                                            newAttrValue = recodeToLDAPEncoding(newAttrValue);

                                            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                                                           new BasicAttribute(attrID, newAttrValue));

                                            // Perform the requested modifications on the named object
                                            ctx.modifyAttributes(searchbase, mods);

                                            /* confirm of success */
                                            transformer.start(attrID, attr);
                                            transformer.data("replaced");
                                            transformer.end(attrID);
                                        }
                                    }

                                    if (!row_element.equals("")) {
                                        transformer.end(row_element);
                                    }

                                    /* end indicate element of executing query */
                                    if (!exec_element.equals("")) {
                                        transformer.end(exec_element);
                                    }
                                }
                            }

                        } catch (Exception e) {
                            if (sax_error) {
                                throw new Exception("[LDAPTransformer] Error replacing an attribute: " + e.toString());
                            } else {
                                transformer.start(error_element, attr);
                                transformer.data("[LDAPTransformer] Error replacing an attribute: " + e.toString());
                                transformer.end(error_element);
                                transformer.getLogger().error("[LDAPTransformer] Error replacing an attribute: " + e.toString());
                                if (!row_element.equals("")) {
                                    transformer.end(row_element);
                                }
                                if (!exec_element.equals("")) {
                                    transformer.end(exec_element);
                                }
                            }
                        }
                        break;
                    case LDAPTransformer.STATE_INSIDE_EXECUTE_ADD :
                        try {
                            String[] attrVal = new String[attrVale.size()];
                            attrVale.toArray(attrVal);
                            attrVale.clear();
                            if (attrVal.length != attrList.length) {
                                transformer.start(error_element, attr);
                                transformer.data("Attribute values must have the some number as a names");
                                transformer.end(error_element);
                                break;
                            }
                            HashMap attrMap = new HashMap(attrVal.length);

                            for (int i = 0; i < attrVal.length; i++)
                                attrMap.put(attrList[i], attrVal[i]);

                            initConstraints(constraints);
                            if (attrList.length < 1) {
                                transformer.start(error_element, attr);
                                transformer.data("Modify must reference 1 or more attribute.");
                                transformer.end(error_element);
                            } else {
                                if (!filter.equals("")) {
                                    constraints.setReturningAttributes(attrList);
                                    NamingEnumeration ldapresults = ctx.search(searchbase, filter, constraints);
                                    SearchResult si;
                                    /* start indicate element of executing query */
                                    if (!exec_element.equals("")) {
                                        transformer.start(exec_element, attr);
                                    }
                                    while (ldapresults != null && ldapresults.hasMore()) {
                                        if (!row_element.equals("")) {
                                            transformer.start(row_element, attr);
                                        }

                                        si = (SearchResult) ldapresults.next();
                                        javax.naming.directory.Attributes attrs = si.getAttributes();
                                        if (attrs != null) {
                                            /* Replace the attribute if attribute already exist */
                                            NamingEnumeration ae = attrs.getAll();
                                            while (ae.hasMoreElements()) {
                                                Attribute at = (Attribute) ae.next();
                                                String attrID = at.getID();
                                                // Specify the changes to make
                                                ModificationItem[] mods = new ModificationItem[1];

                                                String attrValue = recodeToLDAPEncoding((String)attrMap.get(attrID));
                                                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                                                               new BasicAttribute(attrID, attrValue));
                                                // Perform the requested modifications on the named object
                                                ctx.modifyAttributes(
                                                    new StringBuffer(si.toString().substring(0, si.toString().indexOf(":")))
                                                        .append(",")
                                                        .append(searchbase)
                                                        .toString(),
                                                    mods);

                                                /* confirm of success */
                                                transformer.start(attrID, attr);
                                                transformer.data("replaced");
                                                transformer.end(attrID);
                                                /* Remove the attribute from map after replacing */
                                                attrMap.remove(attrID);
                                            }
                                        }
                                        /* Add the attributes */
                                        if (!attrMap.isEmpty()) {
                                            ModificationItem[] mods = new ModificationItem[1];
                                            for (int i = 0; i < attrList.length; i++) {
                                                if (attrMap.containsKey(attrList[i])) {
                                                    String attrValue = recodeToLDAPEncoding((String)attrMap.get(attrList[i]));
                                                    mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                                                                                   new BasicAttribute(attrList[i], attrValue));
                                                    // Perform the requested modifications on the named object
                                                    ctx.modifyAttributes(
                                                        new StringBuffer(si.toString().substring(0, si.toString().indexOf(":")))
                                                            .append(",")
                                                            .append(searchbase)
                                                            .toString(),
                                                        mods);

                                                    /* confirm of success */
                                                    transformer.start(attrList[i], attr);
                                                    transformer.data("add");
                                                    transformer.end(attrList[i]);
                                                }
                                            }
                                        }
                                        if (!row_element.equals("")) {
                                            transformer.end(row_element);
                                        }
                                    }
                                    if (!exec_element.equals("")) {
                                        transformer.end(exec_element);
                                    }
                                } else {
                                    //filter is not present
                                    javax.naming.directory.Attributes attrs = ctx.getAttributes(searchbase, attrList);
                                    /* start indicate element of executing query */
                                    if (!exec_element.equals("")) {
                                        transformer.start(exec_element, attr);
                                    }
                                    if (!row_element.equals("")) {
                                        transformer.start(row_element, attr);
                                    }

                                    if (attrs != null) {
                                        NamingEnumeration ae = attrs.getAll();
                                        while (ae.hasMoreElements()) {
                                            Attribute at = (Attribute) ae.next();
                                            String attrID = at.getID();
                                            // Specify the changes to make
                                            ModificationItem[] mods = new ModificationItem[1];

                                            String attrValue = recodeToLDAPEncoding((String)attrMap.get(attrID));
                                            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                                                           new BasicAttribute(attrID,  attrValue));
                                            // Perform the requested modifications on the named object
                                            ctx.modifyAttributes(searchbase, mods);

                                            /* confirm of success */
                                            transformer.start(attrID, attr);
                                            transformer.data("replaced");
                                            transformer.end(attrID);
                                            /* Remove the attribute from map after replacing */
                                            attrMap.remove(attrID);
                                        }
                                    }
                                    /* Add the attributes */
                                    if (!attrMap.isEmpty()) {
                                        ModificationItem[] mods = new ModificationItem[1];
                                        for (int i = 0; i < attrList.length; i++) {
                                            if (attrMap.containsKey(attrList[i])) {
                                                String attrValue = recodeToLDAPEncoding((String)attrMap.get(attrList[i]));
                                                mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                                                                               new BasicAttribute(attrList[i], attrValue));
                                                // Perform the requested modifications on the named object
                                                ctx.modifyAttributes(searchbase, mods);
                                                /* confirm of success */
                                                transformer.start(attrList[i], attr);
                                                transformer.data("add");
                                                transformer.end(attrList[i]);
                                            }
                                        }
                                    }
                                    if (!row_element.equals("")) {
                                        transformer.end(row_element);
                                    }
                                    /* end indicate element of executing query */
                                    if (!exec_element.equals("")) {
                                        transformer.end(exec_element);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            if (sax_error) {
                                throw new Exception("[LDAPTransformer] Error replacing an attribute: " + e.toString());
                            } else {
                                transformer.start(error_element, attr);
                                transformer.data("[LDAPTransformer] Error replacing an attribute: " + e.toString());
                                transformer.end(error_element);
                                transformer.getLogger().error("[LDAPTransformer] Error replacing an attribute: " + e.toString());
                                if (!row_element.equals("")) {
                                    transformer.end(row_element);
                                }
                                if (!exec_element.equals("")) {
                                    transformer.end(exec_element);
                                }
                            }
                        }
                        break;
                    default :
                } //end switch
            } catch (NamingException e) {
                if (sax_error) {
                    throw new NamingException("[LDAPTransformer] Failed ldap-connection to directory service: " + e.toString());
                } else {
                    transformer.start(error_element, attr);
                    transformer.data("[LDAPTransformer] Failed ldap-connection to directory service.");
                    transformer.end(error_element);
                    transformer.getLogger().error("[LDAPTransformer] Failed to connect to " + serverurl + e.toString());
                }
            }
            try {
                disconnect();
            } catch (NamingException e) {
                if (sax_error) {
                    throw new NamingException("[LDAPTransformer] Failed ldap-disconnection from directory service: " + e.toString());
                } else {
                    transformer.start(error_element, attr);
                    transformer.data("[LDAPTransformer] Failed ldap-disconnection to directory service.");
                    transformer.end(error_element);
                    transformer.getLogger().error("[LDAPTransformer] Failed to disconnect from " + serverurl + e.toString());
                }
            }
        }

        protected void addAttrList(String attr) {
            attrListe.add(attr);
        }

        protected void addAttrModeVal(String mode) {
            attrModeVal.add(mode);
        }

        protected void addAttrVal(String val) {
            attrVale.add(val);
        }

        protected void addInitialContextValue(String name, String value)
        {
            initialContextValues.put(name, value);
        }

        protected void connect() throws NamingException {
            if (root_dn != null && password != null) {
                env.put(Context.SECURITY_AUTHENTICATION, authentication);
                env.put(Context.SECURITY_PRINCIPAL, root_dn);
                env.put(Context.SECURITY_CREDENTIALS, password);
            }

            env.put("java.naming.ldap.version", version);
            env.put(Context.INITIAL_CONTEXT_FACTORY, initializer);
            env.put(Context.PROVIDER_URL, serverurl + ":" + port);

            // Override existing properties or add new properties with values from initial-context
            env.putAll(initialContextValues);

            try {
                ctx = new InitialDirContext(env);
            } catch (NamingException e) {
                env.clear();
                throw new NamingException(e.toString());
            }
        }

        protected void disconnect() throws NamingException {
            try {
                if (ctx != null)
                    ctx.close();
            } catch (NamingException e) {
                ctx = null;
                env.clear();
                throw new NamingException(e.toString());
            }
            ctx = null;
            env.clear();
        }

        protected void debugPrint() {
            Log logger = transformer.getLogger();
            if (logger.isDebugEnabled()) {
                logger.debug("query_index: " + query_index);
                logger.debug("current_state: " + current_state);
                logger.debug("serverurl: " + serverurl);
                logger.debug("port: " + port);
                logger.debug("root_dn: " + root_dn);
                logger.debug("password: " + password);
                logger.debug("version: " + version);
                logger.debug("scope: " + scope);
                logger.debug("authentication: " + authentication);
                logger.debug("toDo: " + toDo);
                logger.debug("searchbase: " + searchbase);
                logger.debug("showAttribute: " + showAttribute);
                logger.debug("attribute: " + attrListe.toString());
                logger.debug("initial-context: " + initialContextValues);
                logger.debug("filter: " + filter);
                logger.debug("doc_element: " + doc_element);
                logger.debug("row_element: " + row_element);
                logger.debug("error_element: " + error_element);
                logger.debug("sax-error: " + sax_error);
                logger.debug("deref_link: " + deref_link);
                logger.debug("count_limit: " + count_limit);
                logger.debug("time_limit: " + time_limit);
            }
        }

        /**
         * Recodes a String value from {@link #LDAP_ENCODING} to specified {@link #encoding}.
         * @param value the String to recode
         * @return the recoded String
         * @throws UnsupportedEncodingException if either the used encoding
         */        
        private String recodeFromLDAPEncoding(String value) throws UnsupportedEncodingException {
            if (!LDAP_ENCODING.equals(encoding)) {
                value = new String(value.getBytes(LDAP_ENCODING), encoding);
            }
            return value;
        }

        /**
         * Recodes a String value from specified {@link #encoding} to {@link #LDAP_ENCODING}.
         * @param value the String to recode
         * @return the recoded String
         * @throws UnsupportedEncodingException if either the used encoding
         */        
        private String recodeToLDAPEncoding(String value) throws UnsupportedEncodingException {
            if (!LDAP_ENCODING.equals(encoding)) {
                value = new String(value.getBytes(encoding), LDAP_ENCODING);
            }
            return value;
        }
    }
}
