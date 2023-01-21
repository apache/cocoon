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
package org.apache.cocoon.acting;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import junit.framework.TestCase;

import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.NamedEvent;

/**
 * Unit test for {@link JMSEventMessageListener}.
 */
public class JMSEventMessageListenerTest extends TestCase {

    /**
     * Inner class for unit testing.
     */
    public final class TestJMSEventMessageListener extends
            JMSEventMessageListener {
        /**
         * Default constructor.
         * 
         * @throws JMSException
         *             In case, errors occur.
         */
        public TestJMSEventMessageListener() throws JMSException {
            super();
        }
    }

    /**
     * Test extracting an {@link Event} out of a {@link Message}.
     * 
     * @throws JMSException
     *             In case, some JMS action fails.
     */
    public void testEventsFromMessage() throws JMSException {
        TestJMSEventMessageListener listener = new TestJMSEventMessageListener();
        TextMessage msg = new DummyTextMessage();
        msg.setText("one");
        Event event = listener.eventFromTextMessage(msg);
        assertNotNull(event);
        assertEquals("NamedEvent[one]", ((NamedEvent) event).toString());
    }
}
