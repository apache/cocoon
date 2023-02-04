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
package org.apache.cocoon.beans;

import javax.jms.Destination;
import javax.jms.Topic;

/**
 * This test verifies correct startup of various components (Spring beans).
 */
public class TestTopicMessageListener extends BaseMessageListenerTest {

    /**
     * Spring config file.
     */
    private static final String SPRING_CONFIG_FILE = "spring-activemq-topic.xml";

    /**
     * Spring {@link Topic} bean name.
     */
    private static final String TOPIC_BEAN = "topic";

    /**
     * Returns a {@link Topic} to use for JMS message transport.
     * 
     * @return A Spring {@link Topic}.
     */
    protected Destination getDestination() {
        return (Topic) factory.getBean(TOPIC_BEAN);
    }

    /**
     * Returns the Spring config file name.
     * 
     * @return Config file name.
     * @see org.apache.cocoon.beans.BaseMessageListenerTest#getSpringConfigFile()
     */
    protected String getSpringConfigFile() {
        return SPRING_CONFIG_FILE;
    }
}
