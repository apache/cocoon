/* ============================================================================ *
 *                   The Apache Software License, Version 1.1                   *
 * ============================================================================ *
 *                                                                              *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved. *
 *                                                                              *
 * Redistribution and use in source and binary forms, with or without modifica- *
 * tion, are permitted provided that the following conditions are met:          *
 *                                                                              *
 * 1. Redistributions of  source code must  retain the above copyright  notice, *
 *    this list of conditions and the following disclaimer.                     *
 *                                                                              *
 * 2. Redistributions in binary form must reproduce the above copyright notice, *
 *    this list of conditions and the following disclaimer in the documentation *
 *    and/or other materials provided with the distribution.                    *
 *                                                                              *
 * 3. The end-user documentation included with the redistribution, if any, must *
 *    include  the following  acknowledgment:  "This product includes  software *
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)." *
 *    Alternately, this  acknowledgment may  appear in the software itself,  if *
 *    and wherever such third-party acknowledgments normally appear.            *
 *                                                                              *
 * 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be *
 *    used to  endorse or promote  products derived from  this software without *
 *    prior written permission. For written permission, please contact          *
 *    apache@apache.org.                                                        *
 *                                                                              *
 * 5. Products  derived from this software may not  be called "Apache", nor may *
 *    "Apache" appear  in their name,  without prior written permission  of the *
 *    Apache Software Foundation.                                               *
 *                                                                              *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND *
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE *
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT, *
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU- *
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS *
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON *
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT *
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF *
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
 *                                                                              *
 * This software  consists of voluntary contributions made  by many individuals *
 * on  behalf of the Apache Software  Foundation.  For more  information on the *
 * Apache Software Foundation, please see <http://www.apache.org/>.             *
 *                                                                              *
 * ============================================================================ */
package org.apache.garbage.tree;

import org.xml.sax.SAXException;

/**
 * 
 * 
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>, February 2003
 * @version CVS $Id: Runtime.java,v 1.1 2003/09/04 12:42:32 cziegeler Exp $
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
