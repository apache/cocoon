/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.identification;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * <p>A {@link ParsedIdentifier} is a utilty class implementing the
 * {@link Identifier} interface.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class ParsedIdentifier implements Identifier {

    /** <p>The full URL of this {@link Identifier}.</p> */
    private URL url = null;

    /** <p>The base URL of this {@link Identifier}.</p> */
    private URL base = null;

    /** <p>The major version number.</p> */
    private int major = -1;

    /** <p>The minor version number.</p> */
    private int minor = -1;

    /** <p>The revision number.</p> */
    private int revision = -1;

    /** <p>The hash string.</p> */
    private String hash = null;
    
    /* ====================================================================== */

    /**
     * <p>Create a new {@link Identifier} number parsing a {@link String}.</p>
     *
     * @param url a {@link String} specifying the identifier (must be a URL).
     * @throws IdentificationException if the specified {@link String} does
     *                                 not satisfy the {@link Identifier}
     *                                 requirements.
     * @throws NullPointerException if the {@link String} was <b>null</b>.
     */
    public ParsedIdentifier(String url)
    throws IdentificationException {
        if (url == null) throw new NullPointerException("Can't parse null");
        try {
            this.parse(url);
        } catch (MalformedURLException e) {
            throw new IdentificationException("Can't parse \"" + url + "\"", e);
        }
    }

    /**
     * <p>Create a new {@link Identifier} number parsing a {@link URL}.</p>
     *
     * @param url a {@link URL} specifying the identifier.
     * @throws IdentificationException if the specified {@link URL} does not
     *                                 satisfy the {@link Identifier}
     *                                 requirements.
     * @throws NullPointerException if the {@link URL} was <b>null</b>.
     */
    public ParsedIdentifier(URL url)
    throws IdentificationException {
        if (url == null) throw new NullPointerException("Cannot parse null");
        try {
            this.parse(url);
        } catch (MalformedURLException e) {
            throw new IdentificationException("Can't parse \"" + url + "\"", e);
        }
    }

    /* ====================================================================== */
    
    /**
     * <p>Set up this {@link Identifier} instance parsing a {@link String}.</p>
     *
     * @param url The {@link String} representation of the URL to parse.
     * @throws MalformedURLException if the URL could not be parsed.
     */
    private void parse(String url)
    throws MalformedURLException {
        this.parse(new URL(url));
    }

    /**
     * <p>Set up this {@link Identifier} instance parsing a {@link URL}.</p>
     *
     * @param url The {@link URL} to parse.
     * @throws MalformedURLException if the URL could not be parsed.
     */
    private void parse(URL url)
    throws MalformedURLException {
        /* Get the path */
        String path = url.getPath();

        /* Check that url is something like http://somewhere/... */
        if (path.indexOf('/') != 0)
            throw new MalformedURLException("URL doesn't specify path");

        /* Retrieve version info out of the URL */
        String version = path.substring(path.lastIndexOf('/') + 1);
        if (version.length() == 0) 
            throw new MalformedURLException("URL doesn't specify version");
        if (path.lastIndexOf('/') < 1)
            throw new MalformedURLException("URL doesn't specify version");

        /* Retrieve the block path out of the URL */
        path = path.substring(1, path.lastIndexOf('/'));
        if (path.length() == 0)
            throw new MalformedURLException("URL doesn't specify base path");

        /* Parse the version string */
        StringTokenizer tokenizer = new StringTokenizer(version, ".");
        if (!tokenizer.hasMoreTokens())
            throw new MalformedURLException("Major version not specified");
        String major = tokenizer.nextToken();
            if (!tokenizer.hasMoreTokens())
                throw new MalformedURLException("Minor version not specified");
        String minor = tokenizer.nextToken();
        String revision = null;
        if (tokenizer.hasMoreTokens()) revision = tokenizer.nextToken();
    
        /* Parse numbers in version */
        try {
            /* Parse and check major version number */
            this.major = Integer.parseInt(major);
            if ((this.major < 0) || (this.minor > 254)) {
                throw new MalformedURLException("0 <= Major version <= 254");
            }

            /* Parse and check minor version number */
            this.minor = Integer.parseInt(minor); {
            if ((this.minor < 0) || (this.minor > 254))
                throw new MalformedURLException("0 <= Minor version <= 254");
            }

            /* Parse and check revision number */
            if (revision != null) {
                this.revision = Integer.parseInt(revision);
                if ((this.revision < 0) || (this.revision > 254)) {
                    throw new MalformedURLException("0 <= Revision <= 254");
                }
            }
        } catch (NumberFormatException e) {
            throw new MalformedURLException("Invalid version number "+ version);
        }

        /* Reconstruct the URL and check */
        this.base = new URL(url, '/' + path + '/');
        this.url = new URL(url, '/' + path + '/' + this.major + '.' + this.minor
                           + (this.revision < 0 ? "" : ("." + this.revision)));
        if (!this.url.toString().equals(url.toString()))
            throw new MalformedURLException("Parsing versioned URL " + url
                                            + " returned invalid " + this.url);
        
        /* Create the hash string */
        StringBuffer buffer = new StringBuffer(24);
        String hex = Integer.toHexString(this.base.hashCode());
        for (int x = hex.length(); x < 8; x ++) buffer.append('0');
        buffer.append(hex);
        buffer.append('-');
        hex = Integer.toHexString(((this.major & 0x0ff) << 16)
                                  + ((this.minor & 0x0ff) << 8)
                                  + (this.revision & 0x0ff));
        for (int x = hex.length(); x < 6; x ++) buffer.append('0');
        buffer.append(hex);
        this.hash = buffer.toString();
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Compare an {@link Object} for equality.</p>
     *
     * <p>A specified {@link Object} equals this one if it is a
     * {@link Identifier} instance, and its hash code equals the hash code of
     * this instance.</p>
     *
     * @param o an {@link Object} to compare for equality.
     * @return <b>true</b> if the specified object equals this
     *         {@link Identifier} instance, <b>false</b> otherwise.
     */
    public boolean equals(Object o) {
        /* Simple check */
        if (o == null) return (false);
        
        /* If the o is a Identifier, URL or String compare it using strings */
        if (o instanceof Identifier) {
            return(o.hashCode() == this.hashCode());
        }

        /* In all other cases, we're not equals */
        return(false);
    }

    /**
     * <p>Return the hash code of this {@link Identifier}.</p>
     *
     * @return the hash code.
     */
    public int hashCode() {
        return(this.url.hashCode());
    }

    /**
     * <p>Return the full URL of this identifier as a {@link String}.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String toString() {
        return(this.url.toString());
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return the full URL of this identifier as an {@link URL}.</p>
     *
     * @return a <b>non null</b> {@link URL}.
     */
    public URL toURL() {
        return(this.url);
    }

    /* ====================================================================== */

    /**
     * <p>Return the base URL of this identifier as an {@link URL}.</p>
     *
     * @return a <b>non null</b> {@link URL}.
     */
    public URL base() {
        return(this.base);
    }
    
    /**
     * <p>Return a human-readable representation of the version number.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String version() {
        String string = new String(this.major + "." + this.minor);
        if (this.revision < 0) return(string);
        return(string + "." + this.revision);
    }
    
    /**
     * <p>Return the major version number.</p>
     *
     * @return a <b>non negative</b> number.
     */
    public int major() {
        return(this.major);
    }

    /**
     * <p>Return the minor version number.</p>
     *
     * @return a <b>non negative</b> number.
     */
    public int minor() {
        return(this.minor);
    }

    /**
     * <p>Return the revision number.</p>
     *
     * @return a <b>non negative</b> number or -1 if this {@link Identifier}
     *         does not specify a revision number.
     */
    public int revision() {
        return(this.revision);
    }
    
    /* ====================================================================== */
    
    /**
     * <p>Return a hashed {@link String} representation of this
     * {@link Identifier}.</p>
     *
     * @return a <b>non null</b> {@link String}.
     */
    public String hash() {
        return(this.hash);
    }
}
