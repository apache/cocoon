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
package org.apache.cocoon.components.elementprocessor;

import org.apache.cocoon.components.elementprocessor.types.Attribute;

import java.io.IOException;

/**
 * The ElementProcessor interface defines behavior for classes that
 * can handle a particular XML element's content.
 * <br>
 * The life cycle of an ElementProcessor instance is:
 * <ol>
 *     <li>Creation
 *     <li>Initialization via a call to initialize
 *     <li>Acquisition of element data via calls to acceptCharacters
 *         and acceptWhitespaceCharacters
 *     <li>Completion of processing via a call to endProcessing
 * </ol>
 * In response to a startElement event, the POIFSSerializer creates an
 * ElementProcessor, delegating the act of creation to an
 * ElementProcessorFactory, and then initializes the
 * ElementProcessor. In response to subsequent characters and
 * ignorableWhitespace events, the POIFSSerializer will pass data to
 * the ElementProcessor via the acceptCharacters or
 * acceptWhitespaceCharacters methods. Finally, in response to an
 * endElement event, the POIFSSerializer calls the ElementProcessor's
 * endProcessing method.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: ElementProcessor.java,v 1.2 2003/03/11 19:04:59 vgritsenko Exp $
 */
public interface ElementProcessor
                    extends org.apache.avalon.framework.component.Component
{

    String ROLE = ElementProcessor.class.getName();
    
    /**
     * The implementation should walk the array of attributes and
     * perform appropriate actions based on that data. The array of
     * attributes is guaranteed never to be null, but it can be
     * empty. An implementation can expect code like this to work
     * without throwing runtime exceptions:
     * <br>
     * <code>
     * for (int k = 0; k < attributes.length; k++)<br>
     * {<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Attribute attr = attributes[ k ];<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;// process attribute</br>
     * }</br>
     * </code>
     * <br>
     * <b>NOTE: if the XML DTD or schema includes <i>IMPLIED</i>
     * attributes for an element, those attributes are not included in
     * the Attribute array - they're not in the data provided in the
     * startElement call. The constructor for the ElementProcessor
     * should set those implied attributes itself, and allow them to
     * be overridden if the XML source provided <i>explicit</i> values
     * for them.</b>
     * <br>
     * The parent ElementProcessor is a reference to the
     * ElementProcessor whose corresponding XML element contains this
     * ElementProcessor's corresponding XML element. It will not be
     * null unless this ElementProcessor's corresponding XML element
     * is the top-level element in the XML document. Whether this
     * ElementProcessor needs to establish a containment relationship
     * with its parent or not, and whether it needs to do so at the
     * beginning, middle, or end of its life cycle, are private
     * matters left to the discretion of the individual
     * ElementProcessor implementation. If the ElementProcessor needs
     * to interact with its parent ElementProcessor, but is not ready
     * to do so when the initialize method is called, it must cache
     * the reference to its parent.
     * <br>
     *
     * @param attributes the array of Attribute instances; may be
     *                   empty, will never be null
     * @param parent the parent ElementProcessor; may be null
     *
     * @exception IOException if anything goes wrong
     */

    public void initialize(Attribute [] attributes, ElementProcessor parent)
        throws IOException;

    /**
     * The data provided in this method call comes from the
     * corresponding XML element's contents. The array is guaranteed
     * not to be null and will never be empty.
     * <br>
     * The POIFSSerializer will make 0 to many calls to this method;
     * all such calls will occur after the initialize method is called
     * and before the endProcessing method is called.
     * <br>
     * Calls to this method may be interleaved with calls to
     * acceptWhitespaceCharacters. All calls to acceptCharacters and
     * acceptWhitespaceCharacters are guaranteed to be in the same
     * order as their data is in the element.
     *
     * @param data the character data
     */

    public void acceptCharacters(char [] data);

    /**
     * The data provided in this method call comes from the
     * corresponding XML element's contents. The array is guaranteed
     * not to be null and will never be empty.
     * <br>
     * The POIFSSerializer will make 0 to many calls to this method;
     * all such calls will occur after the initialize method is called
     * and before the endProcessing method is called.
     * <br>
     * Calls to this method may be interleaved with calls to
     * acceptCharacters. All calls to acceptCharacters and
     * acceptWhitespaceCharacters are guaranteed to be in the same
     * order as their data is in the element.
     *
     * @param data the whitespace characters
     */

    public void acceptWhitespaceCharacters(char [] data);

    /**
     * This is the last method call executed by the
     * POIFSSerializer. When this method is called, the
     * ElementProcessor should finish its work and perform its final
     * interactions with its parent ElementProcessor.
     * <br>
     * If the implementation cached the parent ElementProcessor
     * reference, when the initialize method was called, it should
     * null out that reference.
     *
     * @exception IOException
     */

    public void endProcessing()
        throws IOException;
}   // end public interface ElementProcessor
