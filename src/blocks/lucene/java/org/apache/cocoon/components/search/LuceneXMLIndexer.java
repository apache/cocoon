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
 * @version CVS $Id: LuceneXMLIndexer.java,v 1.2 2003/03/11 17:44:21 vgritsenko Exp $
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
