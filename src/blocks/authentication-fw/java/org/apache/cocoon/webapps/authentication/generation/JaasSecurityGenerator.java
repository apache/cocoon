/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.webapps.authentication.generation;

import java.security.Principal;
import java.security.acl.Group;
import java.util.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.generation.ServiceableGenerator;
import org.apache.cocoon.webapps.session.ContextManager;
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
 * @version $Id: PortalJaasSecurityGenerator.java 96 2004-08-28 21:30:23Z kulawik $
 */
public class JaasSecurityGenerator extends ServiceableGenerator {
    
	private String userid;
	private String password;
	private String jaasRealm = "jaas-cms-security-domain";

	/* (non-Javadoc)
	 * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
	 */
	public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par) {
		if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("begin setup");
        }
		try {
			super.setup(resolver, objectModel, src, par);
			ContextManager cm = (ContextManager) this.manager.lookup(ContextManager.ROLE);
			try {
				if (cm.hasSessionContext()) {
					cm.deleteContext("authentication");
				}
			} catch (Exception exe) {
			}
			userid = par.getParameter("username");
			password = par.getParameter("password");
			try {
				String jaasRealmTmp = par.getParameter("jaasRealm", null);
				if (jaasRealmTmp != null && !jaasRealmTmp.equalsIgnoreCase("")) {
					jaasRealm = jaasRealmTmp;
				}
			} catch (Exception se) {
			}
			if (this.getLogger().isDebugEnabled()) {
				this.getLogger().debug("trying to login as " + userid + " on the webpage");
			}
		} catch (Exception ex) {
			new ProcessingException(ex.getMessage());
		}
		if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("end setup");
        }
	}

	public void addTextNode(String nodeName, String text) throws SAXException {
		contentHandler.startElement("", nodeName, nodeName, new AttributesImpl());
		contentHandler.characters(text.toCharArray(), 0, text.length());
		contentHandler.endElement("", nodeName, nodeName);
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.generation.Generator#generate()
	 */
	public void generate() throws SAXException, ProcessingException {
		if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug("begin generate");
        }
		contentHandler.startDocument();
		contentHandler.startElement("", "authentication", "authentication", new AttributesImpl());

		try {
			LoginContext lc = new LoginContext(jaasRealm, new InternalCallbackHandler());
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

			addTextNode("ID", principal);
			it = roles.iterator();
			while (it.hasNext()) {
				String role = (String) it.next();
				addTextNode("role", role);
			}
			contentHandler.startElement("", "data", "data", new AttributesImpl());
			addTextNode("user", principal);
			contentHandler.endElement("", "data", "data");
		} catch (Exception exe) {
			this.getLogger().warn("Could not login user \"" + userid + "\"");
		} finally {
			contentHandler.endElement("", "authentication", "authentication");
			contentHandler.endDocument();
			if (this.getLogger().isDebugEnabled()) {
                this.getLogger().debug("end generate");
            }
		}
	}
	
	/**
	 * Callback Handler
	 */
	private class InternalCallbackHandler implements CallbackHandler {
        
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