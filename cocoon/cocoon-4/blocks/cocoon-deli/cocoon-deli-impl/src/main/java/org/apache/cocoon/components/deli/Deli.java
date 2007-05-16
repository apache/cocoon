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
package org.apache.cocoon.components.deli;

import com.hp.hpl.deli.Profile;

import org.apache.cocoon.environment.Request;
import org.w3c.dom.Document;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * A component for providing CC/PP and UAProf support using the DELI
 * library.
 *
 * @version $Id$
 */
public interface Deli {

    String ROLE = Deli.class.getName();

    /**
     * Convert a profile stored as a vector of profile attributes
     *  to a DOM tree.
     *
     *@param	theRequest	The Request.
     *@return	The DOM tree.
     */
    Document getUACapabilities(Request theRequest)
    throws IOException, Exception;

    Profile getProfile(Request theRequest)
    throws IOException, ServletException, Exception;
}

