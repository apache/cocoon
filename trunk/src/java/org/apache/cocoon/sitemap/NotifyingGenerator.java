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
package org.apache.cocoon.sitemap;

import java.util.Map;
import java.io.IOException;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.Notifier;
import org.apache.cocoon.generation.AbstractGenerator;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Constants;

import org.xml.sax.SAXException;

/**
 * Generates an XML representation of the current notification.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:proyal@managingpartners.com">Peter Royal</a>
 * @version CVS $Id: NotifyingGenerator.java,v 1.5 2003/12/27 15:11:57 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=Generator
 * @x-avalon.lifestyle type=pooled
 * @x-avalon.info name=notifying-generator
 */
public class NotifyingGenerator extends AbstractGenerator implements Generator {

    /**
     * The <code>Notification</code> to report.
     */
    private Notifying notification;

    public void setup(SourceResolver resolver, Map objectModel, String src,
                      Parameters par) throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, par);

        this.notification  = (Notifying)objectModel.get(Constants.NOTIFYING_OBJECT);

        if ( this.notification  == null) {
            throw new ProcessingException("Expected Constants.NOTIFYING_OBJECT not found in object model");
        }
    }

    /**
     * Generate the notification information in XML format.
     *
     * @exception  SAXException  Description of problem there is creating the output SAX events.
     * @throws SAXException when there is a problem creating the
     *      output SAX events.
     */
    public void generate() throws SAXException {
        Notifier.notify(notification, this.contentHandler, "text/xml");
    }

    /**
     * Recycle
     */
    public void recycle() {
        super.recycle();
        this.notification = null;
    }
}

