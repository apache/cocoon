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

import java.util.Map;

import org.apache.avalon.framework.component.Component;

/**
 *  Generates an Notifying representation of widely used objects.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @version CVS $Id: NotifyingBuilder.java,v 1.2 2004/03/05 13:02:49 bdelacretaz Exp $
 */

public interface NotifyingBuilder extends Component{

  /**
  * The role implemented by a <code>NotifyingBuilder</code>.
  */
  String ROLE = NotifyingBuilder.class.getName();

  /** Builds a Notifying object (SimpleNotifyingObject in this case)
   *  that tries to explain what the Object o can reveal.
   * @param sender who sent this Object.
   * @param o the object to use when building the SimpleNotifyingObject
   * @return the  Notifying Object that was build
   * @see org.apache.cocoon.components.notification.Notifying
   */
  Notifying build(Object sender, Object o);


  /** Builds a Notifying object (SimpleNotifyingObject in this case)
   *  that explains a notification.
   * @param sender who sent this Object.
   * @param o the object to use when building the SimpleNotifyingObject
   * @param type see the Notifying apidocs
   * @param title see the Notifying apidocs
   * @param source see the Notifying apidocs
   * @param message see the Notifying apidocs
   * @param description see the Notifying apidocs
   * @param extra see the Notifying apidocs
   * @return the  Notifying Object that was build
   * @see org.apache.cocoon.components.notification.Notifying
   */
  Notifying build(Object sender, Object o, String type, String title,
          String source, String message, String description, Map extra);
}


