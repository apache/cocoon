/*-- $Id: AbstractProducer.java,v 1.4 1999-11-30 16:30:10 stefano Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
 
 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
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
package org.apache.cocoon.producer;

import java.io.*;
import org.w3c.dom.*;
import javax.servlet.http.*;
import org.apache.cocoon.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.framework.*;

/**
 * This abstract class implements the Producer interface and provides
 * utitity methods to convert the generated streams into DOM tress
 * that are used inside the processor pipeline. This class must be
 * seen as a transparent "mediator" between stream and DOM realms.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.4 $ $Date: 1999-11-30 16:30:10 $
 */

public abstract class AbstractProducer extends AbstractActor implements Producer, Defaults {
    
    /**
     * This method is the only one called by the Cocoon engine. Producers
     * are allowed to create streams and this class "mediates"
     * translating these streams into DOM trees. For producers willing
     * to generate DOM trees automatically, they should override this method
     * and may well ignore to implement the getStream() method since it's
     * never called directly by Cocoon.
     */
    public Document getDocument(HttpServletRequest request) throws Exception {
        Parser parser = (Parser) director.getActor("parser");
        org.xml.sax.InputSource input = new org.xml.sax.InputSource();
        input.setCharacterStream(getStream(request));
        return parser.parse(input);
    }
    
    /**
     * This method always returns true to reduce the evaluation overhead to
     * a minimum. Producer are highly encouradged to overwrite this method
     * if they can provide a fast way to evaluate the response change.
     */
    public boolean hasChanged(Object request) {
        return true;
    }
}