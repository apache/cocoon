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

import java.util.Map;

import org.apache.avalon.framework.component.Component;

/**
 *  Generates an Notifying representation of widely used objects.
 *
 * @author <a href="mailto:barozzi@nicolaken.com">Nicola Ken Barozzi</a>
 * @version CVS $Id: NotifyingBuilder.java,v 1.1 2003/03/09 00:09:07 pier Exp $
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


