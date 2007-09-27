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
package org.apache.cocoon.portal.event.impl;

import junit.framework.TestCase;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.Receiver;

/**
 * $Id$
 */
public class DefaultEventManagerTestCase extends TestCase {

    protected DefaultEventManager eventManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        this.eventManager = new DefaultEventManager();
    }

    public void testEventReceiver() throws Exception {
        EventReceiver receiver = new EventReceiver();
        this.eventManager.subscribe(receiver);
        assertEquals(0, receiver.receiveCount);
        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);
        this.eventManager.send(new Event1());
        assertEquals(2, receiver.receiveCount);
        this.eventManager.send(new Event2());
        assertEquals(3, receiver.receiveCount);
        this.eventManager.send(new Event3());
        assertEquals(4, receiver.receiveCount);
    }

    public void testMultipleEventReceivers() throws Exception {
        EventReceiver receiver = new EventReceiver();
        EventReceiver1 receiver1 = new EventReceiver1();
        EventReceiver2 receiver2 = new EventReceiver2();
        EventReceiver3 receiver3 = new EventReceiver3();
        this.eventManager.subscribe(receiver);
        this.eventManager.subscribe(receiver1);
        this.eventManager.subscribe(receiver2);
        this.eventManager.subscribe(receiver3);
        assertEquals(0, receiver.receiveCount);
        assertEquals(0, receiver1.receiveCount);
        assertEquals(0, receiver2.receiveCount);
        assertEquals(0, receiver3.receiveCount);

        // each time we send an event we check all four receivers
        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);
        assertEquals(1, receiver1.receiveCount);
        assertEquals(0, receiver2.receiveCount);
        assertEquals(0, receiver3.receiveCount);

        this.eventManager.send(new Event1());
        assertEquals(2, receiver.receiveCount);
        assertEquals(2, receiver1.receiveCount);
        assertEquals(0, receiver2.receiveCount);
        assertEquals(0, receiver3.receiveCount);

        this.eventManager.send(new Event2());
        assertEquals(3, receiver.receiveCount);
        assertEquals(2, receiver1.receiveCount);
        assertEquals(1, receiver2.receiveCount);
        assertEquals(0, receiver3.receiveCount);

        this.eventManager.send(new Event3());
        assertEquals(4, receiver.receiveCount);
        assertEquals(2, receiver1.receiveCount);
        assertEquals(1, receiver2.receiveCount);
        assertEquals(1, receiver3.receiveCount);

        this.eventManager.send(new Event11());
        assertEquals(5, receiver.receiveCount);
        assertEquals(3, receiver1.receiveCount);
        assertEquals(1, receiver2.receiveCount);
        assertEquals(1, receiver3.receiveCount);

        this.eventManager.send(new Event12());
        assertEquals(6, receiver.receiveCount);
        assertEquals(4, receiver1.receiveCount);
        assertEquals(1, receiver2.receiveCount);
        assertEquals(1, receiver3.receiveCount);
    }

    public void testSimpleHierarchicalEventReceiver() throws Exception {
        EventReceiver10 receiver = new EventReceiver10();
        this.eventManager.subscribe(receiver);
        assertEquals(0, receiver.receiveCount11 + receiver.receiveCount12);

        this.eventManager.send(new Event1());
        assertEquals(0, receiver.receiveCount11 + receiver.receiveCount12);

        this.eventManager.send(new Event2());
        assertEquals(0, receiver.receiveCount11 + receiver.receiveCount12);

        this.eventManager.send(new Event3());
        assertEquals(0, receiver.receiveCount11 + receiver.receiveCount12);

        this.eventManager.send(new Event11());
        assertEquals(1, receiver.receiveCount11);
        assertEquals(0, receiver.receiveCount12);

        this.eventManager.send(new Event12());
        assertEquals(1, receiver.receiveCount11);
        assertEquals(1, receiver.receiveCount12);

        this.eventManager.send(new Event11());
        assertEquals(2, receiver.receiveCount11);
        assertEquals(1, receiver.receiveCount12);

        this.eventManager.send(new Event1());
        assertEquals(2, receiver.receiveCount11);
        assertEquals(1, receiver.receiveCount12);
    }

    public void testHierarchicalEventReceiver() throws Exception {
        EventReceiver10Full receiver = new EventReceiver10Full();
        this.eventManager.subscribe(receiver);
        assertEquals(0, receiver.receiveCount11 + receiver.receiveCount12);
        assertEquals(0, receiver.receiveCount);

        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);
        assertEquals(0, receiver.receiveCount11);
        assertEquals(0, receiver.receiveCount12);

        this.eventManager.send(new Event2());
        assertEquals(2, receiver.receiveCount);
        assertEquals(0, receiver.receiveCount11);
        assertEquals(0, receiver.receiveCount12);

        this.eventManager.send(new Event3());
        assertEquals(3, receiver.receiveCount);
        assertEquals(0, receiver.receiveCount11);
        assertEquals(0, receiver.receiveCount12);

        this.eventManager.send(new Event11());
        assertEquals(4, receiver.receiveCount);
        assertEquals(1, receiver.receiveCount11);
        assertEquals(0, receiver.receiveCount12);

        this.eventManager.send(new Event12());
        assertEquals(5, receiver.receiveCount);
        assertEquals(1, receiver.receiveCount11);
        assertEquals(1, receiver.receiveCount12);

        this.eventManager.send(new Event11());
        assertEquals(6, receiver.receiveCount);
        assertEquals(2, receiver.receiveCount11);
        assertEquals(1, receiver.receiveCount12);

        this.eventManager.send(new Event1());
        assertEquals(7, receiver.receiveCount);
        assertEquals(2, receiver.receiveCount11);
        assertEquals(1, receiver.receiveCount12);
    }

    public void testNoEventReceiver() throws Exception {
        this.eventManager.send(new Event1());
    }

    public void testUnsubscribe() throws Exception {
        EventReceiver receiver = new EventReceiver();
        this.eventManager.subscribe(receiver);
        assertEquals(0, receiver.receiveCount);
        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);
        this.eventManager.unsubscribe(receiver);
        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);
        this.eventManager.subscribe(receiver);
        this.eventManager.send(new Event2());
        assertEquals(2, receiver.receiveCount);
    }

    public void testMultiSubscribe() throws Exception {
        EventReceiver receiver = new EventReceiver();
        this.eventManager.subscribe(receiver);
        assertEquals(0, receiver.receiveCount);
        this.eventManager.send(new Event1());
        assertEquals(1, receiver.receiveCount);

        this.eventManager.subscribe(receiver);
        this.eventManager.send(new Event1());
        assertEquals(2, receiver.receiveCount);
        this.eventManager.subscribe(receiver);
        this.eventManager.send(new Event2());
        assertEquals(3, receiver.receiveCount);
    }

    public static final class EventReceiver implements Receiver {

        public int receiveCount;

        public void inform(Event event, PortalService service) {
            receiveCount++;
        }
    }

    public static final class EventReceiver1 implements Receiver {

        public int receiveCount;

        public void inform(Event1 event, PortalService service) {
            receiveCount++;
        }
    }

    public static final class EventReceiver2 implements Receiver {

        public int receiveCount;

        public void inform(Event2 event, PortalService service) {
            receiveCount++;
        }
    }

    public static final class EventReceiver3 implements Receiver {

        public int receiveCount;

        public void inform(Event3 event, PortalService service) {
            receiveCount++;
        }
    }

    public static final class EventReceiver10 implements Receiver {

        public int receiveCount11;
        public int receiveCount12;

        public void inform(Event11 event, PortalService service) {
            receiveCount11++;
        }

        public void inform(Event12 event, PortalService service) {
            receiveCount12++;
        }
    }

    public static final class EventReceiver10Full implements Receiver {

        public int receiveCount;
        public int receiveCount11;
        public int receiveCount12;

        public void inform(Event event, PortalService service) {
            receiveCount++;
        }

        public void inform(Event11 event, PortalService service) {
            receiveCount11++;
        }

        public void inform(Event12 event, PortalService service) {
            receiveCount12++;
        }
    }

    public static class Event1 implements Event {
        // dummy event
    }

    public static class Event2 implements Event {
        // dummy event
    }

    public static class Event3 implements Event {
        // dummy event
    }

    public static class Event11 extends Event1 {
        // dummy event
    }

    public static class Event12 extends Event1 {
        // dummy event
    }
}
