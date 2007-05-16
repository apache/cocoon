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
package org.apache.cocoon.portal.wsrp.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import oasis.names.tc.wsrp.v1.types.NamedString;

/**
 * This class implements the Request interface and used by 
 * the swing consumer to store request related information. 
 * 
 * @version $Id$
 */
public class RequestImpl implements Request {

    /** Map to store all form params. */
    protected Map formParameters;

    /** interaction state. */
    protected String interactionState;

	/**
	 * Default constructor	  	
	 **/
    public RequestImpl() {
        this.formParameters = new HashMap();
    }
	
	/**
	* Add any parameters to the request. These parameters should
	* be carried in the form parameters field of WSRP.
	* 
	* @param name The key which identifies the parameter
	* @param value The value of the parameter
	**/
    public void addFormParameter(String name, String value) {
        this.formParameters.put(name, value);
    }
	
	/**
	 * Get all form parameters from the request. The returned
	 * <code>NamedString</code> array contains all parameter key/value pairs
	 * and can directly be passed to the form parameter field in WSRP.
	 *
	 * @return Array with all set parameters
	 **/
    public NamedString[] getFormParameters() {
        ArrayList paramList = new ArrayList();
        Iterator params = this.formParameters.keySet().iterator();
        while (params.hasNext()) {
            String name = (String)params.next();

            NamedString parameter = new NamedString();
            parameter.setName(name);
            parameter.setValue((String)this.formParameters.get(name));
            paramList.add(parameter);
        }
        
        NamedString[] formParams = new NamedString[paramList.size()];
        paramList.toArray(formParams);

        return formParams;
    }

	/**
	 * Set the interaction state of a portlet which should be passed 
	 * to the producer.
	 * 
	 * @param state the interaction state of a portlet
	 **/
    public void setInteractionState(String state) {
        this.interactionState = state;
    }
	
	/**
	* Get the interaction state of the portlet.
	* 
	* @return interaction state of a portlet carried in a request
	**/
    public String getInteractionState() {
        return this.interactionState;
    }
}
