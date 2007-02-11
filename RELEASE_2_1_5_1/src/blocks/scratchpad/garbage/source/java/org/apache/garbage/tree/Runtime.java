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
package org.apache.garbage.tree;

import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Runtime.java,v 1.2 2004/03/05 10:07:24 bdelacretaz Exp $
 */
public interface Runtime {

    /**
     * Receive notification of a <code>DocType</code> event.
     *
     * @see DocType
     */
    public void doctype(String name, String public_id, String system_id)
    throws SAXException;

    /**
     * Receive notification of a <code>CData</code> event.
     *
     * @see CData
     */
    public void cdata(char data[])
    throws SAXException;

    /**
     * Receive notification of a <code>Characters</code> event.
     *
     * @see Characters
     */
    public void characters(char data[])
    throws SAXException;

    /**
     * Receive notification of a <code>Comment</code> event.
     *
     * @see Comment
     */
    public void comment(char data[])
    throws SAXException;

    /**
     * Receive notification of a <code>ProcessingInstruction</code> event.
     *
     * @see ProcessingInstruction
     */
    public void processingInstruction(String target, String data)
    throws SAXException;

    /**
     * Receive notification of a <code>ElementStart</code> event.
     * <p>
     * The <code>attributes</code> array will be formatted as following:
     * <ul>
     *   <li><code>attributes[...][0]</code> will contain the prefix.</li>
     *   <li><code>attributes[...][1]</code> will contain the local name.</li>
     *   <li><code>attributes[...][2]</code> will contain the qualified name.</li>
     *   <li><code>attributes[...][3]</code> will contain the value.</li>
     * </ul>
     * </p>
     * <p>
     * The <code>namespaces</code> array will be formatted as following:
     * <ul>
     *   <li><code>namespaces[...][0]</code> will contain the prefix.</li>
     *   <li><code>namespaces[...][1]</code> will contain the uri.</li>
     * </ul>
     * </p>
     *
     * @param prefix The namespace prefix of the of the element.
     * @param local The local name (without prefix) of the element.
     * @param qualified The fully qualified name of the element.
     * @param attributes All attributes associated with this element.
     * @param namespaces All namespaces declared by this element.
     * @see ElementStart
     */
    public void startElement(String prefix, String local, String qualified,
                             String attributes[][], String namespaces[][])
    throws SAXException;

    /**
     * Receive notification of a <code>ElementEnd</code> event.
     *
     * @param prefix The namespace prefix of the of the element.
     * @param local The local name (without prefix) of the element.
     * @param qualified The fully qualified name of the element.
     * @see ElementEnd
     */
    public void endElement(String prefix, String local, String qualified)
    throws SAXException;

}
