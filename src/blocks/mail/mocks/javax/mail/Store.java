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
 * @version CVS $Id:
 */
public class Store {
    
    public void connect() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public boolean isConnected() {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public void close() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Folder getDefaultFolder() throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }

    public Folder getFolder(String name) throws MessagingException {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public void connect(String host, String user, String password) {
        throw new NoSuchMethodError("This is a mock object");
    }

    public void connect(String host, int port, String user, String password) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
}
