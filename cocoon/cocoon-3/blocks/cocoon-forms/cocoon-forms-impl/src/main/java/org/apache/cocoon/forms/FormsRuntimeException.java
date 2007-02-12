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
package org.apache.cocoon.forms;

import org.apache.cocoon.util.location.LocatedRuntimeException;
import org.apache.cocoon.util.location.Location;

/**
 * An exception raised when some runtime error occurs in form handling. This is a
 * located exception, which points to the relevant element declaration.
 *
 * @version $Id$
 */
public class FormsRuntimeException extends LocatedRuntimeException {

    public FormsRuntimeException(String message) {
        super(message);
    }

    public FormsRuntimeException(String message, Throwable cause)
    throws LocatedRuntimeException {
        super(message, cause);
    }

    public FormsRuntimeException(String message, Location location) {
        super(message, location);
    }

    public FormsRuntimeException(String message, Throwable cause, Location location)
    throws LocatedRuntimeException {
        super(message, cause, location);
    }
}
