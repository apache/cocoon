/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.util.StringMessage;
import org.apache.cocoon.xml.XMLUtils;
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
 * @version $Id$
 */
public class Messages extends AbstractWidget {

    private static final String MESSAGES_EL = "messages";
    private static final String MESSAGE_EL = "message";

    private final MessagesDefinition definition;
    private ArrayList messages;


    protected Messages(MessagesDefinition definition) {
        super(definition);
        this.definition = definition;
        this.messages = new ArrayList();
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void readFromRequest(FormContext formContext) {
        if (getCombinedState().isAcceptingInputs()) {
            messages.clear();
        }
    }

    public boolean validate() {
        if (!getCombinedState().isValidatingValues())  {
            this.wasValid = true;
            return true;
        }
        this.wasValid = messages.size() == 0;
        return this.wasValid;
    }

    /**
     * Adds a string message.
     */
    public void addMessage(String message) {
        messages.add(new StringMessage(message));
        getForm().addWidgetUpdate(this);
    }

    /**
     * Adds a message in the form an object that implements the XMLizable interface.
     * This allows to add messages that produce mixed content. The XMLizable should
     * only generate a SAX fragment, i.e. without start/endDocument calls.
     *
     * <p>A useful implementation is {@link org.apache.cocoon.forms.util.I18nMessage I18nMesage}.
     */
    public void addMessage(XMLizable message) {
        messages.add(message);
        getForm().addWidgetUpdate(this);
    }

    /**
     * @return "messages"
     */
    public String getXMLElementName() {
        return MESSAGES_EL;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        Iterator i = messages.iterator();
        while (i.hasNext()) {
            XMLizable message = (XMLizable) i.next();
            contentHandler.startElement(FormsConstants.INSTANCE_NS, MESSAGE_EL, FormsConstants.INSTANCE_PREFIX_COLON + MESSAGE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            message.toSAX(contentHandler);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, MESSAGE_EL, FormsConstants.INSTANCE_PREFIX_COLON + MESSAGE_EL);
        }
    }

}
