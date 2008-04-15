/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.excalibur.source;

import java.io.Serializable;

/**
 * A <code>SourceValidity</code> object contains all information to check if a Source
 * object is still valid.
 * <p>
 * There are two possibilities:
 * <ul>
 * <li>The validity object has all information to check by itself if it is valid
 *     (e.g. given an expires date).</li>
 * <li>The validity object possibility needs another (newer) validity object to compare
 *     against (e.g. to test a last modification date).</li>
 * </ul>
 * To avoid testing what the actual implementation of the validity object supports,
 * the invocation order is to first call {@link #isValid()} and only if this result
 * is <code>0</code> (i.e. "don't know"), then to call {@link #isValid(SourceValidity)}.
 * <p>
 * Remember to call {@link #isValid(SourceValidity)} when {@link #isValid()} returned
 * <code>0</code> !
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public interface SourceValidity
    extends Serializable
{
    final int VALID   = +1;
    final int INVALID = -1;
    final int UNKNOWN = 0;

    /**
     * Check if the component is still valid. The possible results are :
     * <ul>
     * <li><code>-1</code>: invalid. The component isn't valid anymore.</li>
     * <li><code>0</code>: don't know. This validity should be checked against a new
     *     validity object using {@link #isValid(SourceValidity)}.</li>
     * <li><code>1</code>: valid. The component is still valid.</li>
     * </ul>
     */
    int isValid();

    /**
     * Check if the component is still valid. This is only true if the incoming Validity
     * is of the same type and has the "same" values.
     * <p>
     * The invocation order is that the isValid
     * method of the old Validity object is called with the new one as a
     * parameter.
     * @return -1 is returned, if the validity object is not valid anymore
     *          +1 is returned, if the validity object is still valid
     *          0  is returned, if the validity check could not be performed.
     *             In this case, the new validity object is not usable. Examples
     *             for this are: when the validity objects have different types,
     *             or when one validity object for any reason is not able to
     *             get the required information.
     */
    int isValid( SourceValidity newValidity );
}
