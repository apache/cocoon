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
 * @version CVS $Id: TagSupport.java,v 1.2 2003/03/16 17:49:08 vgritsenko Exp $
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
