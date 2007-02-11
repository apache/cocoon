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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.avalon.framework.logger.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.IOException;
import java.util.*;

/**
 * The <code>LDAPTransformer</code> is a class that can be plugged into a pipeline
 * to transform the SAX events which passes thru this transformer into queries
 * an responses to/from a ldap interface.
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
 * &lt;!ELEMENT execute-query (attribute+ | show-attribute? | scope? | 
 *              initializer? | authentication? | error-element? | sax-error?  
 *              doc-element? | row-element? | version? | serverurl? | rootdn? | 
 *              password? | deref-link? | count-limit? | searchbase, 
 *              filter)&gt;<br>
 * &lt;!ELEMENT execute-increment (attribute | show-attribute? | scope? | 
 *                                 initializer? | authentication? | 
 *                                 error-element? | sax-error? | doc-element? | 
 *                                 row-element? | version? | serverurl? | 
 *                                 rootdn? | password? | deref-link? | 
 *                                 count-limit? | searchbase, filter)&gt;<br>
 * increments (+1) an integer attribute on a directory-server (ldap)<br>
 * <br>
 * &lt;!ELEMENT initializer (#PCDATA)&gt;+ (default: "com.sun.jndi.ldap.LdapCtxFactory")<br>
 * &lt;!ELEMENT authentication (#PCDATA)&gt;+ (default: "simple")<br>
 * &lt;!ELEMENT version (#PCDATA)&gt;+ (default: "2")<br>
 * &lt;!ELEMENT serverurl (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT port (#PCDATA)&gt;+ (default: 389)<br>
 * &lt;!ELEMENT rootdn (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT password (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT scope (ONELEVEL_SCOPE | SUBTREE_SCOPE | OBJECT_SCOPE)&gt;+ 
 *                    (default: ONELEVEL_SCOPE)<br>
 * &lt;!ELEMENT searchbase (#PCDATA)&gt;+<br>
 * &lt;!ELEMENT doc-element (#PCDATA)&gt;+ (default: "doc-element")<br>
 * &lt;!ELEMENT row-element (#PCDATA)&gt;+ (default: "row-element")<br>
 * &lt;!ELEMENT error-element (#PCDATA)&gt;+ (default: "ldap-error") 
 *                            (in case of error returned error tag)<br>
 * &lt;!ELEMENT sax_error (TRUE  | FALSE)&gt;+; (default: FALSE) 
 *                        (throws SAX-Exception instead of error tag)<br>
 * &lt;!ELEMENT attribute (#PCDATA)&gt;<br>
 * &lt;!ELEMENT show-attribute (TRUE | FALSE)&gt; (default: TRUE)<br>
 * &lt;!ELEMENT filter (#PCDATA | execute-query)&gt;<br>
 * &lt;!ELEMENT deref-link (TRUE | FALSE)&gt; (default: FALSE)<br>
 * &lt;!ELEMENT count-limit (#PCDATA)&gt; (integer default: 0 -&gt; no limit)<br>
 * &lt;!ELEMENT time-limit (#PCDATA)&gt; (integer default: 0 -&gt; infinite)<br>
 * &lt;!ELEMENT debug (TRUE  | FALSE)&gt;+; (default: FALSE)<br>
 * <br>
 * + can also be defined as parameter in the sitemap.
 * <br>
 * <br>
 *
 * @author Felix Knecht
 * @version CVS $Id: LDAPTransformer.java,v 1.4 2003/05/16 07:20:41 cziegeler Exp $
 */
public class LDAPTransformer extends AbstractTransformer {

    /** The LDAP namespace ("http://apache.org/cocoon/LDAP/1.0") */
    public static final String my_uri = "http://apache.org/cocoon/LDAP/1.0";

    public static final String my_name = "LDAPTransformer";

