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
 * @version CVS $Id: XSPGenerator.java,v 1.3 2003/12/06 21:22:09 cziegeler Exp $
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
