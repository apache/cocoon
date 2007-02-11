/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.notification;

import java.util.HashMap;
import java.util.Map;

/**
 *  A simple bean implementation of Notifying.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @version CVS $Id: SimpleNotifyingBean.java,v 1.1 2003/03/09 00:09:07 pier Exp $
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
