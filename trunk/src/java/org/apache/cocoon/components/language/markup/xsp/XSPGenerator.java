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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.generation.AbstractServerPage;
import org.xml.sax.SAXException;

/**
 * Base class for XSP-generated <code>ServerPagesGenerator</code> classes
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: XSPGenerator.java,v 1.4 2004/03/08 13:58:31 cziegeler Exp $
 */
public abstract class XSPGenerator extends AbstractServerPage implements CompiledComponent, Contextualizable, Recyclable {
    protected Context avalonContext = null;

    /** Contextualize this class */
    public void contextualize(Context context) throws ContextException  {
        this.avalonContext = context;
    }

    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Generator</code> and initialize relevant instance variables.
     *
     * @param manager The global component manager
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
    }

    // XSP Helper methods accessible from the page

    /**
     * Add character data
     *
     * @param data The character data
     */
    public void xspCharacters(String data) throws SAXException {
        this.contentHandler.characters(data.toCharArray(), 0, data.length());
    }

    /**
     * Add a comment
     *
     * @param comment The comment data
     */
    public void xspComment(String comment) throws SAXException {
        this.lexicalHandler.comment(comment.toCharArray(), 0, comment.length());
    }

    /**
     * Implementation of &lt;xsp:expr&gt; for String, Collection,
     * XMLizable, Node, and Object.
     *
     * @param v the value
     */
    public void xspExpr(Object v) throws SAXException {
        XSPObjectHelper.xspExpr(this.contentHandler, v);
    }
}
