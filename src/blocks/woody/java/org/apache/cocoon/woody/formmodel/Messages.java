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
 * 
 * @version $Id: Messages.java,v 1.6 2004/03/09 13:53:56 reinhard Exp $
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
