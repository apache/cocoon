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
package org.apache.cocoon.classloader.reloading;

// TODO rcl
//import org.apache.commons.jci.listeners.NotificationListener;
//import org.apache.commons.jci.stores.ResourceStore;

/**
 * Wraps all the stores configured into the sitemap classloaders, in order to dispatch 
 * the notification event to the treeprocessor and force the component reloading in cocoon
 * TODO Extend TransactionalResourceStore, if store is not private.
 *
 * @version $Id$
 */
public class NotifyingResourceStore { // TODO rcl implements ResourceStore {

//    TODO rcl    
//    private NotificationListener listener;
//
//    public NotifyingResourceStore(NotificationListener l) {
//        this.listener = l;
//    }
//    
//    public byte[] read(String pResourceName) {
//        return null;
//    }
//
//    public void remove(String pResourceName) {
//    }
//
//    public void write(String pResourceName, byte[] pResourceData) {
//        this.listener.handleNotification();
//    }
}
