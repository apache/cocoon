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
package org.apache.cocoon.servlet;

import java.io.File;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;

import org.apache.commons.jci.listeners.ReloadingListener;

public class CocoonReloadingListener extends ReloadingListener {

    private static boolean reload = false;
//    private static List subscribers = new ArrayList();
    
    public CocoonReloadingListener(File file) {
        super(file);
    }

    public void onChangeFile(File changedFile) {
        String changedFilePath = changedFile.getAbsolutePath().replace('\\', '/');
        String changedFileParent = changedFile.getParent().replace('\\', '/');
        
        
        System.out.println("A file changed: " + changedFile.getAbsolutePath());
        System.out.println("Parent: " + changedFile.getParent());
        super.onChangeFile(changedFile);

        
        // TODO be more specific when to reload. Not every change needs a reload of the Spring
        // application context
        if(changedFileParent.endsWith("META-INF/cocoon/spring") ||
                changedFilePath.endsWith(".xmap") ||
                changedFilePath.endsWith(".xmap.xml") ||
                changedFileParent.endsWith("config/spring")) {
            System.out.println("File change detected: " + changedFile);       
            reload = true;            
        }
        
//        notifySubscribers();        

    }

    public void onChangeDirectory(File changedDirectory) {
        super.onChangeDirectory(changedDirectory);
//        notifySubscribers();        
    }    
    
    public static synchronized boolean isReload() {
        if(reload == true) {
            reload = false;
            return true;
        }
        return reload;
    }
//    
//    private void notifySubscribers() {
//        for(Iterator nIt = subscribers.iterator(); nIt.hasNext(); ) {
//            ((ReloadingNotificationSubscriber) nIt.next()).handleNotification();
//        }
//    }
//    
//    public static void subscribe(ReloadingNotificationSubscriber subscriber) {
//        subscribers.add(subscriber);
//    }
    
}
