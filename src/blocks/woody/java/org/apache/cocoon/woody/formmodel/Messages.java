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
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.util.StringMessage;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A widget to output one or messages. This widget doesn't respond to input from the user, except
 * that on each form submit the messages are cleared.
 *
 * <p>This widget is typically used to communicate extra validation errors or other messages
 * to the user, that aren't associated with any other widget in particular.
 */
public class Messages extends AbstractWidget {
    private ArrayList messages = new ArrayList();
    private MessagesDefinition definition;

    private static final String MESSAGES_EL = "messages";
    private static final String MESSAGE_EL = "message";

    protected Messages(MessagesDefinition definition) {
        this.definition = definition;
        setLocation(definition.getLocation());
    }

    public String getId() {
        return definition.getId();
    }

    public void readFromRequest(FormContext formContext) {
        messages.clear();
    }

    public boolean validate(FormContext formContext) {
        return messages.size() == 0;
    }

    /**
     * Adds a string message.
     */
    public void addMessage(String message) {
        messages.add(new StringMessage(message));
    }

    /**
     * Adds a message in the form an object that implements the XMLizable interface.
     * This allows to add messages that produce mixed content. The XMLizable should
     * only generate a SAX fragment, i.e. without start/endDocument calls.
     *
     * <p>A useful implementation is {@link org.apache.cocoon.woody.util.I18nMessage I18nMesage}.
     */
    public void addMessage(XMLizable message) {
        messages.add(message);
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        contentHandler.startElement(Constants.WI_NS, MESSAGES_EL, Constants.WI_PREFIX_COLON + MESSAGES_EL, Constants.EMPTY_ATTRS);

        definition.generateDisplayData(contentHandler);

        Iterator messagesIt = messages.iterator();
        while (messagesIt.hasNext()) {
            XMLizable message = (XMLizable)messagesIt.next();
            contentHandler.startElement(Constants.WI_NS, MESSAGE_EL, Constants.WI_PREFIX_COLON + MESSAGE_EL, Constants.EMPTY_ATTRS);
            message.toSAX(contentHandler);
            contentHandler.endElement(Constants.WI_NS, MESSAGE_EL, Constants.WI_PREFIX_COLON + MESSAGE_EL);
        }

        contentHandler.endElement(Constants.WI_NS, MESSAGES_EL, Constants.WI_PREFIX_COLON + MESSAGES_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }
}
