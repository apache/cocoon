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
package org.apache.cocoon.taglib;

import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Abstract implementation for all Tags
 * 
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: TagSupport.java,v 1.3 2004/03/05 13:02:24 bdelacretaz Exp $
 */
public abstract class TagSupport extends AbstractLogEnabled implements Tag, Recyclable {
    protected String var;
    protected Tag parent;
    protected SourceResolver resolver;
    protected Map objectModel;
    protected Parameters parameters;
    private Request request;

    /**
     * Find the instance of a given class type that is closest to a given
     * instance.
     * This method uses the getParent method from the Tag
     * interface.
     * This method is used for coordination among cooperating tags.
     *
     * @param from The instance from where to start looking.
     * @param klass The subclass of Tag or interface to be matched
     * @return the nearest ancestor that implements the interface
     * or is an instance of the class specified
     */
    public static final Tag findAncestorWithClass(Tag from, Class klass) {
        boolean isInterface = false;

        if (from == null || klass == null || (!Tag.class.isAssignableFrom(klass) && !(isInterface = klass.isInterface()))) {
            return null;
        }

        for (;;) {
            Tag tag = from.getParent();

            if (tag == null) {
                return null;
            }

            if ((isInterface && klass.isInstance(tag)) || klass.isAssignableFrom(tag.getClass()))
                return tag;
            else
                from = tag;
        }
    }

    /**
     * Process the end tag for this instance.
     *
     * @return EVAL_PAGE.
     * @throws SAXException
     */
    public int doEndTag(String namespaceURI, String localName, String qName) throws SAXException {
        return EVAL_PAGE;
    }

    /**
     * Process the start tag for this instance.
     * <p>
     * The doStartTag method assumes that pageContext and
     * parent have been set. It also assumes that any properties exposed as
     * attributes have been set too. When this method is invoked, the body
     * has not yet been evaluated.
     *
     * @return EVAL_BODY or SKIP_BODY.
     */
    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        return EVAL_BODY;
    }

    /**
     * Searches for the named attribute in request, session (if valid),
     * and application scope(s) in order and returns the value associated or
     * null.
     *
     * @return the value associated or null
     */
    public final Object findAttribute(String name) {
        if (request == null)
            request = ObjectModelHelper.getRequest(objectModel);
        Object o = request.getAttribute(name);
        if (o != null)
            return o;

        Session session = request.getSession(false);
        if (session != null) {
            o = session.getAttribute(name);
            if (o != null)
                return o;
        }
        Context context = ObjectModelHelper.getContext(objectModel);
        return context.getAttribute(name);
    }

    /**
     * Get the parent (closest enclosing tag handler) for this tag handler.
     *
     * @return the current parent, or null if none.
     */
    public final Tag getParent() {
        return parent;
    }

    public void recycle() {
        getLogger().debug("recycle");
        this.var = null;
        this.parent = null;
        this.resolver = null;
        this.objectModel = null;
        this.parameters = null;
        this.request = null;
    }

    /**
     * Set the parent (closest enclosing tag handler) of this tag handler.
     * Invoked by the TagTransformer prior to doStartTag().
     * <p>
     * This value is *not* reset by doEndTag() and must be explicitly reset
     * by a Tag implementation.
     *
     * @param parent the parent tag, or null.
     */
    public final void setParent(Tag parent) {
        this.parent = parent;
    }

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>
     * and sitemap <code>Parameters</code> used to process the request.
     */
    public void setup(SourceResolver resolver, Map objectModel, Parameters parameters) {
        this.resolver = resolver;
        this.objectModel = objectModel;
        this.parameters = parameters;
    }
}
