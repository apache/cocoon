/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-02 11:07:33 $
 */

public class NetUtils {

    /**
     Create a URL from a location. This method supports the
     * <i>resource://</i> pseudo-protocol for loading resources
     * accessible to this same class' <code>ClassLoader</code>
     *
     * @param location The location
     * @return The URL pointed to by the location
     * @exception MalformedURLException If the location is malformed
     */
    public static URL getURL(String location) throws MalformedURLException {
        if (location.indexOf("://") < 0) {
            return (new File(location)).toURL();
        } else if (location.startsWith("resource://")) {
            URL u = ClassUtils.getClassLoader().getResource(location.substring("resource://".length()));
            if (u != null) return u;
            else throw new RuntimeException(location + " could not be found. (possible classloader problem)");
        } else {
            return new URL(location);
        }
    }

    /**
     * Adjusts the context the location of the child depending on the
     * parent context.
     *
     * @param parentURI the parent context
     * @param childURI the context child
     * @return The location with the adjusted context
     */
    public static String adjustContext(String parentURI, String childURI) {
        if (childURI.charAt(0) != '/') {
            int lastSlash = parentURI.lastIndexOf('/');
            if (lastSlash > -1) {
                return parentURI.substring(0, lastSlash + 1) + childURI;
            } else {
                return childURI;
            }
        } else {
            return childURI;
        }
    }

    /**
     * Normalize a uri containing ../ and ./ paths (the leading .. or . are 
     * left unchanged)
     *
     * @param uri The uri path to normalize
     * @return The normalized uri
     */
    public static String normalizeURI(String uri) {
        String[] dirty = StringUtils.split(uri, "/");
        int length = dirty.length;
        String[] clean = new String[length];

        boolean stillDirty;
        do {
            stillDirty = false;
            for (int i = 0, j = 0; (i < length) && (dirty[i] != null); i++) {
                if (!".".equals(dirty[i])) {
                    if ("..".equals(dirty[i])) {
                        stillDirty = true;
                    } else if ((i+1 < length) && ("..".equals(dirty[i+1]))) {
                        i += 2;
                    }
                    clean[j++] = dirty[i];
                }
            }
            dirty = clean;
            clean = new String[length];
        } while (stillDirty);

        StringBuffer b = new StringBuffer(uri.length());
        
        for (int i = 0; (i < length) && (dirty[i] != null); i++) {
            b.append(dirty[i]);
            if ((i+1 < length) && (dirty[i+1] != null)) b.append("/");
        }
        
        return b.toString();
    }

    public static void main (String[] a) {
        System.out.println(a[0] + " ---> " + normalizeURI(a[0]));
    }
    
    /**
    * Remove path and file information from a filename returning only its
    * extension  component
    *
    * @param filename The filename
    * @return The filename extension (with starting dot!)
    */
    public static String getExtension(String uri) {
        int dot = uri.lastIndexOf('.');
        if (dot > -1) {
            uri = uri.substring(dot);
            int slash = uri.lastIndexOf('/');
            if (slash > -1) {
                return null;
            } else {
                int sharp = uri.lastIndexOf('#');
                if (sharp > -1) {
                    return uri.substring(dot, sharp);
                } else {
                    int mark = uri.lastIndexOf('?');
                    if (mark > -1) {
                        return uri.substring(dot, mark);
                    } else {
                        return uri;
                    }
                }
            }
        } else {
            return null;
        }
    }
}
