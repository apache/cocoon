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
package org.apache.cocoon.components.naming;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchResult;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 *	The <code>LDAPEntryManager</code> is an Avalon Component for managing Entries in a Javax Naming Directory.
 *	This is the LDAP implementation of the {@link org.apache.cocoon.components.naming.EntryManager EntryManager} interface.
 *  This is designed to be used from FlowScript, it uses Maps instead of NamingEnumerations and Attributes.
 *
 *   Example configuration (goes in cocoon.xconf)
 *   <pre><tt>
 *	&lt;component role="org.apache.cocoon.component.EntryManager" class="org.apache.cocoon.components.naming.LDAPEntryManager" logger="flow.ldap"&gt;
 *	  &lt;parameter name="ldap-host" value="hostname:port"/&gt;
 *	  &lt;parameter name="ldap-base" value="dc=example,dc=com"/&gt;
 *	  &lt;parameter name="ldap-user" value="username"/&gt;
 *	  &lt;parameter name="ldap-pass" value="password"/&gt;
 *  &lt;/component&gt;
 *   </tt></pre></p>
 *
 * @version $Id$
 */
public class LDAPEntryManager extends AbstractLogEnabled
	                          implements EntryManager, Parameterizable, Disposable,
                                         Recyclable {

	/* congiguration parameter names */
	protected final static String LDAP_HOST_PARAM = "ldap-host";
	protected final static String LDAP_USER_PARAM = "ldap-user";
	protected final static String LDAP_PASS_PARAM = "ldap-pass";
	protected final static String LDAP_BASE_PARAM = "ldap-base";
	
	/* internal state */
	private boolean disposed;
	
	/* internal instance variables */
	protected DirContext context;
	protected Hashtable environment;

    
	/** Avalon, Parameterize this Class */
	public void parameterize(Parameters params) throws ParameterException {
        String host = params.getParameter(LDAP_HOST_PARAM);
        String base = params.getParameter(LDAP_BASE_PARAM);
        String user = params.getParameter(LDAP_USER_PARAM);
        String pass = params.getParameter(LDAP_PASS_PARAM);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Using LDAP host: " + host + ", base: " + base +
                              ", user: " + user + ", pass: " + pass);
        }
        this.environment = new Hashtable();
        this.environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        this.environment.put(Context.PROVIDER_URL, host + "/" + base);
        if (user != null) {
            this.environment.put(Context.SECURITY_AUTHENTICATION, "simple");
            this.environment.put(Context.SECURITY_PRINCIPAL, user);
            this.environment.put(Context.SECURITY_CREDENTIALS, pass);
        }
	}

	/* Avalon, Recycle this Class */
	public final void recycle()	 {
        try {
            this.context.close();
        } catch (Exception e) {
            getLogger().error("Exception in recycle", e);
        } finally {
            this.context = null;
        }
    }

	/* Avalon, Dispose of this Class */
	public final void dispose()	 {
        try {
            if (context != null) {
                this.context.close();
            }
        } catch (Exception e) {
            getLogger().error("Exception in dispose", e);
        } finally {
            this.context = null;
            this.environment = null;
            this.disposed = true;
        }
    }

	/* lazy initialise this class */
	protected void initialize() throws Exception {
        if (this.disposed) {
            throw new IllegalStateException("initialize() : Already disposed");
        }
        this.context = new InitialDirContext(this.environment);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New context: " + this.context.getNameInNamespace());
        }
    }


    /**
     * Creates a new Entry
     *
     * @param name       The name of the Entry to create
     * @param attributes The Map of Attributes to create it with
     */
    public void create(String name, Map attributes) throws ProcessingException {
        Context newContext = null;
        try {
            if (this.context == null) {
                initialize();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Creating new Context: " + name);
            }
            newContext = context.createSubcontext(name, map2Attributes(attributes));
        } catch (Exception e) {
            throw new ProcessingException(e);
        } finally {
            try {
                if (newContext != null) {
                    newContext.close();
                }
            } catch (NamingException e) {
                throw new ProcessingException(e);
			}
		}
	}

    /**
     * Retrieves a named Entry's Attributes
     *
     * @param name The name of the Entry to modify
     * @return a Map of the Attributes
     */
    public Map get(String name) throws ProcessingException {
        try {
            if (this.context == null) {
                initialize();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Retrieving Entry: " + name);
            }
            return attributes2Map(context.getAttributes(name));
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    /**
     * Finds Entries based on matching their Attributes
     *
     * @param attributes The Attributes to match
     * @return a Map of the results, each with a Map of their Attributes
     */
    public Map find(Map attributes) throws ProcessingException {
        return find("", attributes);
    }

    /**
     * Finds Entries based on their Attributes
     *
     * @param cntx       The sub-context to search
     * @param attributes The Attributes to match
     * @return a Map of the results, each with a Map of their Attributes
     */
    public Map find(String cntx, Map attributes) throws ProcessingException {
        try {
            if (this.context == null) {
                initialize();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Finding Entries in: " + cntx);
            }
            return namingEnumeration2Map(context.search(cntx, map2Attributes(attributes)));
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    /**
     * Modifies an existing Entry
     *
     * @param name       The name of the Entry to modify
     * @param mod_op     The modification mode to use
     * @param attributes The Map of modifications
     */
    public void modify(String name, int mod_op, Map attributes) throws ProcessingException {
        try {
            if (this.context == null) initialize();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Modifying Entry: " + name);
            }
            context.modifyAttributes(name, mod_op, map2Attributes(attributes));
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    /*
         Converts an Attributes Enumeration into a Map of those Attributes
         Should be easier to manupulate in FlowScript and display in JXTemplate
         Keep in mind that because there can be many entries for each Attribute
         we store each value of the Map as an Array
     */
    private Map attributes2Map(Attributes attributes) throws NamingException {
        Map map = new HashMap();
        for (NamingEnumeration atts = attributes.getAll(); atts.hasMore();) {
            Attribute attr = (Attribute) atts.next();
            String id = attr.getID();
            List val = new java.util.ArrayList();
            NamingEnumeration vals = attr.getAll();
            while (vals.hasMore()) {
                val.add(vals.next());
            }
            map.put(id, val);
        }
        return map;
    }

    /*
         Converts a Map into an Enumeration of Attributes
         Should be easier to provide from FlowScript
     */
    private Attributes map2Attributes(Map map) {
        Attributes attrs = new BasicAttributes(false);
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            Iterator vals = ((List) map.get(key)).iterator();
            Attribute attr = new BasicAttribute(key);
            while (vals.hasNext()) {
                attr.add(vals.next());
            }
            attrs.put(attr);
        }
        return attrs;
    }

    /*
         Converts a NamingEnumeration into a Map of those Entries, with Attributes
         Should be easier to manupulate in FlowScript and display in JXTemplate
     */
    private Map namingEnumeration2Map(NamingEnumeration enumeration) throws NamingException {
        Map map = new HashMap();
        while (enumeration.hasMore()) {
            SearchResult sr = (SearchResult) enumeration.next();
            map.put(sr.getName (), attributes2Map(sr.getAttributes ()));
		}
		return map;
	}
}
