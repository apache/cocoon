/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.util;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

import org.apache.log.LogKit;

/**
 * A collection of <code>File</code>, <code>URL</code> and filename
 * utility methods
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2001-02-21 17:22:31 $
 */
public class IOUtils {

  // **********************
  // Serialize Methods
  // **********************

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
  // File Methods
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

      char[] chars = path[i].toCharArray();
      if (chars.length < 1) buffer.append('_');

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
    return (i > -1) ? filename.substring(0, i) : filename;
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
    return (i > -1) ? filename.substring(i + 1) : filename;
  }

  /**
   * Strip a filename of its <i>last</i> extension (the portion
   * immediately following the last dot character, if any)
   *
   * @param filename The filename
   * @return The filename sans extension
   */
  public static String baseName(String filename) {
    int i = filename.lastIndexOf('.');
    return (i > -1) ? filename.substring(0, i) : filename;
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
    } catch (Exception e) {
      LogKit.getLoggerFor("cocoon").debug("IOUtils.getFullFilename", e);
      return file.getAbsolutePath();
    }
  }

  /**
   * Return the path within a base directory
   */
  public static String getContextFilePath(String contextDir, String filePath) {
      if (filePath.startsWith(contextDir)) {
          return filePath.substring(contextDir.length());
      }

      return filePath;
  }

  /**
   * Return a file with the given filename creating the necessary
   * directories if not present.
   *
   * @param filename The file
   * @return The created File instance
   */
  public static File createFile(File destDir, String filename) {
    File file = new File(destDir, filename);
    File parent = file.getParentFile();
    if (parent != null) parent.mkdirs();
    return file;
  }
}
