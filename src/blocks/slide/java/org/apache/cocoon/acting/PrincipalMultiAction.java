/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.acting;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.slide.Principal;
import org.apache.cocoon.components.slide.PrincipalGroup;
import org.apache.cocoon.components.slide.PrincipalProvider;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;

/**
 * Multiple actions for to add, to removing and to modify principals or principal groups.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: PrincipalMultiAction.java,v 1.4 2003/12/02 19:18:46 unico Exp $
 */ 
public class PrincipalMultiAction extends AbstractMultiAction implements ThreadSafe {

    public final static String CALLER_PRINCIPAL_NAME = "cocoon-caller-principal-name";
    public final static String CALLER_PRINCIPAL_PASSWORD = "cocooon-caller-principal-password";

    public final static String PRINCIPAL_PROVIDER = "cocoon-principal-provider";

    public final static String PRINCIPAL_NAME = "cocoon-principal-name";
    public final static String PRINCIPAL_ROLE = "cocoon-principal-role";
    public final static String PRINCIPAL_PASSWORD = "cocoon-principal-password";

    public final static String PRINCIPAL_GROUP_NAME = "cocoon-principal-group-name";

    public Map doAddPrincipal(Redirector redirector,
                              SourceResolver resolver,
                              Map objectModel,
                              String src,
                              Parameters parameters) throws Exception {

        getLogger().debug("add principal called");

        Request request = ObjectModelHelper.getRequest(objectModel);

        String principal_provider = parameters.getParameter(PRINCIPAL_PROVIDER,
            request.getParameter(PRINCIPAL_PROVIDER));

        String caller_principal_name = parameters.getParameter(CALLER_PRINCIPAL_NAME,
            request.getParameter(CALLER_PRINCIPAL_NAME));

        String caller_principal_password = parameters.getParameter(CALLER_PRINCIPAL_PASSWORD,
            request.getParameter(CALLER_PRINCIPAL_PASSWORD));

        String principal_name = parameters.getParameter(PRINCIPAL_NAME,
            request.getParameter(PRINCIPAL_NAME));

        String principal_role = parameters.getParameter(PRINCIPAL_ROLE,
            request.getParameter(PRINCIPAL_ROLE));

        String principal_password = parameters.getParameter(PRINCIPAL_PASSWORD,
            request.getParameter(PRINCIPAL_PASSWORD));

        ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(principal_provider);

            Principal caller = new Principal(caller_principal_name, caller_principal_password);
            Principal principal = new Principal(principal_name, principal_role, principal_password);

            principalprovider.addPrincipal(caller, principal);
 
        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup for service.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
        return EMPTY_MAP;
    }

    public Map doRemovePrincipal(Redirector redirector,
                                 SourceResolver resolver,
                                 Map objectModel,
                                 String src,
                                 Parameters parameters) throws Exception {

        getLogger().debug("remove principal called");

        Request request = ObjectModelHelper.getRequest(objectModel);

        String principal_provider = parameters.getParameter(PRINCIPAL_PROVIDER,
            request.getParameter(PRINCIPAL_PROVIDER));

        String caller_principal_name = parameters.getParameter(CALLER_PRINCIPAL_NAME,
            request.getParameter(CALLER_PRINCIPAL_NAME));

        String caller_principal_password = parameters.getParameter(CALLER_PRINCIPAL_PASSWORD,
            request.getParameter(CALLER_PRINCIPAL_PASSWORD));

        String principal_name = parameters.getParameter(PRINCIPAL_NAME,
            request.getParameter(PRINCIPAL_NAME));

        ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(principal_provider);

            Principal caller = new Principal(caller_principal_name, caller_principal_password);
            Principal principal = new Principal(principal_name);

            principalprovider.removePrincipal(caller, principal);

        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup for service.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
        return EMPTY_MAP;
    }

    public Map doAddPrincipalGroup(Redirector redirector,
                                   SourceResolver resolver,
                                   Map objectModel,
                                   String src,
                                   Parameters parameters) throws Exception {

        getLogger().debug("add principal group called");

        Request request = ObjectModelHelper.getRequest(objectModel);

        String principal_provider = parameters.getParameter(PRINCIPAL_PROVIDER,
            request.getParameter(PRINCIPAL_PROVIDER));

        String caller_principal_name = parameters.getParameter(CALLER_PRINCIPAL_NAME,
            request.getParameter(CALLER_PRINCIPAL_NAME));

        String caller_principal_password = parameters.getParameter(CALLER_PRINCIPAL_PASSWORD,
            request.getParameter(CALLER_PRINCIPAL_PASSWORD));

        String principal_group_name = parameters.getParameter(PRINCIPAL_GROUP_NAME,
            request.getParameter(PRINCIPAL_GROUP_NAME));

            ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(principal_provider);

            Principal caller = new Principal(caller_principal_name, caller_principal_password);
            PrincipalGroup principalgroup = new PrincipalGroup(principal_group_name);

            principalprovider.addPrincipalGroup(caller, principalgroup);

        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup for service.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
        return EMPTY_MAP;
    }

