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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.environment.Cookie;

/**
 * Implements {@link Cookie} interface for the JSR-168 Portlet environment.
 *
 * Portlet preferences are available in the Cocoon as Cookie objects.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletCookie.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
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
