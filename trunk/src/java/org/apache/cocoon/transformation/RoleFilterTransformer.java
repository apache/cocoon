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
package org.apache.cocoon.transformation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Filter XML fragments based on a user's role.  This will help in
 * the development of smart forms that must only show information to
 * people who are logged in and have the correct role.  The Role is
 * specified by the Request semantics.  You can specify multiple roles
 * by using comma delimiting.
 *
 * <pre>
 *   &lt;root xmlns:roles="http://apache.org/cocoon/role-filter/1.0"&gt;
 *     &lt;textbox name="identifier" roles:restricted="admin,boss"/&gt;
 *     &lt;textbox name="name" roles:read-only="admin,boss"/&gt;
 *   &lt;/root&gt;
 * </pre>
 *
 * The previous example will only show the "identifier" textbox for the
 * roles "admin" and "boss".  It will pass role:read-only="" if the
 * roles "admin" or "boss" are accessing the page.  That way you can
 * specify any special processing by testing for the read-only attribute.
 * This filter does not care about the prefix, only the namespace URI.
 * That means you can reassign the namespace to another prefix and all
 * will work as expected.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: RoleFilterTransformer.java,v 1.5 2003/12/06 21:22:07 cziegeler Exp $
 */
public class RoleFilterTransformer extends FilterTransformer {
    private final static String URI = "http://apache.org/cocoon/role-filter/1.0";
    private final static String RESTRICT = "restricted";
    private final static String VIEW = "read-only";
    Request request = null;

    public RoleFilterTransformer() {
    }

    public final void setup(SourceResolver resolver, Map objectModel, String src, Parameters params)
    throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, params);
        this.request = ObjectModelHelper.getRequest(objectModel);
    }

    /**
     * Disable caching
     */
    public java.io.Serializable getKey() {
        return null;
    }

    public final void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        int roleIndex = a.getIndex(RoleFilterTransformer.URI, RoleFilterTransformer.RESTRICT);
        int viewIndex = a.getIndex(RoleFilterTransformer.URI, RoleFilterTransformer.VIEW);
        boolean propogate = true;
        boolean readOnly = false;

        if (roleIndex >= 0) {
            String roleRestriction = a.getValue(roleIndex);
            StringTokenizer roles = new StringTokenizer(roleRestriction, ",", false);
            propogate = false;

            while ((! propogate) && roles.hasMoreTokens()) {
                if (request.isUserInRole(roles.nextToken())) {
                    propogate = true;
                }
            }
        }

        if (! propogate) {
            super.elementName = loc;
        } else {
            if (viewIndex >= 0) {
                String viewRestriction = a.getValue(viewIndex);
                StringTokenizer roles = new StringTokenizer(viewRestriction, ",", false);

                while ((! readOnly) && roles.hasMoreTokens()) {
                    if (request.isUserInRole(roles.nextToken())) {
                        readOnly = true;
                    }
                }
            }
        }

        super.startElement(uri, loc, raw,
                this.copyAttributes(a, roleIndex, viewIndex, readOnly));
    }

    public final void endElement(String uri, String loc, String raw)
    throws SAXException {
        super.endElement(uri, loc, raw);

        if (! super.skip) {
            super.elementName = "";
        }
    }

    private final Attributes copyAttributes(final Attributes a, final int role,
                                            final int view, boolean readOnly) {
        if (role < 0 && view < 0) {
            return a;
        }

        AttributesImpl attr = new AttributesImpl();
        attr.setAttributes(a);
        if (role >= 0) {
            attr.removeAttribute(role);
        }

        if (view >= 0) {
            if (readOnly) {
                attr.setValue(view, "");
            } else {
                attr.removeAttribute(view);
            }
        }

        return attr;
    }

    public void recycle() {
        this.request = null;
        super.recycle();
    }
}