    public Map doRemovePrincipalGroup(Redirector redirector,
                                      SourceResolver resolver,
                                      Map objectModel,
                                      String src,
                                      Parameters parameters) throws Exception {

        getLogger().debug("remove principal group called");

        Request request = ObjectModelHelper.getRequest(objectModel);

        String principal_provider = parameters.getParameter(PRINCIPAL_PROVIDER,
            request.getParameter(PRINCIPAL_PROVIDER));

        String caller_principal_name = parameters.getParameter(CALLER_PRINCIPAL_NAME,
            request.getParameter(CALLER_PRINCIPAL_NAME));

        String caller_principal_password = parameters.getParameter(CALLER_PRINCIPAL_PASSWORD,
            request.getParameter(CALLER_PRINCIPAL_PASSWORD));

        String principal_group_name = parameters.getParameter(PRINCIPAL_GROUP_NAME,
            request.getParameter(PRINCIPAL_GROUP_NAME));

            ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(principal_provider);

            Principal caller = new Principal(caller_principal_name, caller_principal_password);
            PrincipalGroup principalgroup = new PrincipalGroup(principal_group_name);

            principalprovider.removePrincipalGroup(caller, principalgroup);

        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup for service.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
        return EMPTY_MAP;
    }

    public Map doAddPrincipalGroupMember(Redirector redirector,
                                         SourceResolver resolver,
                                         Map objectModel,
                                         String src,
                                         Parameters parameters) throws Exception {

        getLogger().debug("add principal group member called");

        Request request = ObjectModelHelper.getRequest(objectModel);

        String principal_provider = parameters.getParameter(PRINCIPAL_PROVIDER,
            request.getParameter(PRINCIPAL_PROVIDER));

        String caller_principal_name = parameters.getParameter(CALLER_PRINCIPAL_NAME,
            request.getParameter(CALLER_PRINCIPAL_NAME));

        String caller_principal_password = parameters.getParameter(CALLER_PRINCIPAL_PASSWORD,
            request.getParameter(CALLER_PRINCIPAL_PASSWORD));

        String principal_group_name = parameters.getParameter(PRINCIPAL_GROUP_NAME,
            request.getParameter(PRINCIPAL_GROUP_NAME));

        String principal_name = parameters.getParameter(PRINCIPAL_NAME,
            request.getParameter(PRINCIPAL_NAME));

            ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(principal_provider);

            Principal caller = new Principal(caller_principal_name, caller_principal_password);
            PrincipalGroup principalgroup = new PrincipalGroup(principal_group_name);
            Principal principal = new Principal(principal_name);

            principalprovider.addMember(caller, principalgroup, principal);

        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup for service.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
        return EMPTY_MAP;
    }

    public Map doRemovePrincipalGroupMember(Redirector redirector,
                                            SourceResolver resolver,
                                            Map objectModel,
                                            String src,
                                            Parameters parameters) throws Exception {

        getLogger().debug("add principal group member called");

        Request request = ObjectModelHelper.getRequest(objectModel);

        String principal_provider = parameters.getParameter(PRINCIPAL_PROVIDER,
            request.getParameter(PRINCIPAL_PROVIDER));

        String caller_principal_name = parameters.getParameter(CALLER_PRINCIPAL_NAME,
            request.getParameter(CALLER_PRINCIPAL_NAME));

        String caller_principal_password = parameters.getParameter(CALLER_PRINCIPAL_PASSWORD,
            request.getParameter(CALLER_PRINCIPAL_PASSWORD));

        String principal_group_name = parameters.getParameter(PRINCIPAL_GROUP_NAME,
            request.getParameter(PRINCIPAL_GROUP_NAME));

        String principal_name = parameters.getParameter(PRINCIPAL_NAME,
            request.getParameter(PRINCIPAL_NAME));

            ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(principal_provider);

            Principal caller = new Principal(caller_principal_name, caller_principal_password);
            PrincipalGroup principalgroup = new PrincipalGroup(principal_group_name);
            Principal principal = new Principal(principal_name);

            principalprovider.removeMember(caller, principalgroup, principal);

        } catch (ServiceException se) {
            throw new ProcessingException("Could not lookup for service.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
        return EMPTY_MAP;
    }
}

