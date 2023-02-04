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
package org.apache.cocoon.environment.internal;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't 
 * really need it.
 *
 * This class is an {@link XMLConsumer} that changes the current environment.
 * When a pipeline calls an internal pipeline, two environments are
 * established: one for the calling pipeline and one for the internal pipeline.
 * Now, if SAX events are send from the internal pipeline, they are
 * received by some component of the calling pipeline, so inbetween we
 * have to change the environment forth and back.
 *
 * This environment changer push a given environment on the
 * environment stack before calling the embeded consumer and pops it
 * afterwards. It should be placed before a sitemap component that
 * should be executed in another environment.
 *
 * @version $Id$
 * @since 2.2
 */
final class PushEnvironmentChanger
    implements XMLConsumer {

    final XMLConsumer consumer;
    final Environment environment;

    PushEnvironmentChanger(XMLConsumer consumer, Environment environment) {
        this.consumer = consumer;
        this.environment = environment;
    }

    private void enterEnvironment() throws SAXException {
        try {
            EnvironmentHelper.enterEnvironment(this.environment);
        } catch (ProcessingException e) {
            throw new SAXException("PushEnvironmentChanger: ", e);
        }
    }

    private void leaveEnvironment() {
        EnvironmentHelper.leaveEnvironment();
    }

    public void setDocumentLocator(Locator locator) {
        try {
            enterEnvironment();
        } catch (SAXException e) {
            throw new UnableToPushEnvironmentException("Unable to push the environment", e);
        }
        this.consumer.setDocumentLocator(locator);
        leaveEnvironment();
    }

    public void startDocument()
    throws SAXException {
        enterEnvironment();
        this.consumer.startDocument();
        leaveEnvironment();
    }

    public void endDocument()
    throws SAXException {
        enterEnvironment();
        this.consumer.endDocument();
        leaveEnvironment();
    }

    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        enterEnvironment();
        this.consumer.startPrefixMapping(prefix, uri);
        leaveEnvironment();
    }

    public void endPrefixMapping(String prefix)
    throws SAXException {
        enterEnvironment();
        this.consumer.endPrefixMapping(prefix);
        leaveEnvironment();
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        enterEnvironment();
        this.consumer.startElement(uri, loc, raw, a);
        leaveEnvironment();
    }

    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        enterEnvironment();
        this.consumer.endElement(uri, loc, raw);
        leaveEnvironment();
    }

    public void characters(char c[], int start, int len)
    throws SAXException {
        enterEnvironment();
        this.consumer.characters(c, start, len);
        leaveEnvironment();
    }

    public void ignorableWhitespace(char c[], int start, int len)
    throws SAXException {
        enterEnvironment();
        this.consumer.ignorableWhitespace(c, start, len);
        leaveEnvironment();
    }

    public void processingInstruction(String target, String data)
    throws SAXException {
        enterEnvironment();
        this.consumer.processingInstruction(target, data);
        leaveEnvironment();
    }

    public void skippedEntity(String name)
    throws SAXException {
        enterEnvironment();
        this.consumer.skippedEntity(name);
        leaveEnvironment();
    }

    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        enterEnvironment();
        this.consumer.startDTD(name, publicId, systemId);
        leaveEnvironment();
    }

    public void endDTD()
    throws SAXException {
        enterEnvironment();
        this.consumer.endDTD();
        leaveEnvironment();
    }

    public void startEntity(String name)
    throws SAXException {
        enterEnvironment();
        this.consumer.startEntity(name);
        leaveEnvironment();
    }

    public void endEntity(String name)
    throws SAXException {
        enterEnvironment();
        this.consumer.endEntity(name);
        leaveEnvironment();
    }

    public void startCDATA()
    throws SAXException {
        enterEnvironment();
        this.consumer.startCDATA();
        leaveEnvironment();
    }

    public void endCDATA()
    throws SAXException {
        enterEnvironment();
        this.consumer.endCDATA();
        leaveEnvironment();
    }

    public void comment(char ch[], int start, int len)
    throws SAXException {
        enterEnvironment();
        this.consumer.comment(ch, start, len);
        leaveEnvironment();
    }
}
