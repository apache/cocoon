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
package org.apache.cocoon.xml;

import java.util.Iterator;
import java.util.Map;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Modification of the SAX buffer with parameterization capabilities.
 *
 * Any <code>{name}</code> expression inside of the character events can be
 * replaced by the content of another SaxBuffer if it is present in the map
 * passed to the {@link toSAX(ContentHandler, Map)} method.
 * 
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: ParamSaxBuffer.java,v 1.1 2003/12/09 21:37:35 vgritsenko Exp $
 */
public class ParamSaxBuffer extends SaxBuffer {

    /**
     * Creates empty SaxBuffer
     */
    public ParamSaxBuffer() {
    }

    /**
     * Creates copy of another SaxBuffer
     */
    public ParamSaxBuffer(SaxBuffer saxBuffer) {
        super(saxBuffer);
    }

    /**
     * Parses text and extracts <code>{name}</code> parameters for later
     * substitution.
     */
    public void characters(char ch[], int start, int length) throws SAXException {
        int end = start + length;
        for (int i = start; i < end; i++) {
            if (ch[i] == '{') {
                // Send any collected characters so far
                if (i > start) {
                    addBit(new Characters(ch, start, i - start));
                }
                
                // Find closing brace, and construct parameter name
                StringBuffer name = new StringBuffer();
                int j = i + 1;
                for (; j < end; j++) {
                    if (ch[j] == '}') {
                        break;
                    }
                    name.append(ch[j]);
                }
                if (j == length) {
                    throw new SAXException("Unclosed '}'");
                }
                addBit(new Parameter(name.toString()));
                
                // Continue processing
                i = j;
                start = j + 1;
                continue;
            }
        }
        
        // Send any tailing characters
        if (start < end) {
            addBit(new Characters(ch, start, end - start));
        }
    }

    /**
     * @param parameters map containing SaxBuffers
     */
    public void toSAX(ContentHandler contentHandler, Map parameters) throws SAXException {
        for (Iterator i = bits(); i.hasNext();) {
            SaxBit saxbit = (SaxBit)i.next();
            if (saxbit instanceof Parameter) {
                ((Parameter)saxbit).send(contentHandler, parameters);
            } else {
                saxbit.send(contentHandler);
            }
        }
    }


    final static class Parameter implements SaxBit {
        private final String name;

        public Parameter(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
        }

        public void send(ContentHandler contentHandler, Map parameters) throws SAXException {
            SaxBuffer value = (SaxBuffer)parameters.get(name);
            if (value != null) {
                value.toSAX(contentHandler);
            }
        }
    }
}
