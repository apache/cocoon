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

import java.util.Collections;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.jms.AbstractMessagePublisher;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.springframework.jms.core.MessageCreator;

/**
 * Action to publish TextMessages to a given JMS {@link Destination}, which could be ether a Topic or a Queue. For
 * description of static parameter configuration see {@link org.apache.cocoon.components.jms.AbstractMessagePublisher}
 * 
 * <p>
 * Sitemap parameters:
 * </p>
 * <table border="1"> <tbody>
 * <tr>
 * <th align="left">parameter</th>
 * <th align="left">required</th>
 * <th align="left">default</th>
 * <th align="left">description</th>
 * </tr>
 * <tbody>
 * <tr>
 * <td>message</td>
 * <td>required</td>
 * <td>&nbsp;</td>
 * <td>Content of TextMessage to publish</td>
 * </tr>
 * </tbody> </table>
 */
public class JMSPublisherAction extends AbstractMessagePublisher implements Action {

    /**
     * Action parameter name.
     */
    private static final String MESSAGE_PARAM = "message";

    /**
     * Default constructor.
     */
    public JMSPublisherAction() {
        super();
    }

    public Map act(Redirector redirector, SourceResolver resolver, Map objectModel, String source, Parameters parameters)
            throws Exception {

        Map result = null;
        try {
            // Get message.
            final String msg = parameters.getParameter(MESSAGE_PARAM);

            MessageCreator creator = new MessageCreator() {
                public Message createMessage(Session session) throws JMSException {
                    String text = msg;
                    TextMessage message = session.createTextMessage(text);
                    return message;
                }
            };

            // Publish the message.
            this.template.send(this.destination, creator);

            result = Collections.EMPTY_MAP;
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Error delivering message.", e);
            }
        }

        return result;
    }
}