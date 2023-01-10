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
package org.apache.cocoon.util.location;

/**
 * Extension of {@link Locatable} for exceptions.
 * <p>
 * In order to dump location information in the stacktrace, the <code>getMessage()</code> method of
 * a {@link Locatable} exception should return a concatenation of the raw message (given in the
 * constructor) and the exception's location, e.g. "<code>foo failed (file.xml:12:3)</code>". However,
 * {@link Locatable}-aware classes will want to handle the raw message (i.e. "<code>foo failed</code>")
 * and location separately. This interface gives access to the raw message.
 * <p>
 * <strong>Note:</strong> care should be taken for locatable exceptions to use only immutable and
 * serializable implementations of {@link Location}
 *
 * @see org.apache.cocoon.util.location.LocationImpl#get(Location)
 * @since 2.1.8
 * @version $Id$
 */
public interface LocatableException extends Locatable {

    /**
     * Get the raw message of the exception (the one used in the constructor)
     *
     * @return the raw message
     */
    public String getRawMessage();
}
