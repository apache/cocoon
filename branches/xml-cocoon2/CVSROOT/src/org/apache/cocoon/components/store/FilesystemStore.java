package org.apache.cocoon.components.store;

import java.io.File;
import java.util.Enumeration;

import org.apache.cocoon.util.IOUtils;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

import org.apache.avalon.ThreadSafe;

import java.io.IOException;

public class FilesystemStore implements Store, ThreadSafe, Loggable {
  /** The directory repository */
  protected File directoryFile;
  protected volatile String directoryPath;

  private Logger log;

  /**
   * Constructor
   */
  public FilesystemStore(String directoryName) throws IOException {
    this(new File(directoryName));
  }

  public FilesystemStore(File directoryFile) throws IOException {
    this.directoryFile = directoryFile;

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

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
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
  public void store(Object key, Object value) {
    try {
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
           log.error("File cannot be deleted: " + file.toString());
           return;
          }
        }

        file.mkdir();
      } else if (value instanceof String) { /* Text file */
        IOUtils.serializeString(file, (String) value);
      } else { /* Serialized Object */
        IOUtils.serializeObject(file, value);
      }
    } catch (Exception e) { /* FAILURE */
      log.warn("FilesystemStore.store()", e);
      throw new RuntimeException(e.getMessage());
    }
  }

  /**
   * Holds the given object in a volatile state.
   */
  public void hold(Object key, Object value) {
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

    name = IOUtils.getFullFilename(new File(name));

    String path = IOUtils.normalizedFilename(IOUtils.pathComponent(name));
    String filename = name.substring(name.lastIndexOf(File.separatorChar));

    String extension = null;
    int extensionPosition = filename.lastIndexOf(".");
    if (extensionPosition >= 0) {
      extension = filename.substring(extensionPosition + 1);
      filename = filename.substring(0, extensionPosition);
    }

    filename = IOUtils.normalizedFilename(filename);

    if (extension != null) {
      filename += "." + extension;
    }

    return new File(this.directoryPath + path + File.separator + filename);
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

  public String normalizedFilename(String filename) {
    return IOUtils.normalizedFilename(filename);
  }
}
