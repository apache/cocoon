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

import oasis.names.tc.wsrp.v1.types.ClientData;
import oasis.names.tc.wsrp.v1.types.MarkupContext;
import oasis.names.tc.wsrp.v1.types.NamedString;
import oasis.names.tc.wsrp.v1.types.SessionContext;

import org.apache.wsrp4j.consumer.ConsumerEnvironment;
import org.apache.wsrp4j.consumer.InteractionRequest;
import org.apache.wsrp4j.consumer.MarkupRequest;
import org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl;

/**
 * Holds all parameters to communicate with the producer to get the right portlet.
 * It uses the {@link org.apache.cocoon.portal.wsrp.consumer#SimplePortletSessionImpl}
 * to get the window-information, the {@link org.apache.cocoon.portal.wsrp.consumer#Request} 
 * for the interaction-state, form-parameters and so on, last but not least the 
 * {@link org.apache.cocoon.portal.wsrp.consumer#consumerEnvironment} for all 
 * other information.<br/>
 * 
 * @version $Id$
 */
public class WSRPRequestImpl extends GenericWSRPBaseRequestImpl 
                             implements InteractionRequest, MarkupRequest {

    /** Portlet-window settings */
    protected final SimplePortletWindowSession windowSession;
    
    /** Request-parameters */
    protected final Request request;
    
    /** Consumer environment contains all registries to get the required information*/
    protected final ConsumerEnvironment consEnv;
    
	/**
     * Constructor
     * 
	 * @param session
	 * @param request
	 * @param env ConsumerEnvironment
	 */
	public WSRPRequestImpl(SimplePortletWindowSession session,
                           Request request,
                           ConsumerEnvironment env) {
		if (session == null) {
			throw(new IllegalStateException("session must not be null"));
		}
		if (env == null) {
			throw(new IllegalStateException("environment must not be null"));
		}
        if ( request == null ) {
            this.request = new RequestImpl();
        } else {
            this.request = request;
        }
		this.windowSession = session;
		this.consEnv = env;
	}
	
	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getSessionID()
	 */
	public String getSessionID() {
		SessionContext sessionCtx = windowSession.getPortletSession().getSessionContext();
		if (sessionCtx != null) {
			return sessionCtx.getSessionID();
		}
		return null;
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getPortletInstanceKey()
	 */
	public String getPortletInstanceKey() {
		return windowSession.getWindowID();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getNavigationalState()
	 */
	public String getNavigationalState() {
		return windowSession.getNavigationalState();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getWindowState()
	 */
	public String getWindowState() {
		return windowSession.getWindowState();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getMode()
	 */
	public String getMode()	{
		return this.windowSession.getMode();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getClientData()
	 */
	public ClientData getClientData() {
		return null;
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getLocales()
	 */
	public String[] getLocales() {
		return this.consEnv.getSupportedLocales();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getModes()
	 */
	public String[] getModes() {
		return this.consEnv.getSupportedModes();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getWindowStates()
	 */
	public String[] getWindowStates() {
		return this.consEnv.getSupportedWindowStates();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getMimeTypes()
	 */
	public String[] getMimeTypes() {
		return consEnv.getMimeTypes();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getCharacterEncodingSet()
	 */
	public String[] getCharacterEncodingSet() {
		return this.consEnv.getCharacterEncodingSet();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.driver.GenericWSRPBaseRequestImpl#getUserAuthentication()
	 */
	public String getUserAuthentication() {
		return this.consEnv.getUserAuthentication();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.InteractionRequest#getInteractionState()
	 */
	public String getInteractionState() {
		return this.request.getInteractionState();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.InteractionRequest#getFormParameters()
	 */
	public NamedString[] getFormParameters() {
		return this.request.getFormParameters();
	}

	/**
	 * @see org.apache.wsrp4j.consumer.MarkupRequest#getCachedMarkup()
	 */
	public MarkupContext getCachedMarkup() {
		return this.windowSession.getCachedMarkup();
	}
}
