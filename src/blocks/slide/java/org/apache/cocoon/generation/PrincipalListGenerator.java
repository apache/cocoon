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

package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.slide.Principal;
import org.apache.cocoon.components.slide.PrincipalGroup;
import org.apache.cocoon.components.slide.PrincipalProvider;
import org.apache.cocoon.environment.SourceResolver;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The generator generates a list of all principals and group of
 * principals from a PrincipalProvider.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: PrincipalListGenerator.java,v 1.5 2003/12/02 19:18:46 unico Exp $
 */
public class PrincipalListGenerator extends ServiceableGenerator 
        implements Recyclable {

    /** Namespace of the generated list. */
    private static final String PL_NS =
      "http://apache.org/cocoon/principal/1.0";

    /** The namespace prefix of the resource description framework. */
    //private static final String PL_PREFIX                   = "pl";

    private static final String LIST_ELEMENT_NAME           = "list";
    private static final String PRINCIPAL_ELEMENT_NAME      = "principal";
    private static final String PRINCIPALGROUP_ELEMENT_NAME = "group";

    private static final String NAME_ATTR_NAME              = "name";
    private static final String ROLE_ATTR_NAME              = "role";
    private static final String PASSWORD_ATTR_NAME          = "password";

    private Principal principalcaller = null;
    private String principalprovidername = null;

    public void setup(SourceResolver resolver, Map objectModel, String location, Parameters parameters)
        throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, location, parameters);

        try {
            this.principalcaller = new Principal(parameters.getParameter("principalcaller", "guest"));
            this.principalprovidername = parameters.getParameter("principalprovider");
        } catch (ParameterException pe) {
            throw new ProcessingException("Could not retrieve parameters", pe);
        }
    }

    /**
     * Generate XML data.
     */
    public void generate() throws IOException, SAXException, ProcessingException {

        ServiceSelector principalproviders = null;
        PrincipalProvider principalprovider = null;
        try {
            principalproviders = (ServiceSelector)this.manager.lookup(PrincipalProvider.ROLE+"Selector");

            principalprovider = (PrincipalProvider)principalproviders.select(this.principalprovidername);

            Principal[] principals = principalprovider.getPrincipals(this.principalcaller);
            PrincipalGroup[] principalgroups = principalprovider.getPrincipalGroups(this.principalcaller);

            this.contentHandler.startDocument();
            this.contentHandler.startPrefixMapping("",PL_NS);

            this.contentHandler.startElement(PL_NS, LIST_ELEMENT_NAME,
                                                    LIST_ELEMENT_NAME, new AttributesImpl());

            AttributesImpl attributes;
            for(int i=0; i<principals.length; i++) {
                attributes = new AttributesImpl();
                attributes.addAttribute("", NAME_ATTR_NAME, NAME_ATTR_NAME, "CDATA", principals[i].getName());
                if (principals[i].getRole()!=null)
                    attributes.addAttribute("", ROLE_ATTR_NAME, ROLE_ATTR_NAME, "CDATA", principals[i].getRole());
                if (principals[i].getPassword()!=null)
                    attributes.addAttribute("", PASSWORD_ATTR_NAME, PASSWORD_ATTR_NAME, "CDATA", principals[i].getPassword());

                this.contentHandler.startElement(PL_NS, PRINCIPAL_ELEMENT_NAME,
                                                        PRINCIPAL_ELEMENT_NAME, attributes);
                this.contentHandler.endElement(PL_NS, PRINCIPAL_ELEMENT_NAME, PRINCIPAL_ELEMENT_NAME);
            }

            for(int i=0; i<principalgroups.length; i++) {
                attributes = new AttributesImpl();
                attributes.addAttribute("", NAME_ATTR_NAME, NAME_ATTR_NAME, "CDATA", principalgroups[i].getName());

                this.contentHandler.startElement(PL_NS, PRINCIPALGROUP_ELEMENT_NAME,
                                                        PRINCIPALGROUP_ELEMENT_NAME, attributes);

                Principal[] members = principalprovider.members(this.principalcaller, principalgroups[i]);
                for(int j=0; j<members.length; j++) {
                    attributes = new AttributesImpl();
                    attributes.addAttribute("", NAME_ATTR_NAME, NAME_ATTR_NAME, "CDATA", members[j].getName());
                    if (members[j].getRole()!=null) 
                        attributes.addAttribute("", ROLE_ATTR_NAME, ROLE_ATTR_NAME, "CDATA", members[j].getRole());
                    if (members[j].getPassword()!=null)
                        attributes.addAttribute("", PASSWORD_ATTR_NAME, PASSWORD_ATTR_NAME, "CDATA",
                                                members[j].getPassword());

                    this.contentHandler.startElement(PL_NS, PRINCIPAL_ELEMENT_NAME,
                                                            PRINCIPAL_ELEMENT_NAME, attributes);
                    this.contentHandler.endElement(PL_NS, PRINCIPAL_ELEMENT_NAME, PRINCIPAL_ELEMENT_NAME);
                }

                this.contentHandler.endElement(PL_NS, PRINCIPALGROUP_ELEMENT_NAME, PRINCIPALGROUP_ELEMENT_NAME);
            }
                

            this.contentHandler.endElement(PL_NS, LIST_ELEMENT_NAME, LIST_ELEMENT_NAME);

            this.contentHandler.endPrefixMapping("");
            this.contentHandler.endDocument();

        } catch (ServiceException se) {
            getLogger().error("Could not lookup for component.", se);
        } finally {
            if (principalprovider!=null)
                principalproviders.release(principalprovider);
            principalprovider = null;

            if (principalproviders!=null)
                this.manager.release(principalproviders);
            principalproviders = null;
        }
    }

    public void recycle() {
        this.principalcaller = null;
        this.principalprovidername = null;
        super.recycle();
    }
}

