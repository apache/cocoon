/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.util;

import java.net.URL;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-09-06 23:22:26 $
 */
public class IOUtils {
  /**
   * Create a URL from a location. This method supports the
   * <i>resource://</i> pseudo-protocol for loading resources
   * accessible to this same class' <code>ClassLoader</code>
   *
   * @param location The location
   * @return The URL pointed to by the location
   * @exception MalformedURLException If the location is malformed
   */
  public static URL getURL(String location)
    throws MalformedURLException
  {
    if (location.indexOf("://") < 0) {
      // Absolute file location
      File file = new File(location);

      return new URL(
        "file://" +
	getFullFilename(file).replace(File.separatorChar, '/')
      );
    } else if (location.startsWith("resource://")) {
      return ClassUtils.getClassLoader().getSystemResource(
        location.substring("resource://".length())
      );
    } else {
      return new URL(location);
    }
  }

  /**
   * Dump a <code>String</code> to a text file.
   *
   * @param file The output file
   * @param string The string to be dumped
   * @exception IOException IO Error
   */
  public static void serializeString(File file, String string)
    throws IOException
  {
    FileWriter fw = new FileWriter(file);
    fw.write(string);
    fw.flush();
    fw.close();
  }

  /**
   * Load a text file contents as a <code>String<code>.
   * This method does not perform enconding conversions
   *
   * @param file The input file
   * @return The file contents as a <code>String</code>
   * @exception IOException IO Error
   */
  public static String deserializeString(File file)
    throws IOException
  {
    int len;
    char[] chr = new char[4096];
    FileReader reader = new FileReader(file);
    StringBuffer buffer = new StringBuffer();

    while ((len = reader.read(chr)) > 0) {
      buffer.append(chr, 0, len);
    }

    return buffer.toString();
  }

  /**
   * This method serializes an object to an output stream.
   *
   * @param file The output file
   * @param object The object to be serialized
   * @exception IOException IOError
   */
  public static void serializeObject(File file, Object object)
    throws IOException
  {
    FileOutputStream fos = new FileOutputStream(file);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(object);
    oos.flush();
    fos.close();
  }

  /**
   * This method deserializes an object from an input stream.
   *
   * @param file The input file
   * @return The deserialized object
   * @exception IOException IOError
   */
  public static Object deserializeObject(File file)
   throws IOException, ClassNotFoundException
  {
    FileInputStream fis = new FileInputStream(file);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object object = ois.readObject();
    fis.close();
    return object;
  }

  // **********************
  // Filename Methods
  // **********************

  /**
   * Return a modified filename suitable for replicating directory
   * structures below the store's base directory. The following
   * conversions are performed:
   * <ul>
   * <li>Path separators are converted to regular directory names</li>
   * <li>File path components are transliterated to make them valid (?)
   *     programming language identifiers. This transformation may well
   *     generate collisions for unusual filenames.</li>
   * </ul>
   * @return The transformed filename
   */
  public static String normalizedFilename(String filename) {
    String[] path = StringUtils.split(filename, File.separator);
    int start = (path[0].length() == 0) ? 1 : 0;

    StringBuffer buffer = new StringBuffer();
    for (int i = start; i < path.length; i++) {
      if (i > start) {
        buffer.append(File.separator);
      }

      buffer.append('_');
      char[] chars = path[i].toCharArray();

      for (int j = 0; j < chars.length; j++) {
        if (StringUtils.isAlphaNumeric(chars[j])) {
          buffer.append(chars[j]);
        } else {
          buffer.append('_');
        }
      }
    }

    return buffer.toString();
  }

  /**
   * Remove file information from a filename returning only its path
   * component
   *
   * @param filename The filename
   * @return The path information
   */
  public static String pathComponent(String filename) {
    int i = filename.lastIndexOf(File.separator);
    return (i >= 0) ? filename.substring(0, i) : filename;
  }

  /**
   * Remove path information from a filename returning only its file
   * component
   *
   * @param filename The filename
   * @return The filename sans path information
   */
  public static String fileComponent(String filename) {
    int i = filename.lastIndexOf(File.separator);
    return (i >= 0) ? filename.substring(i + 1) : filename;
  }

  /**
   * Strip a filename of its <i>last</i> extension (the portion
   * immediately following the last dot character, if any)
   *
   * @param filename The filename
   * @return The filename sans extension
   */
  public static String baseName(String filename) {
    return baseName(filename, ".");
  }

  public static String baseName(String filename, String suffix) {
    int lastDot = filename.lastIndexOf(suffix);

    if (lastDot >= 0) {
      filename = filename.substring(0, lastDot);
    }

    return filename;
  }

  /**
   * Get the complete filename corresponding to a (typically relative)
   * <code>File</code.
   * This method accounts for the possibility of an error in getting
   * the filename's <i>canonical</i> path, returning the io/error-safe
   * <i>absolute</i> form instead
   *
   * @param file The file
   * @return The file's absolute filename
   */
  public static String getFullFilename(File file) {
    try {
      return file.getCanonicalPath();
    } catch (IOException e) {
      return file.getAbsolutePath();
    }
  }
}
