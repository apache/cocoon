/*
 * Copyright 2004, Ugo Cei.
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.butterfly.source;

import java.io.UnsupportedEncodingException;


/**
 * Utility class for source resolving.
 * 
 * @version CVS $Id: SourceUtil.java,v 1.1 2004/07/23 08:47:20 ugo Exp $
 */
public class SourceUtil {

    /**
     * Get the position of the scheme-delimiting colon in an absolute URI, as specified
     * by <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>, appendix A. This method is
     * primarily useful for {@link Source} implementors that want to separate
     * the scheme part from the specific part of an URI.
     * <p>
     * Use this method when you need both the scheme and the scheme-specific part of an URI,
     * as calling successively {@link #getScheme(String)} and {@link #getSpecificPart(String)}
     * will call this method twice, and as such won't be efficient.
     *
     * @param uri the URI
     * @return int the scheme-delimiting colon, or <code>-1</code> if not found.
     */
    public static int indexOfSchemeColon(String uri)
    {
        // absoluteURI   = scheme ":" ( hier_part | opaque_part )
        //
        // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
        //
        // alpha         = lowalpha | upalpha
        //
        // lowalpha = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" |
        //            "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" |
        //            "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"
        //
        // upalpha  = "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" |
        //            "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" |
        //            "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"
        //
        // digit    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" |
        //            "8" | "9"

        // Must have at least one character followed by a colon
        if (uri == null || uri.length() < 2)
        {
            return -1;
        }

        // Check that first character is alpha
        // (lowercase first since it's the most common case)
        char ch = uri.charAt(0);
        if ( (ch < 'a' || ch > 'z') &&
             (ch < 'A' || ch > 'Z') )
        {
            // Invalid first character
            return -1;
        }

        int pos = uri.indexOf(':');
        if (pos != -1)
        {
            // Check that every character before the colon is in the allowed range
            // (the first one was tested above)
            for (int i = 1; i < pos; i++)
            {
                ch = uri.charAt(i);
                if ( (ch < 'a' || ch > 'z') &&
                     (ch < 'A' || ch > 'Z') &&
                     (ch < '0' || ch > '9') &&
                     ch != '+' && ch != '-' && ch != '.')
                {
                    return -1;
                }
            }
        }

        return pos;
    }

    /**
     * Get the scheme of an absolute URI.
     *
     * @param uri the absolute URI
     * @return the URI scheme
     */
    public static String getScheme(String uri)
    {
        int pos = indexOfSchemeColon(uri);
        return (pos == -1) ? null : uri.substring(0, pos);
    }

    /**
     * Get the scheme-specific part of an absolute URI. Note that this includes everything
     * after the separating colon, including the fragment, if any (RFC 2396 separates it
     * from the scheme-specific part).
     *
     * @param uri the absolute URI
     * @return the scheme-specific part of the URI
     */
    public static String getSpecificPart(String uri)
    {
        int pos = indexOfSchemeColon(uri);
        return (pos == -1) ? null : uri.substring(pos+1);
    }

    /**
     * Calls absolutize(url1, url2, false).
     */
    public static String absolutize(String url1, String url2)
    {
        return absolutize(url1, url2, false, true);
    }

    /**
     * Calls absolutize(url1, url2, false, true).
     */
    public static String absolutize(String url1, String url2, boolean treatAuthorityAsBelongingToPath)
    {
        return absolutize(url1, url2, treatAuthorityAsBelongingToPath, true);
    }

