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

import java.util.Properties;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: Session.java,v 1.5 2004/03/06 02:25:46 antonio Exp $
 */

public class Session {
    
    public static Session getDefaultInstance(Properties props) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public static Session getDefaultInstance(Properties props, Authenticator auth) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Store getStore(URLName name) {
        throw new NoSuchMethodError("This is a mock object");
    }
    
    public Store getStore(String protocol) {
        throw new NoSuchMethodError("This is a mock object");
    }

    public Provider[] getProviders() {
        throw new NoSuchMethodError("This is a mock object");
    }

    public Transport getTransport(String name) {
        throw new NoSuchMethodError("This is a mock object");
    }
}