    /** The LDAP namespace element names **/
    public static final String MAGIC_EXECUTE_QUERY = "execute-query";
    public static final String MAGIC_EXECUTE_INCREMENT = "execute-increment";
    public static final String MAGIC_INITIALIZER_ELEMENT = "initializer";
    public static final String MAGIC_DOC_ELEMENT = "doc-element";
    public static final String MAGIC_ROW_ELEMENT = "row-element";
    public static final String MAGIC_ERROR_ELEMENT = "error-element";
    public static final String MAGIC_SAX_ERROR = "sax-error";
    public static final String MAGIC_ATTRIBUTE_ELEMENT = "attribute";
    public static final String MAGIC_SERVERURL_ELEMENT = "serverurl";
    public static final String MAGIC_PORT_ELEMENT = "port";
    public static final String MAGIC_SEARCHBASE_ELEMENT = "searchbase";
    public static final String MAGIC_FILTER_ELEMENT = "filter";
    public static final String MAGIC_ROOT_DN_ELEMENT = "rootdn";
    public static final String MAGIC_PASSWORD_ELEMENT = "password";
    public static final String MAGIC_SHOW_ATTRIBUTE_ELEMENT = "show-attribute";
    public static final String MAGIC_SCOPE_ELEMENT = "scope";
    public static final String MAGIC_VERSION_ELEMENT = "version";
    public static final String MAGIC_AUTHENTICATION_ELEMENT = "authentication";
    public static final String MAGIC_DEREF_LINK_ELEMENT = "deref-link";
    public static final String MAGIC_COUNT_LIMIT_ELEMENT = "count-limit";
    public static final String MAGIC_TIME_LIMIT_ELEMENT = "time-limit";
    public static final String MAGIC_DEBUG_ELEMENT = "debug";
    public static final String MAGIC_ENCODING_ELEMENT = "encoding";

    /** The states we are allowed to be in **/
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

    /** Default parameters that might apply to all queries */
    protected Properties default_properties = new Properties();

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

