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

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * Provides a dummy {@link TextMessage} class to serve simple text messages,
 * which means, only methods of TextMessage are implemented. Invocation of other
 * methods will be rejected by throwing a {@link JMSException.
 */
class DummyTextMessage implements TextMessage {

    /**
     * The text to store.
     */
    private String text = "";

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TextMessage#getText()
     */
    public String getText() throws JMSException {
        return text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.TextMessage#setText(java.lang.String)
     */
    public void setText(String arg0) throws JMSException {
        this.text = arg0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#acknowledge()
     */
    public void acknowledge() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#clearBody()
     */
    public void clearBody() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#clearProperties()
     */
    public void clearProperties() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getBooleanProperty(java.lang.String)
     */
    public boolean getBooleanProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getByteProperty(java.lang.String)
     */
    public byte getByteProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getDoubleProperty(java.lang.String)
     */
    public double getDoubleProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getFloatProperty(java.lang.String)
     */
    public float getFloatProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getIntProperty(java.lang.String)
     */
    public int getIntProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSCorrelationID()
     */
    public String getJMSCorrelationID() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSCorrelationIDAsBytes()
     */
    public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSDeliveryMode()
     */
    public int getJMSDeliveryMode() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSDestination()
     */
    public Destination getJMSDestination() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSExpiration()
     */
    public long getJMSExpiration() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSMessageID()
     */
    public String getJMSMessageID() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSPriority()
     */
    public int getJMSPriority() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSRedelivered()
     */
    public boolean getJMSRedelivered() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSReplyTo()
     */
    public Destination getJMSReplyTo() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSTimestamp()
     */
    public long getJMSTimestamp() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getJMSType()
     */
    public String getJMSType() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getLongProperty(java.lang.String)
     */
    public long getLongProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getObjectProperty(java.lang.String)
     */
    public Object getObjectProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getPropertyNames()
     */
    public Enumeration getPropertyNames() throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getShortProperty(java.lang.String)
     */
    public short getShortProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#getStringProperty(java.lang.String)
     */
    public String getStringProperty(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#propertyExists(java.lang.String)
     */
    public boolean propertyExists(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setBooleanProperty(java.lang.String, boolean)
     */
    public void setBooleanProperty(String arg0, boolean arg1)
            throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setByteProperty(java.lang.String, byte)
     */
    public void setByteProperty(String arg0, byte arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setDoubleProperty(java.lang.String, double)
     */
    public void setDoubleProperty(String arg0, double arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setFloatProperty(java.lang.String, float)
     */
    public void setFloatProperty(String arg0, float arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setIntProperty(java.lang.String, int)
     */
    public void setIntProperty(String arg0, int arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSCorrelationID(java.lang.String)
     */
    public void setJMSCorrelationID(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSCorrelationIDAsBytes(byte[])
     */
    public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSDeliveryMode(int)
     */
    public void setJMSDeliveryMode(int arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSDestination(javax.jms.Destination)
     */
    public void setJMSDestination(Destination arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSExpiration(long)
     */
    public void setJMSExpiration(long arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSMessageID(java.lang.String)
     */
    public void setJMSMessageID(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSPriority(int)
     */
    public void setJMSPriority(int arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSRedelivered(boolean)
     */
    public void setJMSRedelivered(boolean arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSReplyTo(javax.jms.Destination)
     */
    public void setJMSReplyTo(Destination arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSTimestamp(long)
     */
    public void setJMSTimestamp(long arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setJMSType(java.lang.String)
     */
    public void setJMSType(String arg0) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setLongProperty(java.lang.String, long)
     */
    public void setLongProperty(String arg0, long arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setObjectProperty(java.lang.String,
     *      java.lang.Object)
     */
    public void setObjectProperty(String arg0, Object arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setShortProperty(java.lang.String, short)
     */
    public void setShortProperty(String arg0, short arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.jms.Message#setStringProperty(java.lang.String,
     *      java.lang.String)
     */
    public void setStringProperty(String arg0, String arg1) throws JMSException {
        throw new JMSException("Not implemented!");
    }

}
