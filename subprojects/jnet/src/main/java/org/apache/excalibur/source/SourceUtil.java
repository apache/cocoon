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
package org.apache.excalibur.source;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.Iterator;

/**
 *
 * Utility class for source resolving.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id$
 */
public final class SourceUtil
{
    private static final char[] alphabet = new char[]
    {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', // 0 to 7
        'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 8 to 15
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 16 to 23
        'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', // 24 to 31
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 32 to 39
        'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 40 to 47
        'w', 'x', 'y', 'z', '0', '1', '2', '3', // 48 to 55
        '4', '5', '6', '7', '8', '9', '+', '/'}; // 56 to 63

    /**
     * Append parameters to the uri
     * Each parameter is appended to the uri with "parameter=value",
     * the parameters are separated by "&".
     */
    public static String appendParameters( String uri,
                                           SourceParameters parameters )
    {
        if( parameters != null )
        {
            StringBuffer buffer = new StringBuffer( uri );
            Iterator keys = parameters.getParameterNames();
            String current;
            char separator = ( uri.indexOf( "?" ) == -1 ? '?' : '&' );
            Iterator values;

            while( keys.hasNext() == true )
            {
                current = (String)keys.next();
                values = parameters.getParameterValues( current );
                while( values.hasNext() == true )
                {
                    buffer.append( separator )
                        .append( current )
                        .append( '=' )
                        .append( SourceUtil.encode( (String)values.next() ) );
                    separator = '&';
                }
            }
            return buffer.toString();
        }

        return uri;
    }

    /**
     * BASE 64 encoding.
     * See also RFC 1421
     */
    public static String encodeBASE64( String s )
    {
        return encodeBASE64( s.getBytes() );
    }

    /**
     * BASE 64 encoding.
     * See also RFC 1421
     */
    public static String encodeBASE64( byte[] octetString )
    {
        int bits24;
        int bits6;

        char[] out = new char[ ( ( octetString.length - 1 ) / 3 + 1 ) * 4 ];

        int outIndex = 0;
        int i = 0;

        while( ( i + 3 ) <= octetString.length )
        {
            // store the octets
            bits24 = ( octetString[ i++ ] & 0xFF ) << 16;
            bits24 |= ( octetString[ i++ ] & 0xFF ) << 8;
            bits24 |= ( octetString[ i++ ] & 0xFF ) << 0;

            bits6 = ( bits24 & 0x00FC0000 ) >> 18;
            out[ outIndex++ ] = alphabet[ bits6 ];
            bits6 = ( bits24 & 0x0003F000 ) >> 12;
            out[ outIndex++ ] = alphabet[ bits6 ];
            bits6 = ( bits24 & 0x00000FC0 ) >> 6;
            out[ outIndex++ ] = alphabet[ bits6 ];
            bits6 = ( bits24 & 0x0000003F );
            out[ outIndex++ ] = alphabet[ bits6 ];
        }

        if( octetString.length - i == 2 )
        {
            // store the octets
            bits24 = ( octetString[ i ] & 0xFF ) << 16;
            bits24 |= ( octetString[ i + 1 ] & 0xFF ) << 8;

            bits6 = ( bits24 & 0x00FC0000 ) >> 18;
            out[ outIndex++ ] = alphabet[ bits6 ];
            bits6 = ( bits24 & 0x0003F000 ) >> 12;
            out[ outIndex++ ] = alphabet[ bits6 ];
            bits6 = ( bits24 & 0x00000FC0 ) >> 6;
            out[ outIndex++ ] = alphabet[ bits6 ];

            // padding
            out[ outIndex++ ] = '=';
        }
        else if( octetString.length - i == 1 )
        {
            // store the octets
            bits24 = ( octetString[ i ] & 0xFF ) << 16;

            bits6 = ( bits24 & 0x00FC0000 ) >> 18;
            out[ outIndex++ ] = alphabet[ bits6 ];
            bits6 = ( bits24 & 0x0003F000 ) >> 12;
            out[ outIndex++ ] = alphabet[ bits6 ];

            // padding
            out[ outIndex++ ] = '=';
            out[ outIndex++ ] = '=';
        }

        return new String( out );
    }

    /** A BitSet defining the characters which don't need encoding */
    static BitSet charactersDontNeedingEncoding;
    static final int characterCaseDiff = ( 'a' - 'A' );