    /* BEGIN SitemapComponent methods */
    public void setup(SourceResolver resolver, Map objectModel,
                      String source,
                      Parameters parameters)
                        throws ProcessingException, SAXException,
                               IOException {
        current_state = STATE_OUTSIDE;

        // Check the initializer
        String parameter = parameters.getParameter(MAGIC_INITIALIZER_ELEMENT,
                                                   null);

        if (parameter!=null) {
            default_properties.setProperty(MAGIC_INITIALIZER_ELEMENT,
                                           parameter);
        }
        // Check the version
        parameter = parameters.getParameter(MAGIC_VERSION_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_VERSION_ELEMENT, parameter);
        }
        // Check the authentication
        parameter = parameters.getParameter(MAGIC_AUTHENTICATION_ELEMENT,
                                            null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_AUTHENTICATION_ELEMENT,
                                           parameter);
        }
        // Check the scope
        parameter = parameters.getParameter(MAGIC_SCOPE_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_SCOPE_ELEMENT, parameter);
        }
        // Check the serverurl
        parameter = parameters.getParameter(MAGIC_SERVERURL_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_SERVERURL_ELEMENT,
                                           parameter);
        }
        // Check the ldap-root_dn
        parameter = parameters.getParameter(MAGIC_ROOT_DN_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_ROOT_DN_ELEMENT, parameter);
        }
        // Check the ldap-pwd
        parameter = parameters.getParameter(MAGIC_PASSWORD_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_PASSWORD_ELEMENT, parameter);
        }
        // Check the port
        parameter = parameters.getParameter(MAGIC_PORT_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_PORT_ELEMENT, parameter);
        }
        // Check the searchbase
        parameter = parameters.getParameter(MAGIC_SEARCHBASE_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_SEARCHBASE_ELEMENT,
                                           parameter);
        }
        // Check the doc-element
        parameter = parameters.getParameter(MAGIC_DOC_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_DOC_ELEMENT, parameter);
        }
        // Check the row-element
        parameter = parameters.getParameter(MAGIC_ROW_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_ROW_ELEMENT, parameter);
        }
        // Check the error-element
        parameter = parameters.getParameter(MAGIC_ERROR_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_ERROR_ELEMENT, parameter);
        }
        // Check the sax-error
        parameter = parameters.getParameter(MAGIC_SAX_ERROR, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_SAX_ERROR, parameter);
        }
        // Check the deref-link-element
        parameter = parameters.getParameter(MAGIC_DEREF_LINK_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_DEREF_LINK_ELEMENT,
                                           parameter.toUpperCase());
        }
        // Check the count-limit-element
        parameter = parameters.getParameter(MAGIC_COUNT_LIMIT_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_COUNT_LIMIT_ELEMENT,
                                           parameter);
        }
        // Check the time-limit-element
        parameter = parameters.getParameter(MAGIC_TIME_LIMIT_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_TIME_LIMIT_ELEMENT,
                                           parameter);
        }
        // Check the debug-element
        parameter = parameters.getParameter(MAGIC_DEBUG_ELEMENT, null);
        if (parameter!=null) {
            default_properties.setProperty(MAGIC_DEBUG_ELEMENT, parameter);
        }
        // Check the encoding
        parameter = parameters.getParameter(MAGIC_ENCODING_ELEMENT, null);
        if (parameter!=null) {
            getLogger().debug("Get the sitemap parameter "+
                              MAGIC_ENCODING_ELEMENT+"("+parameter+")");
            default_properties.setProperty(MAGIC_ENCODING_ELEMENT, parameter);
        }

    }

    /* END SitemapComponent methods */

    /* BEGIN my very own methods */

    /**
     * This will be the meat of LDAPTransformer, where the query is run.
     */
    protected void executeQuery(int index) throws SAXException {
        this.contentHandler.startPrefixMapping("", LDAPTransformer.my_uri);
        LDAPQuery query = (LDAPQuery) queries.elementAt(index);

        try {
            query.execute();
        } catch (NamingException e) {
            throw new SAXException(e);
        } catch (Exception e) {
            throw new SAXException(e);
        }

        this.contentHandler.endPrefixMapping("");
    }

    protected static void throwIllegalStateException(String message) {
        throw new IllegalStateException(my_name+": "+message);
    }

    protected void startExecuteQuery(Attributes attributes) {
        LDAPQuery query;

        switch (current_state) {
            case LDAPTransformer.STATE_OUTSIDE :
                current_state = LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY;
                current_query_index = queries.size();
                query = new LDAPQuery(this);
                queries.addElement(query);
                getCurrentQuery().toDo = LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY;
                getCurrentQuery().query_index = current_query_index;
                break;

            case LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT :
                current_state = LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY;
                current_query_index = queries.size();
                query = new LDAPQuery(this);
                queries.addElement(query);
                getCurrentQuery().toDo = LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY;
                getCurrentQuery().query_index = current_query_index;
                break;

            default :
                throwIllegalStateException("Not expecting a start execute-query element");
        }
    }

    protected void endExecuteQuery() throws SAXException {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                executeQuery(current_query_index);
                queries.remove(current_query_index);
                --current_query_index;
                if (current_query_index>-1) {
                    current_state = getCurrentQuery().toDo;
                } else {
                    queries.removeAllElements();
                    current_state = LDAPTransformer.STATE_OUTSIDE;
                }
                break;

            default :
                throwIllegalStateException("Not expecting a end execute-query element");
        }
    }

    protected void startExecuteIncrement(Attributes attributes) {
        LDAPQuery query;

        switch (current_state) {
            case LDAPTransformer.STATE_OUTSIDE :
                current_state = LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT;
                current_query_index = queries.size();
                query = new LDAPQuery(this);
                queries.addElement(query);
                getCurrentQuery().toDo = LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT;
                getCurrentQuery().query_index = current_query_index;
                break;

            case LDAPTransformer.STATE_INSIDE_EXECUTE_QUERY :
                current_state = LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT;
                current_query_index = queries.size();
                query = new LDAPQuery(this);
                queries.addElement(query);
                getCurrentQuery().toDo = LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT;
                getCurrentQuery().query_index = current_query_index;
                break;

            default :
                throwIllegalStateException("Not expecting a start execute-increment element");
        }
    }

    protected void endExecuteIncrement() throws SAXException {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT :
                executeQuery(current_query_index);
                queries.remove(current_query_index);
                --current_query_index;
                if (current_query_index>1) {
                    current_state = getCurrentQuery().toDo;
                } else {
                    queries.removeAllElements();
                    current_state = LDAPTransformer.STATE_OUTSIDE;
                }
                break;

            default :
                throwIllegalStateException("Not expecting a end execute-increment element");
        }
    }

    protected void startInitializerElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start initializer element");
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

    protected void startScopeElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start scope element");
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

    protected void startAuthenticationElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start authentication element");
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

    protected void startServerurlElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SERVERURL_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SERVERURL_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start serverurl element");
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

    protected void startPortElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_PORT_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_PORT_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start port element");
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
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SHOW_ATTRIBUTE_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
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
                if (current_value.toString().toUpperCase().equals("FALSE")) {
                    getCurrentQuery().showAttribute = false;
                }
                current_state = getCurrentQuery().toDo;
                break;

            default :
                throwIllegalStateException("Not expecting a end show-attribute element");
        }
    }

    protected void startSearchbaseElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SEARCHBASE_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SEARCHBASE_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start searchbase element");
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

    protected void startDocElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_DOC_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_DOC_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start doc-element element");
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

    protected void startRowElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_ROW_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_ROW_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start row-element element");
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

    protected void startErrorElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_ERROR_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_ERROR_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start error-element element");
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

    protected void startSaxError(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SAX_ERROR_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_SAX_ERROR_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start sax-error element");
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

    protected void startRootDnElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_ROOT_DN_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_ROOT_DN_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start root-dn element");
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

    protected void startPasswordElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_PASSWORD_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_PASSWORD_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start password element");
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
                current_state = LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT;
                current_value.setLength(0);
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
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
                getCurrentQuery().addAttrList(current_value.toString());
                current_state = getCurrentQuery().toDo;
                break;

            default :
                throwIllegalStateException("Not expecting a end attribute element");
        }
    }

    protected void startVersionElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_state = LDAPTransformer.STATE_INSIDE_VERSION_ELEMENT;
                current_value.setLength(0);
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_state = LDAPTransformer.STATE_INSIDE_VERSION_ELEMENT;
                current_value.setLength(0);
                break;

            default :
                throwIllegalStateException("Not expecting a start version element");
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
                current_state = LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT;
                getCurrentQuery().current_state = LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT;
                current_value.setLength(0);
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
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

    protected void startDerefLinkElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_state = LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT;
                current_value.setLength(0);
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_state = LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT;
                current_value.setLength(0);
                break;

            default :
                throwIllegalStateException("Not expecting a start deref-link element");
        }
    }

    protected void endDerefLinkElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT :
                if (current_value.toString().toUpperCase().equals("TRUE")) {
                    getCurrentQuery().deref_link = true;
                }
                current_state = getCurrentQuery().toDo;
                break;

            default :
                throwIllegalStateException("Not expecting a end deref-link element");
        }
    }

    protected void startCountLimitElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_state = LDAPTransformer.STATE_INSIDE_COUNT_LIMIT_ELEMENT;
                current_value.setLength(0);
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_state = LDAPTransformer.STATE_INSIDE_COUNT_LIMIT_ELEMENT;
                current_value.setLength(0);
                break;

            default :
                throwIllegalStateException("Not expecting a start count-limit element");
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

    protected void startTimeLimitElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_state = LDAPTransformer.STATE_INSIDE_TIME_LIMIT_ELEMENT;
                current_value.setLength(0);
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_state = LDAPTransformer.STATE_INSIDE_TIME_LIMIT_ELEMENT;
                current_value.setLength(0);
                break;

            default :
                throwIllegalStateException("Not expecting a start time-limit element");
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

    protected void startDebugElement(Attributes attributes) {
        switch (current_state) {
            case STATE_INSIDE_EXECUTE_QUERY :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT;
                break;

            case STATE_INSIDE_EXECUTE_INCREMENT :
                current_value.setLength(0);
                current_state = LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT;
                break;

            default :
                throwIllegalStateException("Not expecting a start debug element");
        }
    }

    protected void endDebugElement() {
        switch (current_state) {
            case LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT :
                if (current_value.toString().toUpperCase().equals("TRUE")) {
                    getCurrentQuery().debug = true;
                }
                current_state = getCurrentQuery().toDo;
                break;

            default :
                throwIllegalStateException("Not expecting a end debug element");
        }
    }

    protected LDAPQuery getCurrentQuery() {
        return (LDAPQuery) queries.elementAt(current_query_index);
    }

    protected LDAPQuery getQuery(int i) {
        return (LDAPQuery) queries.elementAt(i);
    }

    /* END my very own methods */

    /* BEGIN SAX ContentHandler handlers */

    public void setDocumentLocator(Locator locator) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("PUBLIC ID: "+locator.getPublicId());
            getLogger().debug("SYSTEM ID: "+locator.getSystemId());
        }
        if (super.contentHandler!=null) {
            super.contentHandler.setDocumentLocator(locator);
        }
    }

    public void startElement(String uri, String name, String raw,
                             Attributes attributes) throws SAXException {
        if ( !uri.equals(my_uri)) {
            super.startElement(uri, name, raw, attributes);
            return;
        }
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("RECEIVED START ELEMENT "+name+"("+uri+")");
        }

        if (name.equals(LDAPTransformer.MAGIC_EXECUTE_QUERY)) {
            startExecuteQuery(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_INCREMENT)) {
            startExecuteIncrement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_INITIALIZER_ELEMENT)) {
            startInitializerElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_AUTHENTICATION_ELEMENT)) {
            startAuthenticationElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SCOPE_ELEMENT)) {
            startScopeElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_VERSION_ELEMENT)) {
            startVersionElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SERVERURL_ELEMENT)) {
            startServerurlElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_PORT_ELEMENT)) {
            startPortElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_DOC_ELEMENT)) {
            startDocElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_ROW_ELEMENT)) {
            startRowElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_ERROR_ELEMENT)) {
            startErrorElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SAX_ERROR)) {
            startSaxError(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_ROOT_DN_ELEMENT)) {
            startRootDnElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_PASSWORD_ELEMENT)) {
            startPasswordElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_ATTRIBUTE_ELEMENT)) {
            startAttributeElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SHOW_ATTRIBUTE_ELEMENT)) {
            startShowAttributeElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_SEARCHBASE_ELEMENT)) {
            startSearchbaseElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_FILTER_ELEMENT)) {
            startFilterElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_DEREF_LINK_ELEMENT)) {
            startDerefLinkElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_COUNT_LIMIT_ELEMENT)) {
            startCountLimitElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_TIME_LIMIT_ELEMENT)) {
            startTimeLimitElement(attributes);
        } else if (name.equals(LDAPTransformer.MAGIC_DEBUG_ELEMENT)) {
            startDebugElement(attributes);
        }
    }

    public void endElement(String uri, String name,
                           String raw) throws SAXException {
        if ( !uri.equals(my_uri)) {
            super.endElement(uri, name, raw);
            return;
        }
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("RECEIVED END ELEMENT "+name+"("+uri+")");
        }

        if (name.equals(LDAPTransformer.MAGIC_EXECUTE_QUERY)) {
            endExecuteQuery();
        } else if (name.equals(LDAPTransformer.MAGIC_EXECUTE_INCREMENT)) {
            endExecuteIncrement();
        } else if (name.equals(LDAPTransformer.MAGIC_INITIALIZER_ELEMENT)) {
            endInitializerElement();
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
        }
    }

    public void characters(char ary[], int start,
                           int length) throws SAXException {
        if ((current_state!=LDAPTransformer.STATE_INSIDE_INITIALIZER_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_AUTHENTICATION_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_SCOPE_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_VERSION_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_SERVERURL_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_PORT_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_DOC_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_ROW_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_ERROR_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_SAX_ERROR_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_ROOT_DN_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_PASSWORD_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_ATTRIBUTE_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_SHOW_ATTRIBUTE_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_DEREF_LINK_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_COUNT_LIMIT_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_TIME_LIMIT_ELEMENT) &&
            (current_state!=LDAPTransformer.STATE_INSIDE_DEBUG_ELEMENT) &&
            (current_state!=
             LDAPTransformer.STATE_INSIDE_SEARCHBASE_ELEMENT)) {
            super.characters(ary, start, length);
        }
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("RECEIVED CHARACTERS: "+
                              new String(ary, start, length));
        }
        current_value.append(ary, start, length);
    }

    private void start(String name, AttributesImpl attr) throws SAXException {
        super.contentHandler.startElement("", name, name, attr);
        attr.clear();
    }

    private void end(String name) throws SAXException {
        super.contentHandler.endElement("", name, name);
    }

    private void data(String data) throws SAXException {
        if (data!=null) {
            super.contentHandler.characters(data.toCharArray(), 0,
                                            data.length());
        }
    }

    protected static String getStringValue(Object object) {
        if (object instanceof byte[]) {
            return new String((byte[]) object);
        } else if (object instanceof char[]) {
            return new String((char[]) object);
        } else if (object!=null) {
            return object.toString();
        } else {
            return "";
        }
    }

    public final Logger getTheLogger() {
        return getLogger();
    }

    class LDAPQuery {

        /** What index are you in daddy's queries list */
        protected int query_index;

        /** The current state of the event receiving FSM */
        protected int current_state;

        /** Who's your daddy? */
        protected LDAPTransformer transformer;

        /** LDAP configuration information */
        protected String initializer = "com.sun.jndi.ldap.LdapCtxFactory";

        protected String serverurl = "localhost";
        protected int port = 389;
        protected String root_dn = "";
        protected String password = "";
        protected String version = "2";
        protected String scope = "ONELEVEL_SCOPE";
        protected String authentication = "simple";
        protected String encoding = "ISO-8859-1";

        /** LDAP environment information */
        protected Properties env = new Properties();

        protected DirContext ctx;

        /** LDAP Query */
        protected int toDo;

        protected String searchbase = "";

        protected List attrListe = new LinkedList();

        protected boolean showAttribute = true;

        protected String filter = "";

        protected String doc_element = "doc-element";
        protected String row_element = "row-element";
        protected String error_element = "ldap-error";

        protected boolean sax_error = false;

        protected boolean deref_link = false; // Dereference: true -> dereference the link during search
        protected long count_limit = 0;       // Maximum number of entries to return: 0 -> no limit
        protected int time_limit = 0;         // Number of milliseconds to wait before return: 0 -> infinite

        protected boolean debug = false;

        protected LDAPQuery(LDAPTransformer transformer) {
            this.transformer = transformer;
            if (null!=
                transformer.default_properties.getProperty(MAGIC_INITIALIZER_ELEMENT)) {
                initializer = transformer.default_properties.getProperty(MAGIC_INITIALIZER_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_SERVERURL_ELEMENT)) {
                serverurl = transformer.default_properties.getProperty(MAGIC_SERVERURL_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_PORT_ELEMENT)) {
                port = Integer.parseInt(transformer.default_properties.getProperty(MAGIC_PORT_ELEMENT));
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_ROOT_DN_ELEMENT)) {
                root_dn = transformer.default_properties.getProperty(MAGIC_ROOT_DN_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_PASSWORD_ELEMENT)) {
                password = transformer.default_properties.getProperty(MAGIC_PASSWORD_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_VERSION_ELEMENT)) {
                version = transformer.default_properties.getProperty(MAGIC_VERSION_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_SCOPE_ELEMENT)) {
                scope = transformer.default_properties.getProperty(MAGIC_SCOPE_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_AUTHENTICATION_ELEMENT)) {
                authentication = transformer.default_properties.getProperty(MAGIC_AUTHENTICATION_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_SEARCHBASE_ELEMENT)) {
                searchbase = transformer.default_properties.getProperty(MAGIC_SEARCHBASE_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_SHOW_ATTRIBUTE_ELEMENT)) {
                showAttribute = transformer.default_properties.getProperty(MAGIC_SHOW_ATTRIBUTE_ELEMENT).equals("FALSE")
                                ? false : true;
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_DOC_ELEMENT)) {
                doc_element = transformer.default_properties.getProperty(MAGIC_DOC_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_ROW_ELEMENT)) {
                row_element = transformer.default_properties.getProperty(MAGIC_ROW_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_ERROR_ELEMENT)) {
                error_element = transformer.default_properties.getProperty(MAGIC_ERROR_ELEMENT);
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_SAX_ERROR)) {
                sax_error = transformer.default_properties.getProperty(MAGIC_SAX_ERROR).equals("TRUE")
                            ? true : false;
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_DEREF_LINK_ELEMENT)) {
                deref_link = transformer.default_properties.getProperty(MAGIC_DEREF_LINK_ELEMENT).equals("TRUE")
                             ? true : false;
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_COUNT_LIMIT_ELEMENT)) {
                count_limit = Long.parseLong(transformer.default_properties.getProperty(MAGIC_COUNT_LIMIT_ELEMENT));
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_TIME_LIMIT_ELEMENT)) {
                time_limit = Integer.parseInt(transformer.default_properties.getProperty(MAGIC_TIME_LIMIT_ELEMENT));
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_DEBUG_ELEMENT)) {
                debug = transformer.default_properties.getProperty(MAGIC_DEBUG_ELEMENT).equals("TRUE")
                        ? true : false;
            }
            if (null!=
                transformer.default_properties.getProperty(MAGIC_ENCODING_ELEMENT)) {
                encoding = transformer.default_properties.getProperty(MAGIC_ENCODING_ELEMENT);
            }

        }

        protected void execute() throws Exception, NamingException {
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
                            if (attrList.length>0) {
                                constraints.setReturningAttributes(attrList);
                            }
                            NamingEnumeration ldapresults = ctx.search(searchbase,
                                                                       filter,
                                                                       constraints);

                            if ( !doc_element.equals("")) {
                                transformer.start(doc_element, attr);
                            }

                            while ((ldapresults!=null) &&
                                   ldapresults.hasMore()) {
                                if ( !row_element.equals("")) {
                                    transformer.start(row_element, attr);
                                }
                                SearchResult si = (SearchResult) ldapresults.next();
                                javax.naming.directory.Attributes attrs = si.getAttributes();

                                if (attrs!=null) {
                                    NamingEnumeration ae = attrs.getAll();

                                    while (ae.hasMoreElements()) {
                                        Attribute at = (Attribute) ae.next();
                                        Enumeration vals = at.getAll();
                                        String attrID = at.getID();

                                        if (showAttribute) {
                                            transformer.start(attrID, attr);
                                        }
                                        String attrVal = (String) vals.nextElement();

                                        // Changed by yuryx (encode string value to UTF characters from base encoding)
                                        if ( !encoding.equals("ISO-8859-1")) {
                                            attrVal = new String(attrVal.getBytes("ISO-8859-1"),
                                                                 encoding);
                                            attrVal = new String(attrVal.getBytes(encoding),
                                                                 "UTF-8");
                                        }
                                        ;
                                        // end
                                        if (query_index>0) {
                                            switch (transformer.getQuery(query_index-
                                                                         1).current_state) {
                                                case LDAPTransformer.STATE_INSIDE_FILTER_ELEMENT :
                                                    if ( !transformer.getQuery(query_index-
                                                                               1).filter.equals("")) {
                                                        transformer.getQuery(query_index-
                                                                             1).filter.concat(", ");
                                                    }
                                                    transformer.getQuery(query_index-
                                                                         1).filter.concat(attrID).concat("=").concat(attrVal);
                                                    break;

                                                default :
                                                    transformer.start(attrID,
                                                                      attr);
                                            }
                                        } else {
                                            transformer.data(String.valueOf(attrVal));
                                        }
                                        if (showAttribute) {
                                            transformer.end(attrID);
                                        }
                                    }
                                }
                                if ( !row_element.equals("")) {
                                    transformer.end(row_element);
                                }
                            }
                            if ( !doc_element.equals("")) {
                                transformer.end(doc_element);
                            }
                        } catch (Exception e) {
                            if (sax_error) {
                                throw new Exception("[LDAPTransformer] Error in LDAP-Query: "+
                                                    e.toString());
                            } else {
                                transformer.start(error_element, attr);
                                transformer.data("[LDAPTransformer] Error in LDAP-Query: "+
                                                 e);
                                transformer.end(error_element);
                                transformer.getTheLogger().error("[LDAPTransformer] Exception: "+
                                                                 e.toString());
                            }
                        }
                        break;

                    case LDAPTransformer.STATE_INSIDE_EXECUTE_INCREMENT :
                        try {
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
                            if (attrList.length!=1) {
                                transformer.start(error_element, attr);
                                transformer.data("Increment must reference exactly 1 attribute.");
                                transformer.end(error_element);
                            } else {
                                constraints.setReturningAttributes(attrList);
                                NamingEnumeration ldapresults = ctx.search(searchbase,
                                                                           filter,
                                                                           constraints);
                                int attrVal = 0;
                                String attrID = "";
                                SearchResult si = null;

                                while ((ldapresults!=null) &&
                                       ldapresults.hasMore()) {
                                    si = (SearchResult) ldapresults.next();
                                    javax.naming.directory.Attributes attrs = si.getAttributes();

                                    if (attrs!=null) {
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
                                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                                               new BasicAttribute(attrID,
                                                                   Integer.toString(attrVal)));
                                // Perform the requested modifications on the named object
                                ctx.modifyAttributes(new StringBuffer(si.toString().substring(0, si.toString().indexOf(":"))).append(",").append(searchbase).toString(),
                                                     mods);
                            }
                        } catch (Exception e) {
                            if (sax_error) {
                                throw new Exception("[LDAPTransformer] Error incrementing an attribute: "+
                                                    e.toString());
                            } else {
                                transformer.start(error_element, attr);
                                transformer.data("[LDAPTransformer] Error incrementing an attribute: "+
                                                 e.toString());
                                transformer.end(error_element);
                                transformer.getTheLogger().error("[LDAPTransformer] Error incrementing an attribute: "+
                                                                 e.toString());
                            }
                        }
                        break;

                    default :
                } // end switch
            } catch (NamingException e) {
                if (sax_error) {
                    throw new NamingException("[LDAPTransformer] Failed ldap-connection to directory service: "+
                                              e.toString());
                } else {
                    transformer.start(error_element, attr);
                    transformer.data("[LDAPTransformer] Failed ldap-connection to directory service.");
                    transformer.end(error_element);
                    transformer.getTheLogger().error("[LDAPTransformer] Failed to connect to "+
                                                     serverurl+e.toString());
                }
            }
            try {
                disconnect();
            } catch (NamingException e) {
                if (sax_error) {
                    throw new NamingException("[LDAPTransformer] Failed ldap-disconnection from directory service: "+
                                              e.toString());
                } else {
                    transformer.start(error_element, attr);
                    transformer.data("[LDAPTransformer] Failed ldap-disconnection to directory service.");
                    transformer.end(error_element);
                    transformer.getTheLogger().error("[LDAPTransformer] Failed to disconnect from "+
                                                     serverurl+e.toString());
                }
            }
        }

        protected void addAttrList(String attr) {
            attrListe.add(attr);
        }

        protected void connect() throws NamingException {
            if ((root_dn!=null) && (password!=null)) {
                env.put(Context.SECURITY_AUTHENTICATION, authentication);
                env.put(Context.SECURITY_PRINCIPAL, root_dn);
                env.put(Context.SECURITY_CREDENTIALS, password);
            }

            env.put("java.naming.ldap.version", version);
            env.put(Context.INITIAL_CONTEXT_FACTORY, initializer);
            env.put(Context.PROVIDER_URL, serverurl+":"+port);

            try {
                ctx = new InitialDirContext(env);
            } catch (NamingException e) {
                env.clear();
                throw new NamingException(e.toString());
            }
        }

        protected void disconnect() throws NamingException {
            try {
                if (ctx!=null) {
                    ctx.close();
                }
            } catch (NamingException e) {
                ctx = null;
                env.clear();
                throw new NamingException(e.toString());
            }
            ctx = null;
            env.clear();
        }

        protected void debugPrint() {
            transformer.getTheLogger().debug("[LDAPTransformer] query_index: "+
                                             query_index);
            transformer.getTheLogger().debug("[LDAPTransformer] current_state: "+
                                             current_state);
            transformer.getTheLogger().debug("[LDAPTransformer] serverurl: "+
                                             serverurl);
            transformer.getTheLogger().debug("[LDAPTransformer] port: "+port);
            transformer.getTheLogger().debug("[LDAPTransformer] root_dn: "+
                                             root_dn);
            transformer.getTheLogger().debug("[LDAPTransformer] password: "+
                                             password);
            transformer.getTheLogger().debug("[LDAPTransformer] version: "+
                                             version);
            transformer.getTheLogger().debug("[LDAPTransformer] scope: "+
                                             scope);
            transformer.getTheLogger().debug("[LDAPTransformer] authentication: "+
                                             authentication);
            transformer.getTheLogger().debug("[LDAPTransformer] toDo: "+toDo);
            transformer.getTheLogger().debug("[LDAPTransformer] searchbase: "+
                                             searchbase);
            transformer.getTheLogger().debug("[LDAPTransformer] showAttribute: "+
                                             showAttribute);
            transformer.getTheLogger().debug("[LDAPTransformer] attribute: "+
                                             attrListe.toString());
            transformer.getTheLogger().debug("[LDAPTransformer] filter: "+
                                             filter);
            transformer.getTheLogger().debug("[LDAPTransformer] doc_element: "+
                                             doc_element);
            transformer.getTheLogger().debug("[LDAPTransformer] row_element: "+
                                             row_element);
            transformer.getTheLogger().debug("[LDAPTransformer] error_element: "+
                                             error_element);
            transformer.getTheLogger().debug("[LDAPTransformer] sax-error: "+
                                             sax_error);
            transformer.getTheLogger().debug("[LDAPTransformer] deref_link: "+
                                             deref_link);
            transformer.getTheLogger().debug("[LDAPTransformer] count_limit: "+
                                             count_limit);
            transformer.getTheLogger().debug("[LDAPTransformer] time_limit: "+
                                             time_limit);
        }
    }
}
