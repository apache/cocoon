package org.apache.cocoon.components.store;

import java.io.File;
import java.util.Enumeration;

import org.apache.cocoon.util.IOUtils;

import org.apache.avalon.AbstractLoggable;

import org.apache.avalon.ThreadSafe;

import java.io.IOException;

public class FilesystemStore extends AbstractLoggable implements Store, ThreadSafe {
  /** The directory repository */
  protected File directoryFile;
  protected volatile String directoryPath;

  /**
   * Sets the repository's location
   */
  public void setDirectory(String directory) throws IOException {
      this.setDirectory(new File(directory));
  }

  /**
   * Sets the repository's location
   */
  public void setDirectory(File directory) throws IOException {
    this.directoryFile = directory;

    /* Save directory path prefix */
    this.directoryPath = IOUtils.getFullFilename(this.directoryFile);
    this.directoryPath += File.separator;

    /* Does directory exist? */
    if (!this.directoryFile.exists()) {
      /* Create it anew */
      if (!this.directoryFile.mkdir()) {
        throw new IOException(
      "Error creating store directory '" + this.directoryPath + "': "
    );
      }
    }

    /* Is given file actually a directory? */
    if (!this.directoryFile.isDirectory()) {
      throw new IOException("'" + this.directoryPath + "' is not a directory");
    }

    /* Is directory readable and writable? */
    if (!(this.directoryFile.canRead() && this.directoryFile.canWrite())) {
      throw new IOException(
        "Directory '" + this.directoryPath + "' is not readable/writable"
      );
    }
  }

  /**
   * Returns the repository's full pathname
   */
  public String getDirectoryPath() {
    return this.directoryPath;
  }

  /**
   * Get the file associated with the given unique key name.
   */
  public Object get(Object key) {
    File file = fileFromKey(key);

    if (file != null && file.exists()) {
      return file;
    }

    return null;
  }

  /**
   * Store the given object in a persistent state.
   * 1) Null values generate empty directories.
   * 2) String values are dumped to text files
   * 3) Object values are serialized
   */
  public void store(Object key, Object value) throws IOException {
      File file = fileFromKey(key);

      /* Create subdirectories as needed */
      File parent = file.getParentFile();
      if (parent != null) {
        parent.mkdirs();
      }

      /* Store object as file */
      if (value == null) { /* Directory */
        if (file.exists()) {
          if (!file.delete()) { /* FAILURE */
           getLogger().error("File cannot be deleted: " + file.toString());
           return;
          }
        }

        file.mkdir();
      } else if (value instanceof String) { /* Text file */
        IOUtils.serializeString(file, (String) value);
      } else { /* Serialized Object */
        IOUtils.serializeObject(file, value);
      }
  }

  /**
   * Holds the given object in a volatile state.
   */
  public void hold(Object key, Object value) throws IOException {
    this.store(key, value);
    File file = (File) this.get(key);
    if (file != null) {
      file.deleteOnExit();
    }
  }

  /**
   * Remove the object associated to the given key.
   */
  public void remove(Object key) {
    File file = fileFromKey(key);
    if (file != null) {
      file.delete();
    }
  }

  /**
   * Indicates if the given key is associated to a contained object.
   */
  public boolean containsKey(Object key) {
    File file = fileFromKey(key);
    if (file == null) {
      return false;
    }
    return file.exists();
  }

  /**
   * Returns the list of stored files as an Enumeration of Files
   */
  public Enumeration keys() {
    /* Not yet implemented */
    return null;
  }

  /* Utility Methods*/
  protected File fileFromKey(Object key) {
    String name = key.toString();
    return IOUtils.createFile(this.directoryFile, name);
  }

  public String getString(Object key) throws IOException {
    File file = (File) this.get(key);
    if (file != null) {
      return IOUtils.deserializeString(file);
    }

    return null;
  }

  public Object getObject(Object key)
    throws IOException, ClassNotFoundException
  {
    File file = (File) this.get(key);
    if (file != null) {
      return IOUtils.deserializeObject(file);
    }

    return null;
  }
}
