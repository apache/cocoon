/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.util.log;

import org.apache.avalon.framework.logger.Logger;

/**
 * This class provides static accessors for a special "deprecation" logger.
 * All deprecated code should use this logger to log warnings into the 
 * deprecation log. This makes it easier for users to find out if they're 
 * using deprecated stuff.
 *
 * @version $Id$
 */
public class DeprecationLogger {

    /** This is the logger used to log the warn messages.
     *  THIS IS AN INTERNAL FIELD, DON'T USE IT DIRECTLY.
     */
    public static Logger logger;
    
    public static void log(String message) {
        logger.warn(message);
    }
}