    /** Initialize the BitSet */
    static
    {
        charactersDontNeedingEncoding = new BitSet( 256 );
        int i;
        for( i = 'a'; i <= 'z'; i++ )
        {
            charactersDontNeedingEncoding.set( i );
        }
        for( i = 'A'; i <= 'Z'; i++ )
        {
            charactersDontNeedingEncoding.set( i );
        }
        for( i = '0'; i <= '9'; i++ )
        {
            charactersDontNeedingEncoding.set( i );
        }
        charactersDontNeedingEncoding.set( '-' );
        charactersDontNeedingEncoding.set( '_' );
        charactersDontNeedingEncoding.set( '.' );
        charactersDontNeedingEncoding.set( '*' );
        charactersDontNeedingEncoding.set( '"' );
    }

    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format.
     *
     * @param   s   <code>String</code> to be translated.
     * @return  the translated <code>String</code>.
     */
    public static String encode( String s )
    {
        final StringBuffer out = new StringBuffer( s.length() );
        final ByteArrayOutputStream buf = new ByteArrayOutputStream( 32 );
        final OutputStreamWriter writer = new OutputStreamWriter( buf );
        for( int i = 0; i < s.length(); i++ )
        {
            int c = s.charAt( i );
            if( charactersDontNeedingEncoding.get( c ) )
            {
                out.append( (char)c );
            }
            else
            {
                try
                {
                    writer.write( c );
                    writer.flush();
                }
                catch( IOException e )
                {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for( int j = 0; j < ba.length; j++ )
                {
                    out.append( '%' );
                    char ch = Character.forDigit( ( ba[ j ] >> 4 ) & 0xF, 16 );
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if( Character.isLetter( ch ) )
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append( ch );
                    ch = Character.forDigit( ba[ j ] & 0xF, 16 );
                    if( Character.isLetter( ch ) )
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append( ch );
                }
                buf.reset();
            }
        }

        return out.toString();
    }

    /**
     * Translates a string into <code>x-www-form-urlencoded</code> format
     * with specified encoding
     *
     * @param   s   <code>String</code> to be translated.
     * @param   enc The name of a supported charset
     * @return  the translated <code>String</code>.
     * @throws UnsupportedEncodingException
     */
    public static String encode( String s, String enc ) throws UnsupportedEncodingException
    {
        // Why not use the java.net.URLEncoder for this purpose?
        final StringBuffer out = new StringBuffer( s.length() );
        final ByteArrayOutputStream buf = new ByteArrayOutputStream( 32 );
        final OutputStreamWriter writer = new OutputStreamWriter( buf, enc );
        for( int i = 0; i < s.length(); i++ )
        {
            int c = s.charAt( i );
            if( charactersDontNeedingEncoding.get( c ) )
            {
                out.append( (char)c );
            }
            else
            {
                try
                {
                    writer.write( c );
                    writer.flush();
                }
                catch( IOException e )
                {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for( int j = 0; j < ba.length; j++ )
                {
                    out.append( '%' );
                    char ch = Character.forDigit( ( ba[ j ] >> 4 ) & 0xF, 16 );
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if( Character.isLetter( ch ) )
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append( ch );
                    ch = Character.forDigit( ba[ j ] & 0xF, 16 );
                    if( Character.isLetter( ch ) )
                    {
                        ch -= characterCaseDiff;
                    }
                    out.append( ch );
                }
                buf.reset();
            }
        }

        return out.toString();
    }

    /**
     * Return a <code>File</code> object associated with the <code>Source</code> object.
     *
     * @return The corresponding <code>File</code> object or null if the
     *         <code>Source</code> object does not point to a file URI.
     */
    public static File getFile( Source source )
    {
        final String systemId = source.getURI();
        if( systemId.startsWith( "file:" ) )
        {
            return new File( systemId.substring( 5 ) );
        }
        return null;
    }

    /**
     * Move the source to a specified destination.
     *
     * @param source Source of the source.
     * @param destination Destination of the source.
     *
     * @throws SourceException If an exception occurs during
     *                         the move.
     */
    static public void move(Source source,
                              Source destination)
    throws SourceException
    {
        if (source instanceof MoveableSource
            && source.getClass().equals(destination.getClass()))
        {
            ((MoveableSource)source).moveTo(destination);
        }
        else if (source instanceof ModifiableSource)
        {
            copy(source, destination);
            ((ModifiableSource) source).delete();
        }
        else
        {
            throw new SourceException("Source '"+source.getURI()+ "' is not writeable");
        }
    }

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
     * Copy the source to a specified destination.
     *
     * @param source Source of the source.
     * @param destination Destination of the source.
     *
     * @throws SourceException If an exception occurs during
     *                         the copy.
     */
    static public void copy(Source source,
                            Source destination)
        throws SourceException
    {
        if (source instanceof MoveableSource
            && source.getClass().equals(destination.getClass()))
        {
            ((MoveableSource) source).copyTo(destination);
        }
        else
        {
            if ( !(destination instanceof ModifiableSource)) {
                throw new SourceException("Source '"+
                                          destination.getURI()+
                                          "' is not writeable");
            }

            IOException firstE = null;
            ModifiableSource modDestination = (ModifiableSource)destination;
            try
            {
                InputStream in = source.getInputStream();
                try
                {
                    OutputStream out = modDestination.getOutputStream();
                    try
                    {
                        try
                        {
                            copy(in, out);
                        }
                        catch ( IOException e )
                        {
                            // Remebver the original exception in case there are problems closing
                            //  any streams.
                            firstE = e;

                            // If possible, cancel the destination.
                            if ( modDestination.canCancel( out ) )
                            {
                                modDestination.cancel( out );
                                out = null;
                            }
                        }
                    }
                    finally
                    {
                        // out may have already been closed if there was a problem.
                        if ( out != null )
                        {
                            out.close();
                        }
                    }
                }
                finally
                {
                    in.close();
                }
            } catch (IOException ioe) {
                if ( firstE == null )
                {
                    firstE = ioe;
                }
            }

            // If there were any problems then wrap the original exception in a SourceException.
            if ( firstE != null )
            {
                throw new SourceException("Could not copy source '"+
                                          source.getURI()+"' to '"+
                                          destination.getURI()+"' :"+
                                          firstE.getMessage(), firstE);
            }
        }
    }

    /**
     * Copy the contents of an <code>InputStream</code> to an <code>OutputStream</code>.
     *
     * @param in
     * @param out
     * @throws IOException
     */
    static public void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[8192];
        int length = -1;

        while ((length = in.read(buffer))>-1) {
            out.write(buffer, 0, length);
        }
        in.close();
        out.flush();
        out.close();
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
     * This method is internally used by {@link #absolutize(String, String, boolean, boolean)}.
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
     * Used internally by {@link #absolutize(String, String, boolean, boolean)}.
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
