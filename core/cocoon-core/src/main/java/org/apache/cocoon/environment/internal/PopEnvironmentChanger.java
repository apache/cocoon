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
 * This environment changer pop the current environment from the
 * environment stack before calling the embeded consumer and push it
 * back afterwards. It should be placed after a sitemap component that
 * is be executed in another environment.
 *
 * @version $Id$
 * @since 2.2
 */
final class PopEnvironmentChanger
    implements XMLConsumer {

    final XMLConsumer consumer;

    PopEnvironmentChanger(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    private Environment leaveEnvironment() {
        return EnvironmentHelper.leaveEnvironment();
    }

    private void enterEnvironment(Environment environment) throws SAXException {
        try {
            EnvironmentHelper.enterEnvironment(environment);
        } catch (ProcessingException e) {
            throw new SAXException("Unable to enter the environment: " + environment, e);
        }
    }

    public void setDocumentLocator(Locator locator) {
        Environment environment = leaveEnvironment();
        this.consumer.setDocumentLocator(locator);
        try {
            enterEnvironment(environment);
        } catch (SAXException e) {
            throw new UnableToPopEnvironmentException("Unable to re-enter the environment: " + environment, e);
        }
    }

    public void startDocument()
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.startDocument();
        enterEnvironment(environment);
    }

    public void endDocument()
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.endDocument();
        enterEnvironment(environment);
    }

    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.startPrefixMapping(prefix, uri);
        enterEnvironment(environment);
    }

    public void endPrefixMapping(String prefix)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.endPrefixMapping(prefix);
        enterEnvironment(environment);
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.startElement(uri, loc, raw, a);
        enterEnvironment(environment);
    }

    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.endElement(uri, loc, raw);
        enterEnvironment(environment);
    }

    public void characters(char c[], int start, int len)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.characters(c, start, len);
        enterEnvironment(environment);
    }

    public void ignorableWhitespace(char c[], int start, int len)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.ignorableWhitespace(c, start, len);
        enterEnvironment(environment);
    }

    public void processingInstruction(String target, String data)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.processingInstruction(target, data);
        enterEnvironment(environment);
    }

    public void skippedEntity(String name)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.skippedEntity(name);
        enterEnvironment(environment);
    }

    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.startDTD(name, publicId, systemId);
        enterEnvironment(environment);
    }

    public void endDTD()
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.endDTD();
        enterEnvironment(environment);
    }

    public void startEntity(String name)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.startEntity(name);
        enterEnvironment(environment);
    }

    public void endEntity(String name)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.endEntity(name);
        enterEnvironment(environment);
    }

    public void startCDATA()
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.startCDATA();
        enterEnvironment(environment);
    }

    public void endCDATA()
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.endCDATA();
        enterEnvironment(environment);
    }

    public void comment(char ch[], int start, int len)
    throws SAXException {
        Environment environment = leaveEnvironment();
        this.consumer.comment(ch, start, len);
        enterEnvironment(environment);
    }
}
