/*
* Copyright 1999-2004 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package javax.mail;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Folder.java,v 1.4 2004/03/06 02:25:46 antonio Exp $
 */
public class Folder {

    public static final int READ_ONLY = 1;
    public static final int READ_WRITE = 2;
    public static final int HOLDS_FOLDERS = 2;
    public static final int HOLDS_MESSAGES = 1;
    
    public void open(int mode) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public boolean isOpen() {
        throw new NoSuchMethodError("This is a mock object");
    }

    public void close(boolean b) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Message[] getMessages() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public void fetch(Message[] m, FetchProfile profile) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Folder[] list(String pattern) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public String getFullName() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Message getMessage(int id) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Message[] search(javax.mail.search.SearchTerm term) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public String getName() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public URLName getURLName() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public boolean isSubscribed() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public int getType() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public boolean hasNewMessages() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public int getMessageCount() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public int getNewMessageCount() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public int getDeletedMessageCount() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public int getUnreadMessageCount() {
        throw new NoSuchMethodError("This is a mock object");
    }
}
