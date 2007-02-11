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
package org.apache.cocoon.components.notification;

import java.util.HashMap;
import java.util.Map;

/**
 *  A simple bean implementation of Notifying.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @version CVS $Id: SimpleNotifyingBean.java,v 1.2 2004/03/05 13:02:49 bdelacretaz Exp $
 */
public class SimpleNotifyingBean implements Notifying {

    /**
     * The type of the notification. Examples can be "warning" or "error"
     */
    private String type = "unknown";

    /**
     * The title of the notification.
     */
    private String title = "";

    /**
     * The source that generates the notification.
     */
    private String source = "";

    /**
     * The sender of the notification.
     */
    private String sender = "";

    /**
     * The notification itself.
     */
    private String message = "Message not known.";

    /**
     * A more detailed description of the notification.
     */
    private String description = "No details available.";

    /**
     * A HashMap containing extra notifications
     */
    private Map extraDescriptions = new HashMap();


    public SimpleNotifyingBean() {
        this.sender = "unknown";
        setSource(this.getClass().getName());
        setTitle("Generic notification");
    }

    public SimpleNotifyingBean(Object sender) {
        this.sender = sender.getClass().getName();
        setSource(this.getClass().getName());
        setTitle("Generic notification");
    }

    /**
     * Sets the Type of the SimpleNotifyingBean object
     *
     * @param  type  The new Type value
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sets the Title of the SimpleNotifyingBean object
     *
     * @param  title  The new Title value
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets the Source of the SimpleNotifyingBean object
     *
     * @param  source  The new Source value
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Sets the Sender of the SimpleNotifyingBean object
     *
     * @param  sender  The new sender value
    private void setSender(Object sender) {
        this.sender = sender.getClass().getName();
    }
     */

    /**
     * Sets the Sender of the SimpleNotifyingBean object
     *
     * @param  sender  The new sender value
    private void setSender(String sender) {
        this.sender = sender;
    }
     */

    /**
     * Sets the Message of the SimpleNotifyingBean object
     *
     * @param  message  The new Message value
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the Description of the SimpleNotifyingBean object
     *
     * @param  description  The new Description value
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Adds the ExtraDescription to the SimpleNotifyingBean object
     *
     * @param  extraDescriptionDescription  The additional ExtraDescription name
     * @param  extraDescription The additional ExtraDescription value
     */
    public void addExtraDescription(String extraDescriptionDescription,
                                    String extraDescription) {
        this.extraDescriptions.put(extraDescriptionDescription, extraDescription);
    }

    /**
     * Replaces the ExtraDescriptions of the SimpleNotifyingBean object
     */
    protected void replaceExtraDescriptions(Map extraDescriptions) {
        this.extraDescriptions = extraDescriptions;
    }

    /**
     * Adds the ExtraDescriptions to the SimpleNotifyingBean object
     */
    protected void addExtraDescriptions(Map extraDescriptions) {
        if (this.extraDescriptions == null) {
            replaceExtraDescriptions(extraDescriptions);
        } else {
            this.extraDescriptions.putAll(extraDescriptions);
        }
    }

    /**
     * Gets the Type of the SimpleNotifyingBean object
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the Title of the SimpleNotifyingBean object
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the Source of the SimpleNotifyingBean object
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the Sender of the SimpleNotifyingBean object
     */
    public String getSender() {
        return sender;
    }

    /**
     * Gets the Message of the SimpleNotifyingBean object
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the Description of the SimpleNotifyingBean object
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the ExtraDescriptions of the SimpleNotifyingBean object
     */
    public Map getExtraDescriptions() {
        return extraDescriptions;
    }
}
