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
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.regexp.RE;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * JPath Transformer.
 *
 * <p>
 *  Transformer implementation of the JPath XSP tag library.
 * </p>
 *
 * <p>
 *  This transformer (so far) supports the following jpath elements:
 *
 *   <ul>
 *     <li>&lt;jpath:value-of select=".."/&gt; element.
 *     <li>&lt;jpath:continuation/&gt; element.
 *     <li>&lt;jpath:if test=".."&gt;..&lt;/jpath:if&gt; element.
 *     <li>jpath:action attribute on all elements that implicitly replaces any
 *         occurance of the string 'id' with the continuation id (useful when
 *         writing form action attributes). eg:
 *         <pre>&lt;form name="myform" jpath:action="../cont/id"&gt;..&lt;/form&gt;</pre>
 *   </ul>
 * </p>
 *
 * @author <a href="mailto:crafterm@apache.org">Marcus Crafter</a>
 * @version CVS $Id: JPathTransformer.java,v 1.7 2004/03/08 14:03:30 cziegeler Exp $
 */
public class JPathTransformer
extends AbstractSAXTransformer implements Initializable {

    /** namespace constant */
    public static final String JPATH_NAMESPACE_URI  = "http://apache.org/xsp/jpath/1.0";

    /** jpath:action attribute constant */
    public static final String JPATH_ACTION         = "jpath:action";

    /** jpath:value-of element constant */
    public static final String JPATH_VALUEOF        = "value-of";

    /** jpath:value-of select attribute constant */
    public static final String JPATH_VALUEOF_SELECT = "select";

    /** jpath:continuation element constant */
    public static final String JPATH_CONTINUATION   = "continuation";

    /** jpath:continuation select attribute constant */
    public static final String JPATH_CONTINUATION_SELECT = "select";

    /** jpath:if element constant */
    public static final String JPATH_IF             = "if";

    /** jpath generic test attribute */
    public static final String JPATH_TEST           = "test";

    // web contination
    private WebContinuation m_kont;

    // regular expression for matching 'id' strings with jpath:action
    private RE m_re;

    // jxpath context
    private JXPathContext m_jxpathContext;

    // jpath:value-of variable cache
    private Map m_cache;

    /**
     * Initialize this transformer.
     *
     * @exception Exception if an error occurs
     */
    public void initialize()
        throws Exception {

        namespaceURI = JPATH_NAMESPACE_URI;
        m_re = new RE("id");
        m_cache = new HashMap();
    }

    /**
     * Setup this transformer
     *
     * @param resolver a {@link SourceResolver} instance
     * @param objectModel the objectModel
     * @param src <code>src</code> parameter
     * @param parameters optional parameters
     * @exception ProcessingException if an error occurs
     * @exception SAXException if an error occurs
     * @exception IOException if an error occurs
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);

        // setup the jpath transformer for this thread
        Object bean = FlowHelper.getContextObject(objectModel);
        m_kont = FlowHelper.getWebContinuation(objectModel);
        m_jxpathContext = JXPathContext.newContext(bean);
    }

    /**
     * Intercept startElement to ensure all &lt;jpath:action&gt; attributes
     * are modified.
     *
     * @param uri a <code>String</code> value
     * @param loc a <code>String</code> value
     * @param raw a <code>String</code> value
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {

        AttributesImpl impl = new AttributesImpl(a);
        checkJPathAction(impl);

        super.startElement(uri, loc, raw, impl);
    }

    /**
     * Entry method for all elements in our namespace
     *
     * @param uri a <code>String</code> value
     * @param name a <code>String</code> value
     * @param raw a <code>String</code> value
     * @param attr an <code>Attributes</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void startTransformingElement(
        String uri, String name, String raw, Attributes attr
    )
        throws ProcessingException ,IOException, SAXException {

        if (JPATH_VALUEOF.equals(name)) {
            doValueOf(attr);
        } else if (JPATH_CONTINUATION.equals(name)) {
            doContinuation(attr);
        } else if (JPATH_IF.equals(name)) {
            doIf(attr);
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
    }

    /**
     * Exit method for all elements in our namespace
     *
     * @param uri a <code>String</code> value
     * @param name a <code>String</code> value
     * @param raw a <code>String</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void endTransformingElement(
        String uri, String name, String raw
    )
        throws ProcessingException, IOException, SAXException {

        if (JPATH_VALUEOF.equals(name) ||
            JPATH_CONTINUATION.equals(name)) {
            return; // do nothing
        } else if (JPATH_IF.equals(name)) {
            finishIf();
        } else {
            super.endTransformingElement(uri, name, raw);
        }
    }

    /**
     * Helper method to check for the existance of an attribute named
     * jpath:action. If existing the string 'id' is replaced with the
     * continuation id.
     *
     * @param a an {@link AttributesImpl} instance
     */
    private void checkJPathAction(final AttributesImpl a) {

        // check for jpath:action attribute
        int idx = a.getIndex(JPATH_ACTION);

        if (idx != -1 && JPATH_NAMESPACE_URI.equals(a.getURI(idx))) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("found jpath:action, adjusting");
            }

            String value = a.getValue(idx);

            // REVISIT(MC): support for continuation level
            String id = m_kont.getContinuation(0).getId();

            a.removeAttribute(idx);
            a.addAttribute(
                "", "action", "action", "CDATA", m_re.subst(value, id)
            );
        }
    }

    /**
     * Helper method for obtaining the value of a particular variable.
     *
     * @param variable variable name
     * @return variable value as an <code>Object</code>
     */
    private Object getValue(final String variable) {

        Object value;

        if (m_cache.containsKey(variable)) {
            value = m_cache.get(variable);
        } else {
            value = JXPathContext.compile(variable).getValue(m_jxpathContext);

            if (value == null) {
                if (getLogger().isWarnEnabled()) {
                    final String msg =
                        "Value for jpath variable '" + variable + "' does not exist";
                    getLogger().warn(msg);
                }
            }

            m_cache.put(variable, value);
        }

        return value;
    }

    /**
     * Helper method to process a &lt;jpath:value-of select="."&gt; tag
     *
     * @param a an {@link Attributes} instance
     * @exception SAXException if a SAX error occurs
     * @exception ProcessingException if a processing error occurs
     */
    private void doValueOf(final Attributes a)
        throws SAXException, ProcessingException {

        final String select = a.getValue(JPATH_VALUEOF_SELECT);

        if (null != select) {
            sendTextEvent((String)getValue(select));
        } else {
            throw new ProcessingException(
                "jpath:" + JPATH_VALUEOF + " specified without a select attribute"
            );
        }
    }

    /**
     * Helper method to process a &lt;jpath:continuation select=""/&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doContinuation(final Attributes a)
        throws SAXException {

        final String level = a.getValue(JPATH_CONTINUATION_SELECT);

        final String id = (level != null)
            ? m_kont.getContinuation(Integer.decode(level).intValue()).getId()
            : m_kont.getContinuation(0).getId();

        sendTextEvent(id);
    }

    /**
     * Helper method to process a &lt;jpath:if test="..."&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doIf(final Attributes a)
        throws SAXException {

        // handle nested jpath:if statements, if ignoreEventsCount is > 0, then
        // we are processing a nested jpath:if statement for which the parent
        // jpath:if test resulted in a false (ie. disallow subelements) result.

        if (ignoreEventsCount > 0) {
            ++ignoreEventsCount;
            return;
        }

        // get the test variable
        final Object value = getValue(a.getValue(JPATH_TEST));

        final boolean isTrueBoolean =
            value instanceof Boolean && ((Boolean)value).booleanValue() == true;
        final boolean isNonNullNonBoolean =
            value != null && !(value instanceof Boolean);

        if (isTrueBoolean || isNonNullNonBoolean) {
            // do nothing, allow all subelements
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("jpath:if results in allowing subelements");
            }
        } else {
            // disallow all subelements
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("jpath:if results in disallowing subelements");
            }
            ++ignoreEventsCount;
        }
    }

    /**
     * Helper method to process a &lt;/jpath:if&gt; element.
     *
     * @exception SAXException if an error occurs
     */
    private void finishIf()
        throws SAXException {

        // end recording (and dump resulting document fragment) if we've reached
        // the closing jpath:if tag for which the recording was started.
        if (ignoreEventsCount > 0) {
            --ignoreEventsCount;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("jpath:if closed");
        }
    }

    /**
     * Release all held resources.
     */
    public void recycle() {
        super.recycle();

        m_cache.clear();
        m_kont = null;
        m_jxpathContext = null;
    }
}
