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
package org.apache.cocoon.webapps.portal.components;

import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.SAXException;
import org.apache.excalibur.source.SourceParameters;
import org.apache.cocoon.ProcessingException;

/**
 * The coplet interface
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: Coplet.java,v 1.2 2004/03/05 13:02:18 bdelacretaz Exp $
*/
public interface Coplet {

    /**
     * 	This will be called before execute() is called. Should really
     *  only be called when the coplet is loaded (caching).
     *
    */
    boolean init(Map                objectModel,
                 SourceParameters   parameters)
    throws ProcessingException;

    /**
     * Should stream the content to the consumer.
     * The content must be inside a <content> node!
     * If no content is provided (e.g. if size is min)
     * than no <content> node should be generated!
     */
    void execute(ContentHandler     contentHandler,
                 LexicalHandler     lexicalHandler,
                 Map                objectModel,
                 SourceParameters   parameters)
    throws SAXException, ProcessingException;
}

