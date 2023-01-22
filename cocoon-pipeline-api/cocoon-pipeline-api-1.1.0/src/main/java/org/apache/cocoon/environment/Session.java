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
package org.apache.cocoon.environment;

import javax.servlet.http.HttpSession;

/**
 * For Cocoon 2.2 the return type of {@link org.apache.cocoon.environment.Request#getSession()}
 * was changed to {@link javax.servlet.HttpSession}.
 * In order to allow for a smooth migration, Cocoon 2.1.11 added the
 * {@link org.apache.cocoon.environment.Request#getCocoonSession()} method returning the old
 * Session interface type.
 *
 * <pre>
 *   // Works for 2.1.x:
 *   Session session = request.getSession();
 *
 *   // Works for 2.1.11+ and 2.2.x:
 *   Session session = request.getCocoonSession();
 *
 *   // Works for 2.2.x and later:
 *   HttpSession session = request.getSession();
 * </pre>
 *
 * @see javax.servlet.HttpSession
 * @deprecated This interface is deprecated and will be removed in future versions.
 * @version $Id$
 */

public interface Session extends HttpSession {
}
