/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util;

import java.io.File;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-12-18 15:01:21 $
 */

public class NetUtils {

    /**
     * Create a URL from a location. This method supports the
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
            URL u = ClassUtils.getResource(location.substring("resource://".length()));
            if (u != null) return u;
            else throw new RuntimeException(location + " could not be found. (possible classloader problem)");
        } else {
            return new URL(location);
        }
    }

    /**
     * Returns the path of the given resource.
     *
     * @path the resource
     * @return the resource path
     */
    public static String getPath(String uri) {
        int i = uri.lastIndexOf('/');
        return (i > -1) ? uri.substring(0, i) : "";
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

    /**
     * Absolutize a relative resource on the given absolute path.
     *
     * @path the absolute path
     * @relativeResource the relative resource
     * @return the absolutized resource
     */
    public static String absolutize(String path, String relativeResource) {
        if (("".equals(path)) || (path == null)) return relativeResource;
        if (relativeResource.charAt(0) != '/') {
            int length = path.length() - 1;
            boolean slashPresent = (path.charAt(length) == '/');
            StringBuffer b = new StringBuffer();
            b.append(path);
            if (!slashPresent) b.append('/');
            b.append(relativeResource);
            return b.toString();
        } else {
            // resource is already absolute
            return relativeResource;
        }
    }

    /**
     * Relativize an absolute resource on a given absolute path.
     *
     * @path the absolute path
     * @relativeResource the absolute resource
     * @return the resource relative to the given path
     */
    public static String relativize(String path, String absoluteResource) {
        if (("".equals(path)) || (path == null)) return absoluteResource;
        int length = path.length() - 1;
        boolean slashPresent = path.charAt(length) == '/';
        if (absoluteResource.startsWith(path)) {
            // resource is direct descentant
            return absoluteResource.substring(length + (slashPresent ? 1 : 2));
        } else {
            // resource is not direct descendant
            if (!slashPresent) path += "/";
            int index = StringUtils.matchStrings(path, absoluteResource);
            String pathDiff = path.substring(index);
            String resource = absoluteResource.substring(index);
            int levels = StringUtils.count(pathDiff, '/');
            StringBuffer b = new StringBuffer();
            for (int i = 0; i < levels; i++) {
                b.append("../");
            }
            b.append(resource);
            return b.toString();
        }
    }

    /**
     * Normalize a uri containing ../ and ./ paths.
     *
     * @param uri The uri path to normalize
     * @return The normalized uri
     */
    public static String normalize(String uri) {
        String[] dirty = StringUtils.split(uri, "/");
        int length = dirty.length;
        String[] clean = new String[length];

        boolean path;
        boolean finished;
        while (true) {
            path = false;
            finished = true;
            for (int i = 0, j = 0; (i < length) && (dirty[i] != null); i++) {
                if (".".equals(dirty[i])) {
                    // ignore
                } else if ("..".equals(dirty[i])) {
                    clean[j++] = dirty[i];
                    if (path) finished = false;
                } else {
                    if ((i+1 < length) && ("..".equals(dirty[i+1]))) {
                        i++;
                    } else {
                        clean[j++] = dirty[i];
                        path = true;
                    }
                }
            }
            if (finished) {
                break;
            } else {
                dirty = clean;
                clean = new String[length];
            }
        }

        StringBuffer b = new StringBuffer(uri.length());

        for (int i = 0; (i < length) && (clean[i] != null); i++) {
            b.append(clean[i]);
            if ((i+1 < length) && (clean[i+1] != null)) b.append("/");
        }

        return b.toString();
    }

    /**
     * Remove parameters from a uri.
     *
     * @param uri The uri path to deparameterize.
     * @param parameters The map that collects parameters.
     * @return The cleaned uri
     */
    public static String deparameterize(String uri, Map parameters) {
        int i = uri.lastIndexOf('?');
        if (i == -1) return uri;
        String[] params = StringUtils.split(uri.substring(i+1), "&");
        for (int j = 0; j < params.length; j++) {
            String p = params[j];
            int k = p.indexOf('=');
            if (k == -1) break;
            String name = p.substring(0, k);
            String value = p.substring(k+1);
            parameters.put(name, value);
        }
        return uri.substring(0, i);
    }

    public static void main(String[] args) {
        String absoluteURI = absolutize(args[0], args[1]);
        String normalizedURI = normalize(absoluteURI);
        String relativeURI = relativize(args[0], normalizedURI);
        System.out.println(absoluteURI + " --> " + normalizedURI + " --> " + relativeURI);
    }
}
