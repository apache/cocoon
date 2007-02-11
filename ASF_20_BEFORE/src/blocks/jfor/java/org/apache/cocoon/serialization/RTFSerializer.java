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
package org.apache.cocoon.serialization;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.jfor.jfor.converter.Converter;

/**
 * This class uses the <a href="http://www.jfor.org">jfor</a> library
 * to serialize XSL:FO documents to RTF streams.
 *
 * @author <a href="mailto:gianugo@rabellino.it">Gianugo Rabellino</a>
 * @version CVS $Id: RTFSerializer.java,v 1.2 2003/09/24 21:54:48 cziegeler Exp $
 */

public class RTFSerializer extends AbstractTextSerializer
  implements LogEnabled {

    private Writer rtfWriter;
    private Converter handler;


    /**
     * Set the OutputStream where the serializer will write to.
     *
     * @param out the OutputStream
     */
    public void setOutputStream(OutputStream out) {
        try {
            rtfWriter =
            new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1"));

            // FIXME Find a way to work with the org.apache.avalon.framework.logger.Logger
            handler = new Converter(rtfWriter,
               Converter.createConverterOption(System.out));
            super.setContentHandler(handler);

        } catch (Exception e) {
            getLogger().error("RTFSerializer.setOutputStream()", e);
            throw new CascadingRuntimeException("RTFSerializer.setOutputStream()", e);
        }
    }

    /**
     * Recycle the serializer. GC instance variables
     */
    public void recycle() {
        super.recycle();
        this.rtfWriter = null;
        this.handler = null;
    }
}
