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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.environment.Cookie;

/**
 * Implements {@link Cookie} interface for the JSR-168 Portlet environment.
 *
 * Portlet preferences are available in the Cocoon as Cookie objects.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletCookie.java,v 1.3 2003/12/23 15:28:33 joerg Exp $
 */
public final class PortletCookie implements Cookie {

    private String name;
    private String value;

    public PortletCookie(String name, String value) {
        init(name, value);
    }

    /**
     * Constructs a cookie with a specified name and value.
     *
     * @param name                         a <code>String</code> specifying the name of the cookie
     * @param value                        a <code>String</code> specifying the value of the cookie
     * @see #setValue(String)
     */
    public void init(String name, String value) {
        if (this.name == null) {
            this.name = name;
            this.value = value;
        } else {
            throw new IllegalStateException("Cookie is already initialised");
        }
    }

    private void checkState() {
        if (this.name == null) {
            throw new IllegalStateException("Cookie is not initialised");
        }
    }

    /**
     * This method does nothing
     */
    public void setComment(String purpose) {
    }

    /**
     * @return null
     * @see #setComment(String)
     */
    public String getComment() {
        checkState();
        return null;
    }

    /**
     * This method does nothing
     */
    public void setDomain(String pattern) {
        checkState();
    }

    /**
     * @return null
     * @see #setDomain(String)
     */
    public String getDomain() {
        checkState();
        return null;
    }

    /**
     * This method does nothing
     */
    public void setMaxAge(int expiry) {
        checkState();
    }

    /**
     * @return Integer.MAX_VALUE
     * @see #setMaxAge(int)
     */
    public int getMaxAge() {
        checkState();
        return Integer.MAX_VALUE;
    }

    /**
     * This method does nothing
     */
    public void setPath(String uri) {
        checkState();
    }

    /**
     * @return empty string
     * @see #setPath(String)
     */
    public String getPath() {
        checkState();
        return "";
    }

    /**
     * This method does nothing
     * @see #getSecure()
     */
    public void setSecure(boolean flag) {
        checkState();
    }

    /**
     * @return false
     * @see #setSecure(boolean)
     */
    public boolean getSecure() {
        checkState();
        return false;
    }

    /**
     * Returns the name of the cookie. The name cannot be changed after
     * creation.
     *
     * @return a <code>String</code> specifying the cookie's name
     */
    public String getName() {
        checkState();
        return this.name;
    }

    /**
     * Assigns a new value to a cookie after the cookie is created.
     * If you use a binary value, you may want to use BASE64 encoding.
     *
     * <p>With Version 0 cookies, values should not contain white
     * space, brackets, parentheses, equals signs, commas,
     * double quotes, slashes, question marks, at signs, colons,
     * and semicolons. Empty values may not behave the same way
     * on all browsers.
     *
     * @param newValue a <code>String</code> specifying the new value
     * @see #getValue()
     * @see Cookie
     */
    public void setValue(String newValue) {
        checkState();
        this.value = newValue;
    }

    /**
     * Returns the value of the cookie.
     *
     * @return                        a <code>String</code> containing the cookie's
     *                                present value
     * @see #setValue(String)
     * @see Cookie
     */
    public String getValue() {
        checkState();
        return this.value;
    }

    /**
     * Returns the version of the protocol this cookie complies
     * with.
     *
     * @return Always 0
     * @see #setVersion(int)
     */
    public int getVersion() {
        checkState();
        return 0;
    }

    /**
     * Sets the version of the cookie protocol this cookie complies
     * with. This method does nothing, version 0 is always returned in
     * getVersion
     *
     * @see #getVersion()
     */
    public void setVersion(int v) {
        checkState();
    }
}
