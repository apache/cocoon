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
 * @version CVS $Id: ElementProcessor.java,v 1.3 2004/03/05 13:02:03 bdelacretaz Exp $
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
