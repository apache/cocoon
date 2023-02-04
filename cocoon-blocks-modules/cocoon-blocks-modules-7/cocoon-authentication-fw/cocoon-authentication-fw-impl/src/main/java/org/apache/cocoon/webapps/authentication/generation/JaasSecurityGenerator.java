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
package org.apache.cocoon.webapps.authentication.generation;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This Generator provides a possibility to use the Authentication Framework 
 * of Cocoon against a JAAS-Realm of an Application Server.<br>
 * You must provide following Parameters to get it running:
 * <ul><li><b>jaasRealm</b> - The JNDI name of the JAAS Realm</li>
 * <li><b>username</b> - The username of the user</li>
 * <li><b>password</b> - The password of the user</li></ul>
 * 
 * @deprecated This block is deprecated and will be removed in future versions.
 * @version $Id$
 */
public class JaasSecurityGenerator extends ServiceableGenerator {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.generation.Generator#generate()
	 */
	public void generate() throws SAXException, ProcessingException {
        String userid;
        String password;
        String jaasRealm;
        try {
            userid = this.parameters.getParameter("username");
            password = this.parameters.getParameter("password");
            jaasRealm = this.parameters.getParameter("jaasRealm", "jaas-cms-security-domain");
        } catch (ParameterException pe) {
            throw new ProcessingException("Required parameter is missing.", pe);
        }

        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("trying to login as " + userid + " on the webpage");
        }
		contentHandler.startDocument();
		contentHandler.startElement("", "authentication", "authentication", new AttributesImpl());

		try {
			LoginContext lc = new LoginContext(jaasRealm, new InternalCallbackHandler(userid, password));
			lc.login();
			Subject s = lc.getSubject();
			if (this.getLogger().isDebugEnabled()) {
				this.getLogger().debug("Subject is: " + s.getPrincipals().toString());
			}
			String principal = "";
			ArrayList roles = new ArrayList();

			Iterator it = s.getPrincipals(java.security.Principal.class).iterator();
			while (it.hasNext()) {
				Principal prp = (Principal) it.next();
				if (prp.getName().equalsIgnoreCase("Roles")) {
					Group grp = (Group) prp;
					Enumeration enumm = grp.members();
					while (enumm.hasMoreElements()) {
						Principal sg = (Principal) enumm.nextElement();
						roles.add(sg.getName());
					}
				} else {
					principal = prp.getName();
				}
			}
			lc.logout();

            XMLUtils.createElement(this.xmlConsumer, "ID", principal);
			it = roles.iterator();
			while (it.hasNext()) {
				String role = (String) it.next();
				XMLUtils.createElement(this.xmlConsumer, "role", role);
			}
			contentHandler.startElement("", "data", "data", new AttributesImpl());
			XMLUtils.createElement(this.xmlConsumer, "user", principal);
			contentHandler.endElement("", "data", "data");
            contentHandler.endElement("", "authentication", "authentication");
            contentHandler.endDocument();
		} catch (LoginException exe) {
			this.getLogger().warn("Could not login user \"" + userid + "\"");
		}
	}
	
	/**
	 * Callback Handler
	 */
	private static class InternalCallbackHandler implements CallbackHandler {
        
        private String userid;
        private String password;

        public InternalCallbackHandler(String userid, String password) {
            this.userid = userid;
            this.password = password;
        }
        
		/* (non-Javadoc)
		 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
		 */
		public void handle(Callback[] callbacks) throws UnsupportedCallbackException {

			for (int i = 0; i < callbacks.length; i++) {
				if (callbacks[i] instanceof NameCallback) {
					// prompt the user for a username
					NameCallback nc = (NameCallback) callbacks[i];
					nc.setName(userid);
				} else if (callbacks[i] instanceof PasswordCallback) {
					PasswordCallback pc = (PasswordCallback) callbacks[i];
					pc.setPassword(password.toCharArray());
				}
			}
		}
	}
}