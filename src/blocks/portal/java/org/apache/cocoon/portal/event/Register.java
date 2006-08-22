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
package org.apache.cocoon.portal.event;


/**
 * <tt>Register</tt> allows a <tt>Subscriber</tt> to subscribe to 
 * and unsubscribe from <tt>EventManager</tt>.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @author Mauro Talevi
 * @deprecated Use {@link org.apache.cocoon.portal.event.EventManager#subscribe(Receiver)} instead.
 *
 * @version CVS $Id$
 */
public interface Register {
    
     /**
      * Subscribes a Subscriber to the EventManager.  
      * The Subscriber abstracts all the information needed for the subscription.
      * @param subscriber the Subscriber
      * @see Subscriber
      */
     void subscribe( Subscriber subscriber );
    
     /**
      * Unsubscribes an Subscriber from the EventManager.
      * @param subscriber the Subscriber 
      * @see Subscriber
      */
     void unsubscribe( Subscriber subscriber );
     
} 


