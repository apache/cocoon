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

import org.apache.avalon.framework.CascadingRuntimeException;

import java.util.Map;

/**
 * A CascadingRuntimeException that is also Notifying.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @version CVS $Id: NotifyingCascadingRuntimeException.java,v 1.2 2004/03/05 13:02:49 bdelacretaz Exp $
 */
public class NotifyingCascadingRuntimeException
  extends CascadingRuntimeException
    implements Notifying{

  /**
   * The Notifying Object used internally to keep Notifying fields
   */
  Notifying n;

  /**
   * Construct a new <code>NotifyingCascadingRuntimeException</code> instance.
   */
  public NotifyingCascadingRuntimeException(String message) {
    super(message, null);
    n = new DefaultNotifyingBuilder().build(this, message);
  }

  /**
   * Creates a new <code>ProcessingException</code> instance.
   *
   * @param ex an <code>Exception</code> value
   */
  public NotifyingCascadingRuntimeException(Exception ex) {
    super(ex.getMessage(), ex);
    n = new DefaultNotifyingBuilder().build(this, ex);
  }

  /**
   * Construct a new <code>ProcessingException</code> that references
   * a parent Exception.
   */
  public NotifyingCascadingRuntimeException(String message, Throwable t) {
    super(message, t);
    n = new DefaultNotifyingBuilder().build(this, t);
  }

  /**
   *  Gets the Type attribute of the Notifying object
   */
  public String getType() {
    return n.getType();
  }

  /**
   *  Gets the Title attribute of the Notifying object
   */
  public String getTitle() {
    return n.getTitle();
  }

  /**
   *  Gets the Source attribute of the Notifying object
   */
  public String getSource() {
    return n.getSource();
  }

  /**
   *  Gets the Sender attribute of the Notifying object
   */
  public String getSender() {
    return n.getSender();
  }

  /**
   *  Gets the Message attribute of the Notifying object
   */
  public String getMessage() {
    return n.getMessage();
  }

  /**
   *  Gets the Description attribute of the Notifying object
   */
  public String getDescription() {
    return n.getDescription();
  }

  /**
   *  Gets the ExtraDescriptions attribute of the Notifying object
   */
  public Map getExtraDescriptions() {
    return n.getExtraDescriptions();
  }

}

