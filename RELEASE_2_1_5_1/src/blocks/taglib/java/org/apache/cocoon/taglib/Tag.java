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
 * @version CVS $Id: Tag.java,v 1.3 2004/03/05 13:02:24 bdelacretaz Exp $
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