    /**
     * Applies a location to a baseURI. This is done as described in RFC 2396 section 5.2.
     *
     * @param url1 the baseURI
     * @param url2 the location
     * @param treatAuthorityAsBelongingToPath considers the authority to belong to the path. These
     * special kind of URIs are used in the Apache Cocoon project.
     * @param normalizePath should the path be normalized, i.e. remove ../ and /./ etc.
     */
    public static String absolutize(String url1, String url2, boolean treatAuthorityAsBelongingToPath, boolean normalizePath)
    {
        if (url1 == null)
            return url2;

        // If the URL contains a scheme (and thus is already absolute), don't do any further work
        if (getScheme(url2) != null)
            return url2;

        // parse the urls into parts
        // if the second url contains a scheme, it is not relative so return it right away (part 3 of the algorithm)
        String[] url1Parts = parseUrl(url1);
        String[] url2Parts = parseUrl(url2);

        if (treatAuthorityAsBelongingToPath)
            return absolutizeWithoutAuthority(url1Parts, url2Parts);

        // check if it is a reference to the current document (part 2 of the algorithm)
        if (url2Parts[PATH].equals("") && url2Parts[QUERY] == null && url2Parts[AUTHORITY] == null)
            return makeUrl(url1Parts[SCHEME], url1Parts[AUTHORITY], url1Parts[PATH], url1Parts[QUERY], url2Parts[FRAGMENT]);

        // it is a network reference (part 4 of the algorithm)
        if (url2Parts[AUTHORITY] != null)
            return makeUrl(url1Parts[SCHEME], url2Parts[AUTHORITY], url2Parts[PATH], url2Parts[QUERY], url2Parts[QUERY]);

        String url1Path = url1Parts[PATH];
        String url2Path = url2Parts[PATH];

        // if the path starts with a slash (part 5 of the algorithm)
        if (url2Path != null && url2Path.length() > 0 && url2Path.charAt(0) == '/')
            return makeUrl(url1Parts[SCHEME], url1Parts[AUTHORITY], url2Parts[PATH], url2Parts[QUERY], url2Parts[QUERY]);

        // combine the 2 paths
        String path = stripLastSegment(url1Path);
        path = path + (path.endsWith("/") ? "" : "/") + url2Path;
        if (normalizePath)
            path = normalize(path);

        return makeUrl(url1Parts[SCHEME], url1Parts[AUTHORITY], path, url2Parts[QUERY], url2Parts[FRAGMENT]);
    }

    /**
     * Absolutizes URIs whereby the authority part is considered to be a part of the path.
     * This special kind of URIs is used in the Apache Cocoon project for the cocoon and context protocols.
     * This method is internally used by {@link #absolutize}.
     */
    private static String absolutizeWithoutAuthority(String[] url1Parts, String[] url2Parts)
    {
        String authority1 = url1Parts[AUTHORITY];
        String authority2 = url2Parts[AUTHORITY];

        String path1 = url1Parts[PATH];
        String path2 = url2Parts[PATH];

        if (authority1 != null)
            path1 = "//" + authority1 + path1;
        if (authority2 != null)
            path2 = "//" + authority2 + path2;

        String path = stripLastSegment(path1);
        path = path + (path.endsWith("/") ? "" : "/") + path2;
        path = normalize(path);

        String scheme = url1Parts[SCHEME];
        return scheme + ":" + path;
    }

    private static String stripLastSegment(String path)
    {
        int i = path.lastIndexOf('/');
        if(i > -1)
            return path.substring(0, i + 1);
        return path;
    }

    /**
     * Removes things like &lt;segment&gt;/../ or ./, as described in RFC 2396 in
     * step 6 of section 5.2.
     */
    private static String normalize(String path)
    {
        // replace all /./ with /
        int i = path.indexOf("/./");
        while (i > -1)
        {
            path = path.substring(0, i + 1) + path.substring(i + 3);
            i = path.indexOf("/./");
        }

        if (path.endsWith("/."))
            path = path.substring(0, path.length() - 1);

        int f = path.indexOf("/../");
        while (f > 0)
        {
            int sb = path.lastIndexOf("/", f - 1);
            if (sb > - 1)
                path = path.substring(0, sb + 1) + (path.length() >= f + 4 ? path.substring(f + 4) : "");
            f = path.indexOf("/../");
        }

        if (path.length() > 3 && path.endsWith("/.."))
        {
            int sb = path.lastIndexOf("/", path.length() - 4);
            String segment = path.substring(sb, path.length() - 3);
            if (!segment.equals(".."))
            {
                path = path.substring(0, sb + 1);
            }
        }

        return path;
    }

    /**
     * Assembles an URL from the given URL parts, each of these parts can be null.
     * Used internally by {@link #absolutize}.
     */
    private static String makeUrl(String scheme, String authority, String path, String query, String fragment)
    {
        StringBuffer url = new StringBuffer();
        if (scheme != null)
            url.append(scheme).append(':');

        if (authority != null)
            url.append("//").append(authority);

        if (path != null)
            url.append(path);

        if (query != null)
            url.append('?').append(query);

        if (fragment != null)
            url.append('#').append(fragment);

        return url.toString();
    }

    public static final int SCHEME = 0;
    public static final int AUTHORITY = 1;
    public static final int PATH = 2;
    public static final int QUERY = 3;
    public static final int FRAGMENT = 4;

