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
package org.apache.cocoon.components.search;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.ProcessingException;

import java.net.URL;
import java.util.List;

/**
 * The avalon behavioural component interface of generating
 * lucene documents from an xml content.
 *
 * <p>
 *  The well-known fields of a lucene documents are defined as
 *  <code>*_FIELD</code> constants.
 * </p>
 * <p>
 *  You may access generated lucene documents via
 *  <code>allDocuments()</code>, or <code>iterator()</code>.
 * </p>
 * <p>
 *  You trigger the generating of lucene documents via
 *  <code>build()</code>.
 * </p>
 *
 * @author <a href="mailto:berni_huber@a1.net">Bernhard Huber</a>
 * @version CVS $Id: LuceneXMLIndexer.java,v 1.3 2004/03/05 13:01:59 bdelacretaz Exp $
 */
public interface LuceneXMLIndexer extends Component
{

    /**
     * The ROLE name of this avalon component.
     * <p>
     *   Its value if the FQN of this interface,
     *   ie. <code>org.apache.cocoon.components.search.LuceneXMLIndexer</code>.
     * </p>
     *
     * @since
     */
    String ROLE = "org.apache.cocoon.components.search.LuceneXMLIndexer";

    /**
     * A Lucene document field name, containing xml content text of all xml elements.
     * <p>
     *   A concrete implementation of this interface SHOULD
     *   provides a field named body.
     * </p>
     * <p>
     *   A concrete implementation MAY provide additional lucene
     *   document fields.
     * </p>
     *
     * @since
     */
    String BODY_FIELD = "body";

    /**
     * A Lucene document field name, containg the URI/URL of the indexed
     * document.
     * <p>
     *   A concrete implementation of this interface SHOULD
     *   provide a field named url.
     * </p>
     *
     * @since
     */
    String URL_FIELD = "url";

    /**
     * A Lucene document field name, containg the a unique key of the indexed
     * document.
     * <p>
     *  This document field is used internally to track document
     *  changes, and updates.
     * </p>
     * <p>
     *   A concrete implementation of this interface SHOULD
     *   provide a field named uid.
     * </p>
     *
     * @since
     */
    String UID_FIELD = "uid";

    /**
     * Build lucene documents from a URL.
     * <p>
     *   This method will read the content of the URL, and generates
     *   one or more lucene documents. The generated lucence documents
     *   can be fetched using methods allDocuments(), and iterator().
     * </p>
     *
     * @param  url                      the content of this url gets indexed.
     * @exception  ProcessingException  Description of Exception
     * @since
     */
    List build(URL url) throws ProcessingException;
}
