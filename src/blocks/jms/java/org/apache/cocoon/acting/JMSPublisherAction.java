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
package org.apache.cocoon.acting;

import java.util.Collections;
import java.util.Map;

import javax.jms.Message;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import org.apache.cocoon.components.jms.AbstractMessagePublisher;

/**
 * Action to publish TextMessages to a JMS Topic. For description of static
 * parameter configuration see {@link org.apache.cocoon.components.jms.AbstractMessagePublisher}
 * 
 * <p>Sitemap parameters:</p>
 * <table border="1">
 *  <tbody>
 *   <tr>
 *     <th align="left">parameter</th>
 *     <th align="left">required</th>
 *     <th align="left">default</th>
 *     <th align="left">description</th>
 *   </tr>
 *  <tbody>
 *   <tr>
 *     <td>message</td>
 *     <td>required</td>
 *     <td>&nbsp;</td>
 *     <td>Content of TextMessage to publish</td>
 *   </tr>
 *  </tbody>
 * </table>
 */
public class JMSPublisherAction extends AbstractMessagePublisher implements Action, ThreadSafe {

    // ---------------------------------------------------- Constants

    private static final String MESSAGE_PARAM = "message";

    // ---------------------------------------------------- Lifecycle

    public JMSPublisherAction () {
    }

    // ---------------------------------------------------- Action

    public Map act(Redirector redirector,
                   SourceResolver resolver,
                   Map objectModel,
                   String source,
                   Parameters parameters) throws Exception {

        Map result = null;
        try {
            // publish the message
            final String event = parameters.getParameter(MESSAGE_PARAM);
            final Message message = m_session.createTextMessage(event);
            publishMessage(message);
            result = Collections.EMPTY_MAP;
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Error delivering message.", e);
            }
        }

        return result;
    }

}
