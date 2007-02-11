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

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.SourceResolver;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * The Tag implementation works like a JSP Tag but generate SAX output
 * instead of writing to a OutputStream. The equivalent to the JSPEngine
 * is implemented as a Transformer.
 * 
 * @see org.apache.cocoon.transformation.TagTransformer
 * 
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: Tag.java,v 1.2 2003/03/16 17:49:08 vgritsenko Exp $
 */
public interface Tag extends Component {
    
    String ROLE = Tag.class.getName();

    /**
     * Evaluate body content
     * Valid return value for doStartTag.
     */
    int EVAL_BODY = 0;

    /**
     * Skip body evaluation.
     * Valid return value for doStartTag.
     */
    int SKIP_BODY = 1;

    /**
     * Continue evaluating the page.
     * Valid return value for doEndTag().
     */
    int EVAL_PAGE = 2;

    /**
     * Process the end tag for this instance.
     *
     * @return EVAL_PAGE
     * @throws SAXException
     */
    int doEndTag(String namespaceURI, String localName, String qName) throws SAXException;

    /**
     * Process the start tag for this instance.
     * <p>
     * The doStartTag method assumes that parent have been set.
     * It also assumes that any properties exposed as
     * attributes have been set too. When this method is invoked, the body
     * has not yet been evaluated.
     *
     * @return EVAL_BODY or SKIP_BODY.
     */
    int doStartTag(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException;

    /**
     * Get the parent (closest enclosing tag handler) for this tag handler.
     *
     * @return the current parent or null if none.
     */
    Tag getParent();

    /**
     * Set the <code>SourceResolver</code>, objectModel <code>Map</code>
     * and sitemap <code>Parameters</code> used to process the request.
     */
    void setup(SourceResolver resolver, Map objectModel, Parameters parameters) throws SAXException, IOException;

    /**
     * Set the parent (closest enclosing tag handler) of this tag handler.
     * Invoked by the implementation object prior to doStartTag().
     *
     * @param parent The parent tag or null.
     */
    void setParent(Tag parent);
}
