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
package com.sun.jdori.common;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version CVS $Id: PersistenceManagerFactoryImpl.java,v 1.2 2004/03/06 02:25:58 antonio Exp $
 */

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.PersistenceManager;

public class PersistenceManagerFactoryImpl implements PersistenceManagerFactory {

    public PersistenceManager getPersistenceManager() {
        return null;
    }

}
 