    /**
     * Parses an URL into the following parts: scheme, authority, path, query and fragment identifier.
     *
     * <p>The parsing is designed to be robust in the sense that it will never fail, even when an invalid
     * URL is given. The parser will simply look for the most important delimiter characters. Basically
     * it does the same as what would be achieved using the following regular expression (from RFC 2396):
     * <pre>
     * ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?
     *  12            3  4          5       6  7        8 9
     * </pre>
     * but without actually using the regular expression.
     *
     * <p>The result is returned as a string array, use the constants SCHEME, AUTHORITY, PATH,
     * QUERY and FRAGMENT_IDENTIFIER to access the different parts.
     *
     * <p>If a part is missing, its corresponding entry in the array will be null, except for the
     * path, which will never be null.
     */
    public static String[] parseUrl(String url) {
        char[] urlchars = url.toCharArray();

        int pos = 0;

        String scheme = null;
        String authority = null;
        String path = null;
        String query = null;
        String fragid = null;

        //  ^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?

        // the scheme
        boolean keepgoing = true;
        while (keepgoing && pos < urlchars.length)
        {
            switch (urlchars[pos])
            {
                case ':':
                    if (pos >= 1)
                    {
                        scheme = new String(urlchars, 0, pos);
                        keepgoing = false;
                        pos++;
                        break;
                    }
                case '/':
                case '?':
                case '#':
                    keepgoing = false;
                    break;
                default:
                    pos++;
            }
        }

        if (scheme == null)
            pos = 0;

        //  the authority
        if (pos + 1 < urlchars.length && urlchars[pos] == '/' && urlchars[pos+1] == '/')
        {
            pos += 2;
            int authorityBeginPos = pos;
            keepgoing = true;
            while (keepgoing && pos < urlchars.length)
            {
                switch (urlchars[pos])
                {
                    case '/':
                    case '?':
                    case '#':
                        keepgoing = false;
                        break;
                    default:
                        pos++;
                }
            }
            authority = new String(urlchars, authorityBeginPos, pos - authorityBeginPos);
        }

        //  the path
        int pathBeginPos = pos;
        keepgoing = true;
        while (keepgoing && pos < urlchars.length)
        {
            switch (urlchars[pos])
            {
                case '?':
                case '#':
                    keepgoing = false;
                    break;
                default:
                    pos++;
            }
        }
        path = new String(urlchars, pathBeginPos, pos - pathBeginPos);

        // the query
        if (pos < urlchars.length && urlchars[pos] == '?')
        {
            pos++;
            int queryBeginPos = pos;
            keepgoing = true;
            while (keepgoing && pos < urlchars.length)
            {
                switch (urlchars[pos])
                {
                    case '#':
                        keepgoing = false;
                        break;
                    default:
                        pos++;
                }
            }
            query = new String(urlchars, queryBeginPos, pos - queryBeginPos);
        }

        // the fragment identifier
        pos++;
        if (pos < urlchars.length)
            fragid = new String(urlchars, pos, urlchars.length - pos);

        return new String[] {scheme, authority, path, query, fragid};
    }

    /**
     * Decode a path.
     *
     * <p>Interprets %XX (where XX is hexadecimal number) as UTF-8 encoded bytes.
     * <p>The validity of the input path is not checked (i.e. characters that
     * were not encoded will not be reported as errors).
     * <p>This method differs from URLDecoder.decode in that it always uses UTF-8
     * (while URLDecoder uses the platform default encoding, often ISO-8859-1),
     * and doesn't translate + characters to spaces.
     *
     * @param path the path to decode
     * @return the decoded path
     */
    public static String decodePath(String path) {
        StringBuffer translatedPath = new StringBuffer(path.length());
        byte[] encodedchars = new byte[path.length() / 3];
        int i = 0;
        int length = path.length();
        int encodedcharsLength = 0;
        while (i < length) {
            if (path.charAt(i) == '%') {
                // we must process all consecutive %-encoded characters in one go, because they represent
                // an UTF-8 encoded string, and in UTF-8 one character can be encoded as multiple bytes
                while (i < length && path.charAt(i) == '%') {
                    if (i + 2 < length) {
                        try {
                            byte x = (byte)Integer.parseInt(path.substring(i + 1, i + 3), 16);
                            encodedchars[encodedcharsLength] = x;
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Illegal hex characters in pattern %" + path.substring(i + 1, i + 3));
                        }
                        encodedcharsLength++;
                        i += 3;
                    } else {
                        throw new IllegalArgumentException("% character should be followed by 2 hexadecimal characters.");
                    }
                }
                try {
                    String translatedPart = new String(encodedchars, 0, encodedcharsLength, "UTF-8");
                    translatedPath.append(translatedPart);
                } catch (UnsupportedEncodingException e) {
                    // the situation that UTF-8 is not supported is quite theoretical, so throw a runtime exception
                    throw new RuntimeException("Problem in decodePath: UTF-8 encoding not supported.");
                }
                encodedcharsLength = 0;
            } else {
                // a normal character
                translatedPath.append(path.charAt(i));
                i++;
            }
        }
        return translatedPath.toString();
    }

}
